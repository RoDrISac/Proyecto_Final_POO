package proyectofina.persistencia.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import proyectofina.Cliente;
import proyectofina.DetalleVenta;
import proyectofina.Producto;
import proyectofina.Rol;
import proyectofina.Usuario;
import proyectofina.Venta;

/**
 * CRUD de ventas y detalle_venta. Una venta y sus detalles se guardan juntos
 * (composicion): si se elimina la venta, la BD borra su detalle en cascada.
 */
public class VentaDao extends DaoBase<Venta> {
    private final List<Rol> roles;

    /** El descuento se aplica despues de cargar los detalles (se calcula sobre el subtotal). */
    private final Map<String, Double> descuentos = new java.util.HashMap<>();

    /** Tabla ventas, clave id_venta; necesita los roles para reconstruir al vendedor. */
    public VentaDao(List<Rol> roles) {
        super("ventas", "id_venta");
        this.roles = roles;
    }

    /** Lee la cabecera; los detalles se agregan despues con cargarDetalles. */
    @Override
    protected Venta leer(Connection c, ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(rs.getString("id_cliente"), rs.getString("cliente_nombre"),
                rs.getString("cliente_apellido"), rs.getString("cliente_dni"), texto(rs, "cliente_direccion"),
                texto(rs, "cliente_telefono"), texto(rs, "cliente_correo"));
        String nombreRol = rs.getString("vendedor_rol");
        Rol rol = roles.stream().filter(r -> r.getNombreRol().equals(nombreRol)).findFirst()
                .orElse(new Rol(nombreRol, List.of(), ""));
        Usuario vendedor = new Usuario(rs.getString("id_vendedor"), rs.getString("vendedor_nombre"),
                rs.getString("vendedor_apellido"), rs.getString("vendedor_dni"), rol, "");
        Venta v = new Venta(rs.getString("id_venta"), fecha(rs, "fecha"), cliente, vendedor,
                rs.getString("metodo_pago"), rs.getString("tipo_comprobante"));
        v.setEstado(rs.getString("estado"));
        descuentos.put(v.getIdVenta(), rs.getDouble("descuento_pct"));
        return v;
    }

    /** Todas las ventas con su detalle, en orden cronológico. */
    @Override
    public List<Venta> listar(Connection c) throws SQLException {
        return conDetalles(c, select() + " ORDER BY fecha, id_venta");
    }

    /** Una venta con su detalle. */
    @Override
    public Optional<Venta> buscar(Connection c, String id) throws SQLException {
        return conDetalles(c, select() + " WHERE id_venta = ?", id).stream().findFirst();
    }

    /** Consulta con filtro de fechas (reporte por periodo). */
    public List<Venta> listarPorPeriodo(Connection c, Date desde, Date hasta) throws SQLException {
        return conDetalles(c, select() + " WHERE fecha >= ? AND fecha < ? ORDER BY fecha",
                new java.sql.Timestamp(desde.getTime()), new java.sql.Timestamp(hasta.getTime()));
    }

    /** Ejecuta la consulta de cabeceras y luego agrega el detalle de cada venta. */
    private List<Venta> conDetalles(Connection c, String sql, Object... params) throws SQLException {
        Map<String, Venta> ventas = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = leer(c, rs);
                    ventas.put(v.getIdVenta(), v);
                }
            }
        }
        cargarDetalles(c, ventas);
        return new ArrayList<>(ventas.values());
    }

    /** Una sola consulta con JOIN trae el detalle de todas las ventas leidas. */
    private void cargarDetalles(Connection c, Map<String, Venta> ventas) throws SQLException {
        if (ventas.isEmpty()) {
            return;
        }
        String sql = "SELECT d.id_venta, d.id_producto, d.nombre, d.modelo, d.categoria, d.descripcion, d.cantidad, d.precio_unitario "
                + "FROM detalle_venta d INNER JOIN ventas v ON v.id_venta = d.id_venta ORDER BY d.id_venta, d.linea";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Venta v = ventas.get(rs.getString("id_venta"));
                if (v == null) {
                    continue;
                }
                double precio = rs.getBigDecimal("precio_unitario").doubleValue();
                Producto p = new Producto(rs.getString("id_producto"), rs.getString("nombre"), texto(rs, "modelo"),
                        texto(rs, "categoria"), precio, texto(rs, "descripcion"));
                v.agregarProducto(p, rs.getInt("cantidad"), precio);
            }
        }
        for (Venta v : ventas.values()) {
            v.aplicarDescuento(descuentos.getOrDefault(v.getIdVenta(), 0.0));
        }
    }

    /** INSERT de la cabecera (con instantáneas de cliente y vendedor) y de cada línea de detalle. */
    @Override
    public void insertar(Connection c, Venta v) throws SQLException {
        Cliente cl = v.getCliente();
        Usuario ve = v.getVendedor();
        ejecutar(c, "INSERT INTO ventas(id_venta, fecha, id_cliente, cliente_nombre, cliente_apellido, cliente_dni, cliente_direccion, "
                + "cliente_telefono, cliente_correo, id_vendedor, vendedor_nombre, vendedor_apellido, vendedor_dni, vendedor_rol, "
                + "metodo_pago, tipo_comprobante, estado, descuento_pct, monto_total) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                v.getIdVenta(), v.getFecha(), cl.getIdCliente(), cl.getNombre(), cl.getApellido(), cl.getDni(), cl.getDireccion(),
                cl.getTelefono(), cl.getCorreoElectronico(), ve.getIdUsuario(), ve.getNombre(), ve.getApellido(), ve.getDni(),
                ve.getRol().getNombreRol(), v.getMetodoPago(), v.getTipoComprobante(), v.getEstado(),
                ProductoDao.dinero(v.getDescuentoPorcentaje()), ProductoDao.dinero(v.getMontoTotal()));
        int linea = 1;
        for (DetalleVenta d : v.getDetalles()) {
            Producto p = d.getProducto();
            ejecutar(c, "INSERT INTO detalle_venta(id_venta, linea, id_producto, nombre, modelo, categoria, descripcion, "
                    + "cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    v.getIdVenta(), linea++, p.getIdProducto(), p.getNombre(), p.getModelo(), p.getCategoria(), p.getDescripcion(),
                    d.getCantidad(), ProductoDao.dinero(d.getPrecioUnitario()));
        }
    }

    /** Una venta registrada no cambia sus productos; solo puede cambiar su estado. */
    @Override
    public void actualizar(Connection c, Venta v) throws SQLException {
        ejecutar(c, "UPDATE ventas SET estado = ? WHERE id_venta = ?", v.getEstado(), v.getIdVenta());
    }

    /** Clave primaria de la venta. */
    @Override
    public String id(Venta v) {
        return v.getIdVenta();
    }
}
