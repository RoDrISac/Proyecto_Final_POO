package proyectofina.persistencia.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import proyectofina.Pedido;

/** CRUD de pedidos y su detalle (composicion: el detalle se borra en cascada). */
public class PedidoDao extends DaoBase<Pedido> {

    /** Tabla pedidos, clave id_pedido. */
    public PedidoDao() {
        super("pedidos", "id_pedido");
    }

    /** Convierte la cabecera en un Pedido y restaura su estado. */
    @Override
    protected Pedido leer(Connection c, ResultSet rs) throws SQLException {
        Pedido p = new Pedido(rs.getString("id_pedido"), fecha(rs, "fecha"), new java.util.Date(rs.getDate("fecha_entrega").getTime()),
                rs.getString("id_cliente"), rs.getString("nombre_cliente"), rs.getString("prioridad"));
        p.restaurar(rs.getString("estado"), rs.getString("obs_calidad"), rs.getString("id_venta"));
        return p;
    }

    /** SELECT de pedidos y luego de su detalle, agrupando por pedido. */
    @Override
    public List<Pedido> listar(Connection c) throws SQLException {
        Map<String, Pedido> pedidos = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(select() + " ORDER BY id_pedido"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pedido p = leer(c, rs);
                pedidos.put(p.getIdPedido(), p);
            }
        }
        try (PreparedStatement ps = c.prepareStatement("SELECT d.* FROM detalle_pedido d ORDER BY d.id_pedido, d.linea");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pedido p = pedidos.get(rs.getString("id_pedido"));
                if (p != null) {
                    p.agregarDetalle(new Pedido.DetallePedido(rs.getString("id_producto"), rs.getString("nombre"), rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario").doubleValue(), rs.getString("personalizacion")));
                }
            }
        }
        return new ArrayList<>(pedidos.values());
    }

    /** Busca un pedido con su detalle. */
    @Override
    public Optional<Pedido> buscar(Connection c, String id) throws SQLException {
        return listar(c).stream().filter(p -> p.getIdPedido().equals(id)).findFirst();
    }

    /** INSERT del pedido y de cada línea de su detalle. */
    @Override
    public void insertar(Connection c, Pedido p) throws SQLException {
        ejecutar(c, "INSERT INTO pedidos(id_pedido, fecha, fecha_entrega, id_cliente, nombre_cliente, prioridad, estado, "
                + "obs_calidad, id_venta) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getIdPedido(), p.getFecha(), new java.sql.Date(p.getFechaEntrega().getTime()),
                p.getIdCliente(), p.getNombreCliente(), p.getPrioridad(), p.getEstado(), p.getObservacionCalidad(),
                p.getIdVenta().isEmpty() ? null : p.getIdVenta());
        int linea = 1;
        for (Pedido.DetallePedido d : p.getDetalles()) {
            ejecutar(c, "INSERT INTO detalle_pedido(id_pedido, linea, id_producto, nombre, cantidad, precio_unitario, "
                    + "personalizacion) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    p.getIdPedido(), linea++, d.idProducto(), d.nombre(), d.cantidad(),
                    ProductoDao.dinero(d.precioUnitario()), d.personalizacion());
        }
    }

    /** Un pedido cambia de estado, observacion de calidad y venta asociada. */
    @Override
    public void actualizar(Connection c, Pedido p) throws SQLException {
        ejecutar(c, "UPDATE pedidos SET estado = ?, obs_calidad = ?, id_venta = ? WHERE id_pedido = ?",
                p.getEstado(), p.getObservacionCalidad(), p.getIdVenta().isEmpty() ? null : p.getIdVenta(), p.getIdPedido());
    }

    /** Clave primaria del pedido. */
    @Override
    public String id(Pedido p) {
        return p.getIdPedido();
    }
}
