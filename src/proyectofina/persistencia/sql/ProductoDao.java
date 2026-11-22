package proyectofina.persistencia.sql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import proyectofina.Inventario;

/** CRUD de la tabla productos (cada fila es un Inventario: Producto + existencias). */
public class ProductoDao extends DaoBase<Inventario> {

    /** Tabla productos, clave id_producto. */
    public ProductoDao() {
        super("productos", "id_producto");
    }

    /** Convierte la fila actual en un Inventario. */
    @Override
    protected Inventario leer(Connection c, ResultSet rs) throws SQLException {
        Inventario p = new Inventario(rs.getString("id_producto"), rs.getString("nombre"), texto(rs, "modelo"),
                texto(rs, "categoria"), rs.getBigDecimal("precio").doubleValue(), texto(rs, "descripcion"),
                rs.getInt("stock"), rs.getInt("stock_minimo"), rs.getBigDecimal("costo").doubleValue(),
                fecha(rs, "fecha_ingreso"));
        p.setUnidad(rs.getString("unidad"));
        return p;
    }

    /** INSERT de un producto. */
    @Override
    public void insertar(Connection c, Inventario p) throws SQLException {
        ejecutar(c, "INSERT INTO productos(id_producto, nombre, modelo, categoria, precio, descripcion, unidad, stock, "
                + "stock_minimo, costo, fecha_ingreso) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getIdProducto(), p.getNombre(), p.getModelo(), p.getCategoria(), dinero(p.getPrecio()), p.getDescripcion(),
                p.getUnidad(), p.getStock(), p.getStockMinimo(), dinero(p.getCosto()), p.getFechaIngreso());
    }

    /** UPDATE de datos, precio, stock y costo. */
    @Override
    public void actualizar(Connection c, Inventario p) throws SQLException {
        ejecutar(c, "UPDATE productos SET nombre = ?, modelo = ?, categoria = ?, precio = ?, descripcion = ?, unidad = ?, "
                + "stock = ?, stock_minimo = ?, costo = ?, fecha_ingreso = ? WHERE id_producto = ?",
                p.getNombre(), p.getModelo(), p.getCategoria(), dinero(p.getPrecio()), p.getDescripcion(), p.getUnidad(),
                p.getStock(), p.getStockMinimo(), dinero(p.getCosto()), p.getFechaIngreso(), p.getIdProducto());
    }

    /** Consulta con filtro: productos cuyo stock llego al minimo. */
    public List<Inventario> stockBajo(Connection c) throws SQLException {
        List<Inventario> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(select() + " WHERE stock <= stock_minimo ORDER BY stock");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(leer(c, rs));
            }
        }
        return lista;
    }

    /** Clave primaria del producto. */
    @Override
    public String id(Inventario p) {
        return p.getIdProducto();
    }

    /** Convierte a BigDecimal con 2 decimales para columnas DECIMAL. */
    static BigDecimal dinero(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
