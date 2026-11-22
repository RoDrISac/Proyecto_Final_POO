package proyectofina;

import java.util.Date;

/**
 * Auditoria (S08 N.° 31): quien hizo que cambio, con fecha y hora.
 * Es inmutable: un registro de auditoria nunca se modifica.
 */
public record RegistroAuditoria(Date fecha, String usuario, String accion, String detalle) implements java.io.Serializable {
}
