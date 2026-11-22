package proyectofina.persistencia.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import proyectofina.FallaProduccion;
import proyectofina.OperacionProduccion;
import proyectofina.Produccion;

/**
 * Producciones y fallas. Las dos subclases se guardan en una sola tabla con la
 * columna "tipo"; al leer se crea el objeto de la subclase correcta. Son un
 * historial: solo se insertan las operaciones nuevas.
 */
public class OperacionProduccionDao {

    /** SELECT de las operaciones; según la columna tipo crea Produccion o FallaProduccion y luego carga sus consumos con un JOIN. */
    public List<OperacionProduccion> listar(Connection c) throws SQLException {
        Map<String, OperacionProduccion> ops = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM operaciones_produccion ORDER BY fecha, id_operacion");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OperacionProduccion o;
                if ("PRODUCCION".equals(rs.getString("tipo"))) {
                    o = new Produccion(rs.getString("id_operacion"), DaoBase.fecha(rs, "fecha"), rs.getString("id_usuario"),
                            rs.getString("id_producto"), rs.getString("nombre_producto"), rs.getInt("cantidad"), rs.getInt("aprobadas"),
                            rs.getString("obs_calidad"), rs.getBigDecimal("costo_mano_obra").doubleValue());
                } else {
                    o = new FallaProduccion(rs.getString("id_operacion"), DaoBase.fecha(rs, "fecha"), rs.getString("id_usuario"),
                            rs.getString("motivo"));
                }
                ops.put(o.getIdOperacion(), o);
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT c.* FROM consumos_materia c INNER JOIN operaciones_produccion o ON o.id_operacion = c.id_operacion "
                + "ORDER BY c.id_operacion, c.linea");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OperacionProduccion o = ops.get(rs.getString("id_operacion"));
                if (o != null) {
                    o.agregarConsumo(new OperacionProduccion.Consumo(rs.getString("id_materia"),
                            rs.getString("nombre"), rs.getString("unidad"),
                            rs.getBigDecimal("cantidad").doubleValue(), rs.getBigDecimal("costo_unitario").doubleValue()));
                }
            }
        }
        return new ArrayList<>(ops.values());
    }

    /** INSERT de las operaciones nuevas con sus consumos de materia prima. */
    public void agregarNuevas(Connection c, List<OperacionProduccion> operaciones) throws SQLException {
        Set<String> enTabla = new java.util.HashSet<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT id_operacion FROM operaciones_produccion");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                enTabla.add(rs.getString(1));
            }
        }
        for (OperacionProduccion o : operaciones) {
            if (enTabla.contains(o.getIdOperacion())) {
                continue;
            }
            if (o instanceof Produccion p) {
                DaoBase.ejecutar(c, "INSERT INTO operaciones_produccion(id_operacion, tipo, fecha, id_usuario, id_producto, "
                        + "nombre_producto, cantidad, aprobadas, obs_calidad, costo_mano_obra) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        p.getIdOperacion(), p.getTipo(), p.getFecha(), p.getUsuario(), p.getIdProducto(), p.getNombreProducto(),
                        p.getCantidad(), p.getAprobadas(), p.getObservacionCalidad(), ProductoDao.dinero(p.getCostoManoObra()));
            } else if (o instanceof FallaProduccion f) {
                DaoBase.ejecutar(c, "INSERT INTO operaciones_produccion(id_operacion, tipo, fecha, id_usuario, motivo) "
                        + "VALUES (?, ?, ?, ?, ?)",
                        f.getIdOperacion(), f.getTipo(), f.getFecha(), f.getUsuario(), f.getMotivo());
            }
            int linea = 1;
            for (var con : o.getConsumos()) {
                DaoBase.ejecutar(c, "INSERT INTO consumos_materia(id_operacion, linea, id_materia, nombre, unidad, cantidad, "
                        + "costo_unitario) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        o.getIdOperacion(), linea++, con.idMateria(), con.nombre(), con.unidad(), con.cantidad(), con.costoUnitario());
            }
        }
    }
}
