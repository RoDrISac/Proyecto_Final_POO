package proyectofina.persistencia.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import proyectofina.Cliente;
import proyectofina.Venta;
import proyectofina.persistencia.Estado;
import proyectofina.persistencia.Repositorio;

/**
 * Implementación del contrato Repositorio sobre MySQL (XAMPP) con JDBC.
 * Los servicios y las ventanas solo conocen la interfaz Repositorio, por eso no dependen
 * de dónde se guardan los datos (polimorfismo).
 */
public final class RepositorioSQL implements Repositorio {
    private final ConexionBD conexion;

    /** @param conexion fábrica de conexiones JDBC */
    public RepositorioSQL(ConexionBD conexion) {
        this.conexion = conexion;
    }

    /** Lee todas las tablas; si la base está vacía, primero carga los datos de demostración. */
    @Override
    public Estado cargar() throws IOException {
        try (Connection c = conexion.abrir()) {
            if (vacia(c)) {
                // Primera ejecución: datos de demostración del taller de cerámica
                escribir(c, proyectofina.persistencia.DatosDemo.crear());
            }
            return leer(c);
        } catch (SQLException e) {
            throw new IOException("No se pudo leer la base de datos: " + e.getMessage(), e);
        }
    }

    /** Abre una conexión y escribe el estado completo en una transacción. */
    @Override
    public void guardar(Estado estado) throws IOException {
        try (Connection c = conexion.abrir()) {
            escribir(c, estado);
        } catch (SQLException e) {
            throw new IOException("No se pudo guardar en la base de datos: " + e.getMessage(), e);
        }
    }

    /** Arma el Estado con cada DAO y enlaza el historial de compras de los clientes. */
    private Estado leer(Connection c) throws SQLException {
        Estado e = new Estado();
        e.roles = new RolDao().listar(c);
        e.usuarios = new UsuarioDao(e.roles).listar(c);
        e.clientes = new ClienteDao().listar(c);
        e.productos = new ProductoDao().listar(c);
        e.ventas = new VentaDao(e.roles).listar(c);
        e.movimientos = new MovimientoDao().listar(c);
        e.materias = new MateriaPrimaDao().listar(c);
        e.recetas = new RecetaDao().listar(c);
        e.pedidos = new PedidoDao().listar(c);
        e.operaciones = new OperacionProduccionDao().listar(c);
        HistorialDao historial = new HistorialDao();
        e.auditoria = historial.listarAuditoria(c);
        e.movimientosMateria = historial.listarMovimientosMateria(c);
        e.mesesCerrados = historial.listarCierres(c);
        // Historial de compras de cada cliente (asociacion Cliente -> Venta)
        for (Venta v : e.ventas) {
            for (Cliente cl : e.clientes) {
                if (cl.getIdCliente().equals(v.getCliente().getIdCliente())) {
                    cl.obtenerHistorialVentas().add(v);
                }
            }
        }
        return e;
    }

    /**
     * Guarda todo dentro de UNA transaccion: si algo falla (por ejemplo la venta
     * se registra pero el stock no), se hace rollback y la BD queda como estaba.
     */
    private void escribir(Connection c, Estado e) throws SQLException {
        boolean auto = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            new RolDao().sincronizar(c, e.roles);
            new UsuarioDao(e.roles).sincronizar(c, e.usuarios);
            new ClienteDao().sincronizar(c, e.clientes);
            new ProductoDao().sincronizar(c, e.productos);
            new MateriaPrimaDao().sincronizar(c, e.materias);
            new RecetaDao().sincronizar(c, e.recetas);
            new VentaDao(e.roles).sincronizar(c, e.ventas);
            new PedidoDao().sincronizar(c, e.pedidos);
            new OperacionProduccionDao().agregarNuevas(c, e.operaciones);
            new MovimientoDao().agregarNuevos(c, e.movimientos);
            HistorialDao historial = new HistorialDao();
            historial.agregarMovimientosMateria(c, e.movimientosMateria);
            historial.agregarAuditoria(c, e.auditoria);
            historial.agregarCierres(c, e.mesesCerrados);
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            c.rollback();
            throw ex;
        } finally {
            c.setAutoCommit(auto);
        }
    }

    /** Indica si la base todavía no tiene datos (tabla roles vacía). */
    private static boolean vacia(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM roles");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) == 0;
        }
    }
}
