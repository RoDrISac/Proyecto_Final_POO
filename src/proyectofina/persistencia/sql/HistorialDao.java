package proyectofina.persistencia.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import proyectofina.RegistroAuditoria;
import proyectofina.persistencia.Estado.MovimientoMateria;

/**
 * Tablas de historial que solo crecen: auditoria, kardex de materias primas y
 * meses cerrados. Nunca se modifican ni se borran filas.
 */
public class HistorialDao {

    /** SELECT de la auditoría en orden de registro. */
    public List<RegistroAuditoria> listarAuditoria(Connection c) throws SQLException {
        List<RegistroAuditoria> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT fecha, id_usuario, accion, detalle FROM auditoria ORDER BY id_registro");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new RegistroAuditoria(new Date(rs.getTimestamp(1).getTime()), rs.getString(2), rs.getString(3), rs.getString(4)));
            }
        }
        return lista;
    }

    /** INSERT por lotes solo de los registros nuevos (la tabla nunca se modifica). */
    public void agregarAuditoria(Connection c, List<RegistroAuditoria> registros) throws SQLException {
        int enTabla = contar(c, "auditoria");
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO auditoria(fecha, id_usuario, accion, detalle) VALUES (?, ?, ?, ?)")) {
            for (int i = enTabla; i < registros.size(); i++) {
                RegistroAuditoria r = registros.get(i);
                ps.setTimestamp(1, new Timestamp(r.fecha().getTime()));
                ps.setString(2, r.usuario());
                ps.setString(3, r.accion());
                String d = r.detalle() == null ? "" : r.detalle();
                ps.setString(4, d.length() > 500 ? d.substring(0, 500) : d);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** SELECT del kardex de materias primas. */
    public List<MovimientoMateria> listarMovimientosMateria(Connection c) throws SQLException {
        List<MovimientoMateria> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT fecha, id_usuario, id_materia, tipo, cantidad, saldo, referencia FROM movimientos_materia ORDER BY id_movimiento");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new MovimientoMateria(new Date(rs.getTimestamp(1).getTime()), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getBigDecimal(5).doubleValue(), rs.getBigDecimal(6).doubleValue(), rs.getString(7)));
            }
        }
        return lista;
    }

    /** INSERT por lotes de los movimientos de materia prima nuevos. */
    public void agregarMovimientosMateria(Connection c, List<MovimientoMateria> movimientos) throws SQLException {
        int enTabla = contar(c, "movimientos_materia");
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO movimientos_materia(fecha, id_usuario, id_materia, tipo, cantidad, saldo, referencia) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            for (int i = enTabla; i < movimientos.size(); i++) {
                MovimientoMateria m = movimientos.get(i);
                ps.setTimestamp(1, new Timestamp(m.fecha().getTime()));
                ps.setString(2, m.usuario());
                ps.setString(3, m.materia());
                ps.setString(4, m.tipo());
                ps.setBigDecimal(5, java.math.BigDecimal.valueOf(m.cantidad()));
                ps.setBigDecimal(6, java.math.BigDecimal.valueOf(m.saldo()));
                ps.setString(7, m.referencia());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** SELECT de los meses con la contabilidad cerrada. */
    public Set<String> listarCierres(Connection c) throws SQLException {
        Set<String> meses = new TreeSet<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT periodo FROM cierres_mes"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                meses.add(rs.getString(1));
            }
        }
        return meses;
    }

    /** INSERT de los meses cerrados que aún no están en la tabla. */
    public void agregarCierres(Connection c, Set<String> meses) throws SQLException {
        Set<String> enTabla = listarCierres(c);
        for (String m : meses) {
            if (!enTabla.contains(m)) {
                DaoBase.ejecutar(c, "INSERT INTO cierres_mes(periodo) VALUES (?)", m);
            }
        }
    }

    /** Número de filas de una tabla. */
    private static int contar(Connection c, String tabla) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM " + tabla); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
