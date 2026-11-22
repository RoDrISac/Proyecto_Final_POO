package proyectofina.persistencia.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import proyectofina.MateriaPrima;

/** CRUD de la tabla materias_primas. */
public class MateriaPrimaDao extends DaoBase<MateriaPrima> {

    /** Tabla materias_primas, clave id_materia. */
    public MateriaPrimaDao() {
        super("materias_primas", "id_materia");
    }

    /** Convierte la fila actual en una MateriaPrima. */
    @Override
    protected MateriaPrima leer(Connection c, ResultSet rs) throws SQLException {
        return new MateriaPrima(rs.getString("id_materia"), rs.getString("nombre"), rs.getString("unidad"),
                rs.getBigDecimal("stock").doubleValue(), rs.getBigDecimal("stock_minimo").doubleValue(),
                rs.getBigDecimal("costo_unitario").doubleValue());
    }

    /** INSERT de una materia prima. */
    @Override
    public void insertar(Connection c, MateriaPrima m) throws SQLException {
        ejecutar(c, "INSERT INTO materias_primas(id_materia, nombre, unidad, stock, stock_minimo, costo_unitario) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                m.getIdMateria(), m.getNombre(), m.getUnidad(), m.getStock(), m.getStockMinimo(), m.getCostoUnitario());
    }

    /** UPDATE de stock, mínimo y costo. */
    @Override
    public void actualizar(Connection c, MateriaPrima m) throws SQLException {
        ejecutar(c, "UPDATE materias_primas SET nombre = ?, unidad = ?, stock = ?, stock_minimo = ?, costo_unitario = ? "
                + "WHERE id_materia = ?",
                m.getNombre(), m.getUnidad(), m.getStock(), m.getStockMinimo(), m.getCostoUnitario(), m.getIdMateria());
    }

    /** Clave primaria de la materia prima. */
    @Override
    public String id(MateriaPrima m) {
        return m.getIdMateria();
    }
}
