package proyectofina;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase padre abstracta de las operaciones del taller que consumen materia prima:
 * Produccion (ingreso de productos terminados) y FallaProduccion (egreso por falla).
 * Cada operacion se compone de las materias primas que consumio.
 */
public abstract class OperacionProduccion implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String idOperacion;
    private final Date fecha;
    private final String usuario;
    private final List<Consumo> consumos = new ArrayList<>();

    /** Materia prima consumida, con el costo que tenia en ese momento. */
    public record Consumo(String idMateria, String nombre, String unidad, double cantidad, double costoUnitario)
            implements java.io.Serializable {
        public double subtotal() {
            return Math.round(cantidad * costoUnitario * 100.0) / 100.0;
        }
    }

    /** Datos comunes de toda operación del taller. */
    protected OperacionProduccion(String idOperacion, Date fecha, String usuario) {
        this.idOperacion = idOperacion;
        this.fecha = fecha;
        this.usuario = usuario;
    }

    /** Registra la materia prima usada con su costo actual. */
    public void agregarConsumo(MateriaPrima m, double cantidad) {
        consumos.add(new Consumo(m.getIdMateria(), m.getNombre(), m.getUnidad(), cantidad, m.getCostoUnitario()));
    }

    /** Usado al leer desde la base de datos. */
    public void agregarConsumo(Consumo c) {
        consumos.add(c);
    }

    /** Suma el costo de toda la materia prima consumida. */
    public double calcularCostoMateriales() {
        double total = 0;
        for (Consumo c : consumos) {
            total += c.subtotal();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    // ---------- Getters y setters ----------
    /** PRODUCCION o FALLA: cada subclase responde distinto (polimorfismo). */
    public abstract String getTipo();

    /** Costo total de la operacion segun su tipo. */
    public abstract double calcularCostoTotal();

    /** Texto para el historial del taller. */
    public abstract String describir();

    public String getIdOperacion() {
        return idOperacion;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public List<Consumo> getConsumos() {
        return List.copyOf(consumos);
    }
}
