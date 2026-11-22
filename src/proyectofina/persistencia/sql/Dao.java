package proyectofina.persistencia.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Contrato CRUD generico para una tabla. Cada entidad tiene su propio DAO
 * (polimorfismo): RepositorioSQL trata a todos por esta interfaz.
 *
 * @param <T> entidad del dominio
 */
public interface Dao<T> {

    /** SELECT de todas las filas. */
    List<T> listar(Connection c) throws SQLException;

    /** SELECT por clave primaria. */
    Optional<T> buscar(Connection c, String id) throws SQLException;

    /** INSERT. */
    void insertar(Connection c, T entidad) throws SQLException;

    /** UPDATE. */
    void actualizar(Connection c, T entidad) throws SQLException;

    /** DELETE por clave primaria. */
    void eliminar(Connection c, String id) throws SQLException;

    /** Clave primaria de la entidad. */
    String id(T entidad);

    /** Claves que existen hoy en la tabla. */
    Set<String> ids(Connection c) throws SQLException;

    /**
     * Deja la tabla igual a la lista recibida: inserta los nuevos, actualiza
     * los existentes y elimina los que ya no estan. Se ejecuta dentro de la
     * transaccion abierta por RepositorioSQL.
     */
    default void sincronizar(Connection c, List<T> entidades) throws SQLException {
        Set<String> enTabla = ids(c);
        Set<String> enMemoria = new HashSet<>();
        for (T e : entidades) {
            String clave = id(e);
            enMemoria.add(clave);
            if (enTabla.contains(clave)) {
                actualizar(c, e);
            } else {
                insertar(c, e);
            }
        }
        List<String> sobran = new ArrayList<>(enTabla);
        sobran.removeAll(enMemoria);
        for (String clave : sobran) {
            eliminar(c, clave);
        }
    }
}
