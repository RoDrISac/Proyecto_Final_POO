package proyectofina;

import java.util.Date;

/**
 * Egreso por falla de produccion (S08 N.° 4): una pieza se rompe o se quema en
 * el horno y se descuentan las materias primas que se usaron.
 */
public class FallaProduccion extends OperacionProduccion {
    private static final long serialVersionUID = 1L;
    private final String motivo;

    /** Registra una falla; el motivo es obligatorio. */
    public FallaProduccion(String idOperacion, Date fecha, String usuario, String motivo) {
        super(idOperacion, fecha, usuario);
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Describe el motivo de la falla.");
        }
        this.motivo = motivo.trim();
    }

    // ---------- Getters y setters ----------
    @Override
    public String getTipo() {
        return "FALLA";
    }

    /** En una falla todo el material consumido es perdida. */
    @Override
    public double calcularCostoTotal() {
        return calcularCostoMateriales();
    }

    /** Texto para el historial del taller. */
    @Override
    public String describir() {
        return String.format("%s | FALLA: %s | pérdida S/ %.2f", getIdOperacion(), motivo, calcularCostoTotal());
    }

    public String getMotivo() {
        return motivo;
    }
}
