package proyectofina.persistencia;
import java.io.*;
import java.util.*;
import proyectofina.*;
/** Agregado persistente. Se modifica sobre una copia antes de confirmar en disco. */
public class Estado implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<Rol> roles = new ArrayList<>();
    public List<Usuario> usuarios = new ArrayList<>();
    public List<Cliente> clientes = new ArrayList<>();
    public List<Inventario> productos = new ArrayList<>();
    public List<Venta> ventas = new ArrayList<>();
    public List<Movimiento> movimientos = new ArrayList<>();
    public record Movimiento(Date fecha, String usuario, String producto, String tipo, int cantidad, int saldo) implements Serializable {
    }
    public Estado copia() {
        try {
            var bytes = new ByteArrayOutputStream();
            try (var out = new ObjectOutputStream(bytes)) {
                out.writeObject(this);
            }
            try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                return (Estado) in.readObject();
            }
        }
        catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("No se pudo copiar el estado", e);
        }
    }
}
