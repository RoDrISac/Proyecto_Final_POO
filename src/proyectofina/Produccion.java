package proyectofina;

import java.util.Date;

/**
 * Registro de produccion (S08 N.° 3, 5 y 11): se fabrican piezas de un producto,
 * se descuenta la materia prima de la receta, se hace la revision de calidad
 * (aprobadas / rechazadas) y solo las aprobadas ingresan al inventario.
 */
public class Produccion extends OperacionProduccion {
    private static final long serialVersionUID = 1L;
    private final String idProducto;
    private final String nombreProducto;
    private final int cantidad;
    private final int aprobadas;
    private final String observacionCalidad;
    private final double costoManoObra;

    /** Valida cantidades (aprobadas entre 0 y lo producido) y costo de mano de obra. */
    public Produccion(String idOperacion, Date fecha, String usuario, String idProducto, String nombreProducto,
            int cantidad, int aprobadas, String observacionCalidad, double costoManoObra) {
        super(idOperacion, fecha, usuario);
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad producida debe ser mayor a cero.");
        }
        if (aprobadas < 0 || aprobadas > cantidad) {
            throw new IllegalArgumentException("Las piezas aprobadas deben estar entre 0 y " + cantidad + ".");
        }
        if (!Double.isFinite(costoManoObra) || costoManoObra < 0) {
            throw new IllegalArgumentException("El costo de mano de obra no puede ser negativo.");
        }
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.aprobadas = aprobadas;
        this.observacionCalidad = observacionCalidad == null ? "" : observacionCalidad.trim();
        this.costoManoObra = costoManoObra;
    }

    // ---------- Getters y setters ----------
    @Override
    public String getTipo() {
        return "PRODUCCION";
    }

    /** Costo del lote: materiales + mano de obra. */
    @Override
    public double calcularCostoTotal() {
        return Math.round((calcularCostoMateriales() + costoManoObra) * 100.0) / 100.0;
    }

    /** Costo por pieza buena: el costo de las rechazadas lo absorben las aprobadas. */
    public double calcularCostoUnitario() {
        return aprobadas == 0 ? 0 : Math.round(calcularCostoTotal() / aprobadas * 100.0) / 100.0;
    }

    public int getRechazadas() {
        return cantidad - aprobadas;
    }

    /** Texto para el historial del taller con rechazos y costos. */
    @Override
    public String describir() {
        return String.format("%s | %s x%d | aprobadas %d, rechazadas %d | costo lote S/ %.2f | S/ %.2f por pieza%s",
                getIdOperacion(), nombreProducto, cantidad, aprobadas, getRechazadas(), calcularCostoTotal(),
                calcularCostoUnitario(), observacionCalidad.isEmpty() ? "" : " | Calidad: " + observacionCalidad);
    }

    public String getIdProducto() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getAprobadas() {
        return aprobadas;
    }

    public String getObservacionCalidad() {
        return observacionCalidad;
    }

    public double getCostoManoObra() {
        return costoManoObra;
    }
}
