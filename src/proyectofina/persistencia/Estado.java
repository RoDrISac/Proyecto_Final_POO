package proyectofina.persistencia;

import java.io.*;
import java.util.*;
import proyectofina.*;

/** Agregado persistente. Se modifica sobre una copia antes de confirmar en disco o en la base de datos. */
public class Estado implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<Rol> roles = new ArrayList<>();
    public List<Usuario> usuarios = new ArrayList<>();
    public List<Cliente> clientes = new ArrayList<>();
    public List<Inventario> productos = new ArrayList<>();
    public List<Venta> ventas = new ArrayList<>();
    public List<Movimiento> movimientos = new ArrayList<>();
    // Agregados en la Evaluacion Final (S06, S07 y S08)
    public List<MateriaPrima> materias = new ArrayList<>();
    public List<Receta> recetas = new ArrayList<>();
    public List<OperacionProduccion> operaciones = new ArrayList<>();
    public List<Pedido> pedidos = new ArrayList<>();
    public List<RegistroAuditoria> auditoria = new ArrayList<>();
    public List<MovimientoMateria> movimientosMateria = new ArrayList<>();

    /** Meses con la contabilidad cerrada, con formato AAAA-MM (S08 N.° 22 y 24). */
    public Set<String> mesesCerrados = new TreeSet<>();

    public record Movimiento(Date fecha, String usuario, String producto, String tipo, int cantidad, int saldo) implements Serializable {
    }

    /** Movimiento de materia prima (kardex del taller). */
    public record MovimientoMateria(Date fecha, String usuario, String materia, String tipo, double cantidad,
            double saldo, String referencia) implements Serializable {
    }

    /** Un archivo del modo local guardado sin alguna lista la recibe vacía (evita valores null). */
    public Estado completar() {
        if (materias == null) {
            materias = new ArrayList<>();
        }
        if (recetas == null) {
            recetas = new ArrayList<>();
        }
        if (operaciones == null) {
            operaciones = new ArrayList<>();
        }
        if (pedidos == null) {
            pedidos = new ArrayList<>();
        }
        if (auditoria == null) {
            auditoria = new ArrayList<>();
        }
        if (movimientosMateria == null) {
            movimientosMateria = new ArrayList<>();
        }
        if (mesesCerrados == null) {
            mesesCerrados = new TreeSet<>();
        }
        return this;
    }

    /** Unidades de un producto reservadas por pedidos activos (S08 N.° 14). */
    public int stockComprometido(String idProducto) {
        int total = 0;
        for (Pedido p : pedidos) {
            total += p.cantidadComprometida(idProducto);
        }
        return total;
    }

    /** Stock que se puede vender sin tocar lo reservado para pedidos. */
    public int stockDisponible(Inventario p) {
        return Math.max(0, p.getStock() - stockComprometido(p.getIdProducto()));
    }

    /** Copia profunda por serialización: los servicios trabajan sobre la copia y solo la confirman si el guardado tuvo éxito. */
    public Estado copia() {
        try {
            var bytes = new ByteArrayOutputStream();
            try (var out = new ObjectOutputStream(bytes)) {
                out.writeObject(this);
            }
            try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                return ((Estado) in.readObject()).completar();
            }
        }
        catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("No se pudo copiar el estado", e);
        }
    }
}
