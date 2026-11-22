package proyectofina.persistencia.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Codigo comun a los DAO (herencia): consultas con PreparedStatement,
 * listar/buscar/eliminar/ids a partir del nombre de la tabla y su clave.
 * Las subclases solo definen como se lee una fila y como se escribe.
 */
public abstract class DaoBase<T> implements Dao<T> {
    protected final String tabla;
    protected final String clave;

    /** Nombre de la tabla y de su clave primaria. */
    protected DaoBase(String tabla, String clave) {
        this.tabla = tabla;
        this.clave = clave;
    }

    /** Convierte la fila actual del ResultSet en un objeto. */
    protected abstract T leer(Connection c, ResultSet rs) throws SQLException;

    /** SELECT usado por listar y buscar (las subclases pueden usar JOIN). */
    protected String select() {
        return "SELECT * FROM " + tabla;
    }

    /** SELECT de todas las filas ordenadas por clave. */
    @Override
    public List<T> listar(Connection c) throws SQLException {
        List<T> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(select() + " ORDER BY " + clave);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(leer(c, rs));
            }
        }
        return lista;
    }

    /** SELECT por clave primaria. */
    @Override
    public Optional<T> buscar(Connection c, String id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(select() + " WHERE " + clave + " = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(leer(c, rs)) : Optional.empty();
            }
        }
    }

    /** DELETE por clave primaria. */
    @Override
    public void eliminar(Connection c, String id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + tabla + " WHERE " + clave + " = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    /** Claves que existen hoy en la tabla (para sincronizar). */
    @Override
    public Set<String> ids(Connection c) throws SQLException {
        Set<String> ids = new LinkedHashSet<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT " + clave + " FROM " + tabla);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getString(1));
            }
        }
        return ids;
    }

    /** Ejecuta INSERT/UPDATE con parametros posicionales. */
    protected static int ejecutar(Connection c, String sql, Object... valores) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) {
                Object v = valores[i];
                if (v instanceof Date d) {
                    ps.setTimestamp(i + 1, new Timestamp(d.getTime()));
                } else {
                    ps.setObject(i + 1, v);
                }
            }
            return ps.executeUpdate();
        }
    }

    /** Lee una columna DATETIME como Date (o null). */
    protected static Date fecha(ResultSet rs, String columna) throws SQLException {
        Timestamp t = rs.getTimestamp(columna);
        return t == null ? null : new Date(t.getTime());
    }

    /** Lee una columna de texto; null se convierte en cadena vacía. */
    protected static String texto(ResultSet rs, String columna) throws SQLException {
        String v = rs.getString(columna);
        return v == null ? "" : v;
    }
}
