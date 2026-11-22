package proyectofina.persistencia.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import proyectofina.persistencia.Estado.Movimiento;

/**
 * Kardex de inventario. Es un historial: solo se agregan filas, nunca se
 * modifican ni se borran, por eso no implementa el CRUD completo de Dao.
 */
public class MovimientoDao {

    /** SELECT del kardex de productos en orden de registro. */
    public List<Movimiento> listar(Connection c) throws SQLException {
        List<Movimiento> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT fecha, id_usuario, id_producto, tipo, cantidad, saldo FROM movimientos ORDER BY id_movimiento");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Movimiento(new Date(rs.getTimestamp("fecha").getTime()), rs.getString("id_usuario"),
                        rs.getString("id_producto"), rs.getString("tipo"), rs.getInt("cantidad"), rs.getInt("saldo")));
            }
        }
        return lista;
    }

    /** Inserta solo los movimientos que todavia no estan en la tabla. */
    public void agregarNuevos(Connection c, List<Movimiento> movimientos) throws SQLException {
        int enTabla;
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM movimientos");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            enTabla = rs.getInt(1);
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO movimientos(fecha, id_usuario, id_producto, tipo, cantidad, saldo) VALUES (?, ?, ?, ?, ?, ?)")) {
            for (int i = enTabla; i < movimientos.size(); i++) {
                Movimiento m = movimientos.get(i);
                ps.setTimestamp(1, new Timestamp(m.fecha().getTime()));
                ps.setString(2, m.usuario());
                ps.setString(3, m.producto());
                ps.setString(4, m.tipo());
                ps.setInt(5, m.cantidad());
                ps.setInt(6, m.saldo());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
