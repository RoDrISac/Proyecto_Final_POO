package proyectofina.servicios;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import proyectofina.RegistroAuditoria;

/** Administracion: auditoria (S08 N.° 31) y cierre contable mensual (S08 N.° 22 y 24). */
public final class AdministracionServicio {
    private final Sistema s;

    AdministracionServicio(Sistema s) {
        this.s = s;
    }

    /** Cierra un mes: despues ya no se pueden registrar ventas ni producciones con esa fecha. */
    public void cerrarMes(String periodo) throws IOException {
        YearMonth mes;
        try {
            mes = YearMonth.parse(periodo.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Escribe el mes con formato AAAA-MM, por ejemplo 2026-10.");
        }
        if (mes.isAfter(YearMonth.now())) {
            throw new IllegalArgumentException("No se puede cerrar un mes futuro.");
        }
        String clave = mes.toString();
        s.cambiar("USUARIOS", "CERRAR_MES", clave, e -> {
            if (e.mesesCerrados.contains(clave)) {
                throw new IllegalArgumentException("El mes " + clave + " ya estaba cerrado.");
            }
            e.mesesCerrados.add(clave);
        });
    }

    /** Meses con la contabilidad cerrada (solo administrador). */
    public List<String> mesesCerrados() {
        s.exigir("USUARIOS");
        return List.copyOf(s.consultar().mesesCerrados);
    }

    /** Registros de auditoria del periodo, del mas reciente al mas antiguo. */
    public List<RegistroAuditoria> auditoria(LocalDate desde, LocalDate hasta, String usuario) {
        s.exigir("USUARIOS");
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
        }
        String filtro = usuario == null ? "" : usuario.trim().toUpperCase();
        return s.consultar().auditoria.stream()
                .filter(r -> {
                    LocalDate d = r.fecha().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !d.isBefore(desde) && !d.isAfter(hasta)
                            && (filtro.isEmpty() || String.valueOf(r.usuario()).toUpperCase().contains(filtro));
                })
                .sorted((a, b) -> b.fecha().compareTo(a.fecha()))
                .toList();
    }
}
