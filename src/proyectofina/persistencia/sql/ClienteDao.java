package proyectofina.persistencia.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import proyectofina.Cliente;

/** CRUD de la tabla clientes. */
public class ClienteDao extends DaoBase<Cliente> {

    /** Tabla clientes, clave id_cliente. */
    public ClienteDao() {
        super("clientes", "id_cliente");
    }

    /** Convierte la fila actual en un Cliente. */
    @Override
    protected Cliente leer(Connection c, ResultSet rs) throws SQLException {
        return new Cliente(rs.getString("id_cliente"), rs.getString("nombre"), rs.getString("apellido"),
                rs.getString("dni"), texto(rs, "direccion"), texto(rs, "telefono"), texto(rs, "correo"), rs.getString("categoria"));
    }

    /** INSERT de un cliente. */
    @Override
    public void insertar(Connection c, Cliente x) throws SQLException {
        ejecutar(c, "INSERT INTO clientes(id_cliente, nombre, apellido, dni, direccion, telefono, correo, categoria) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                x.getIdCliente(), x.getNombre(), x.getApellido(), x.getDni(), x.getDireccion(), x.getTelefono(),
                x.getCorreoElectronico(), x.getCategoria());
    }

    /** UPDATE de los datos de un cliente. */
    @Override
    public void actualizar(Connection c, Cliente x) throws SQLException {
        ejecutar(c, "UPDATE clientes SET nombre = ?, apellido = ?, dni = ?, direccion = ?, telefono = ?, correo = ?, "
                + "categoria = ? WHERE id_cliente = ?",
                x.getNombre(), x.getApellido(), x.getDni(), x.getDireccion(), x.getTelefono(),
                x.getCorreoElectronico(), x.getCategoria(), x.getIdCliente());
    }

    /** Clave primaria del cliente. */
    @Override
    public String id(Cliente x) {
        return x.getIdCliente();
    }
}
