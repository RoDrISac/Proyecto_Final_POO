package proyectofina.persistencia.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import proyectofina.Rol;
import proyectofina.Usuario;

/** CRUD de la tabla usuarios. El SELECT une (JOIN) cada usuario con su rol. */
public class UsuarioDao extends DaoBase<Usuario> {
    private final List<Rol> roles;

    /** Recibe los roles ya cargados para asociar el objeto Rol a cada usuario. */
    public UsuarioDao(List<Rol> roles) {
        super("usuarios", "id_usuario");
        this.roles = roles;
    }

    /** SELECT con INNER JOIN a roles. */
    @Override
    protected String select() {
        return "SELECT u.id_usuario, u.nombre, u.apellido, u.dni, u.contrasena, r.nombre_rol "
             + "FROM usuarios u INNER JOIN roles r ON r.nombre_rol = u.nombre_rol";
    }

    /** Convierte la fila en un Usuario con su objeto Rol. */
    @Override
    protected Usuario leer(Connection c, ResultSet rs) throws SQLException {
        return new Usuario(rs.getString("id_usuario"), rs.getString("nombre"), rs.getString("apellido"),
                rs.getString("dni"), rol(rs.getString("nombre_rol")), rs.getString("contrasena"));
    }

    /** Busca el rol ya cargado por nombre. */
    private Rol rol(String nombre) throws SQLException {
        return roles.stream().filter(r -> r.getNombreRol().equals(nombre)).findFirst()
                .orElseThrow(() -> new SQLException("El rol " + nombre + " no existe."));
    }

    /** INSERT de un usuario. */
    @Override
    public void insertar(Connection c, Usuario u) throws SQLException {
        ejecutar(c, "INSERT INTO usuarios(id_usuario, nombre, apellido, dni, contrasena, nombre_rol) VALUES (?, ?, ?, ?, ?, ?)",
                u.getIdUsuario(), u.getNombre(), u.getApellido(), u.getDni(), u.getContraseña(), u.getRol().getNombreRol());
    }

    /** UPDATE de datos, contraseña y rol. */
    @Override
    public void actualizar(Connection c, Usuario u) throws SQLException {
        ejecutar(c, "UPDATE usuarios SET nombre = ?, apellido = ?, dni = ?, contrasena = ?, nombre_rol = ? WHERE id_usuario = ?",
                u.getNombre(), u.getApellido(), u.getDni(), u.getContraseña(), u.getRol().getNombreRol(), u.getIdUsuario());
    }

    /** Clave primaria del usuario. */
    @Override
    public String id(Usuario u) {
        return u.getIdUsuario();
    }
}
