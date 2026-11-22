package proyectofina.persistencia.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import proyectofina.Rol;

/** CRUD de las tablas roles y rol_permisos (un rol tiene varios permisos). */
public class RolDao extends DaoBase<Rol> {

    /** Tabla roles, clave nombre_rol. */
    public RolDao() {
        super("roles", "nombre_rol");
    }

    /** Convierte la fila en un Rol con sus permisos. */
    @Override
    protected Rol leer(Connection c, ResultSet rs) throws SQLException {
        String nombre = rs.getString("nombre_rol");
        return new Rol(nombre, permisos(c, nombre), rs.getString("contrasena_rol"));
    }

    /** SELECT de los permisos de un rol. */
    private List<String> permisos(Connection c, String rol) throws SQLException {
        List<String> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT permiso FROM rol_permisos WHERE nombre_rol = ? ORDER BY permiso")) {
            ps.setString(1, rol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString(1));
                }
            }
        }
        return lista;
    }

    /** Lee primero los roles y luego sus permisos, para no tener dos consultas abiertas a la vez. */
    @Override
    public List<Rol> listar(Connection c) throws SQLException {
        // Primero se leen los nombres y luego los permisos, para no tener dos consultas abiertas a la vez.
        List<String[]> filas = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT nombre_rol, contrasena_rol FROM roles ORDER BY nombre_rol");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new String[]{rs.getString(1), rs.getString(2)});
            }
        }
        List<Rol> roles = new ArrayList<>();
        for (String[] f : filas) {
            roles.add(new Rol(f[0], permisos(c, f[0]), f[1]));
        }
        return roles;
    }

    /** INSERT del rol y de sus permisos. */
    @Override
    public void insertar(Connection c, Rol r) throws SQLException {
        ejecutar(c, "INSERT INTO roles(nombre_rol, contrasena_rol) VALUES (?, ?)", r.getNombreRol(), r.getContraseñaRol());
        guardarPermisos(c, r);
    }

    /** UPDATE de la contraseña y reemplazo de sus permisos. */
    @Override
    public void actualizar(Connection c, Rol r) throws SQLException {
        ejecutar(c, "UPDATE roles SET contrasena_rol = ? WHERE nombre_rol = ?", r.getContraseñaRol(), r.getNombreRol());
        ejecutar(c, "DELETE FROM rol_permisos WHERE nombre_rol = ?", r.getNombreRol());
        guardarPermisos(c, r);
    }

    /** INSERT de cada permiso del rol. */
    private void guardarPermisos(Connection c, Rol r) throws SQLException {
        for (String permiso : r.getPermiso()) {
            ejecutar(c, "INSERT INTO rol_permisos(nombre_rol, permiso) VALUES (?, ?)", r.getNombreRol(), permiso);
        }
    }

    /** Clave primaria del rol. */
    @Override
    public String id(Rol r) {
        return r.getNombreRol();
    }
}
