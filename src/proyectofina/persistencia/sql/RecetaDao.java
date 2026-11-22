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
import proyectofina.Receta;

/** CRUD de recetas: cada receta son varias filas (producto, materia prima, cantidad). */
public class RecetaDao extends DaoBase<Receta> {

    /** Tabla recetas; una receta son varias filas con la misma clave id_producto. */
    public RecetaDao() {
        super("recetas", "id_producto");
    }

    /** No se usa: las recetas se leen agrupadas en listar(). */
    @Override
    protected Receta leer(Connection c, ResultSet rs) throws SQLException {
        throw new UnsupportedOperationException("Las recetas se leen agrupadas en listar().");
    }

    /** Agrupa las filas por producto. */
    @Override
    public List<Receta> listar(Connection c) throws SQLException {
        Map<String, Receta> recetas = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT r.id_producto, r.id_materia, r.cantidad_por_unidad FROM recetas r "
                + "INNER JOIN materias_primas m ON m.id_materia = r.id_materia ORDER BY r.id_producto, m.nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                recetas.computeIfAbsent(rs.getString(1), Receta::new)
                        .agregarInsumo(rs.getString(2), rs.getBigDecimal(3).doubleValue());
            }
        }
        return new ArrayList<>(recetas.values());
    }

    /** Receta de un producto. */
    @Override
    public Optional<Receta> buscar(Connection c, String id) throws SQLException {
        return listar(c).stream().filter(r -> r.getIdProducto().equals(id)).findFirst();
    }

    /** INSERT de cada materia prima de la receta. */
    @Override
    public void insertar(Connection c, Receta r) throws SQLException {
        for (Receta.Insumo i : r.getInsumos()) {
            ejecutar(c, "INSERT INTO recetas(id_producto, id_materia, cantidad_por_unidad) VALUES (?, ?, ?)",
                    r.getIdProducto(), i.idMateria(), i.cantidadPorUnidad());
        }
    }

    /** Una receta se reemplaza completa. */
    @Override
    public void actualizar(Connection c, Receta r) throws SQLException {
        eliminar(c, r.getIdProducto());
        insertar(c, r);
    }

    /** Clave de la receta: el producto. */
    @Override
    public String id(Receta r) {
        return r.getIdProducto();
    }
}
