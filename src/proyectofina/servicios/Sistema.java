package proyectofina.servicios;

import java.io.IOException;
import java.util.function.Consumer;
import proyectofina.*;
import proyectofina.persistencia.*;

/** Contexto transaccional y sesión. Las vistas reciben copias, nunca el estado mutable. */
public final class Sistema {
    private final Repositorio repositorio;
    private Estado estado;
    private String usuarioId;
    private int fallos;
    public final InventarioServicio inventario;
    public final ClientesServicio clientes;
    public final VentasServicio ventas;
    public final UsuariosServicio usuarios;
    public final ProduccionServicio produccion;
    public final PedidosServicio pedidos;
    public final AdministracionServicio administracion;

    /** Carga el estado desde el repositorio y crea los servicios que lo comparten. */
    public Sistema(Repositorio repositorio) throws IOException {
        this.repositorio = repositorio;
        estado = repositorio.cargar();
        inventario = new InventarioServicio(this);
        clientes = new ClientesServicio(this);
        ventas = new VentasServicio(this);
        usuarios = new UsuariosServicio(this);
        produccion = new ProduccionServicio(this);
        pedidos = new PedidosServicio(this);
        administracion = new AdministracionServicio(this);
    }

    /** Inicia sesión; después de 3 intentos fallidos bloquea el acceso. */
    public void ingresar(String codigo, String clave) {
        if (fallos >= 3) {
            throw new IllegalArgumentException("Se agotaron los 3 intentos. Cierra y vuelve a abrir la aplicación.");
        }
        var usuario = estado.usuarios.stream()
                .filter(u -> u.getIdUsuario().equalsIgnoreCase(codigo.trim()) && u.validarAcceso(clave))
                .findFirst();
        if (usuario.isEmpty()) {
            fallos++;
            throw new IllegalArgumentException("Código o contraseña incorrectos. Intentos restantes: " + (3 - fallos));
        }
        usuarioId = usuario.get().getIdUsuario();
        fallos = 0;
    }

    /** Cierra la sesión. */
    public void salir() {
        usuarioId = null;
    }

    /** Usuario con sesión activa (copia). */
    public Usuario usuario() {
        if (usuarioId == null) {
            throw new IllegalStateException("Inicia sesión para continuar.");
        }
        return estado.copia().usuarios.stream().filter(u -> u.getIdUsuario().equals(usuarioId)).findFirst().orElseThrow();
    }

    /** Indica si el usuario actual tiene el permiso. */
    public boolean permite(String permiso) {
        return usuario().getRol().tenerPermiso(permiso);
    }

    /** Lanza un error si el usuario actual no tiene el permiso. */
    public void exigir(String permiso) {
        if (!permite(permiso)) {
            throw new IllegalArgumentException("Tu rol no tiene permiso para esta operación.");
        }
    }

    /** Copia del estado para leer; modificarla no cambia el sistema. */
    public Estado consultar() {
        usuario();
        return estado.copia();
    }

    /** Modificación cuya acción de auditoría es el mismo permiso (atajo del método siguiente). */
    void cambiar(String permiso, Consumer<Estado> accion) throws IOException {
        cambiar(permiso, permiso, "", accion);
    }

    /**
     * Toda modificación pasa por aquí: verifica el permiso, trabaja sobre una copia,
     * registra la auditoría (quién, qué, cuándo) y guarda todo junto. Si el guardado
     * falla, el estado anterior se conserva.
     */
    void cambiar(String permiso, String accionAuditoria, String detalle, Consumer<Estado> accion) throws IOException {
        exigir(permiso);
        Estado siguiente = estado.copia();
        accion.accept(siguiente);
        siguiente.auditoria.add(new proyectofina.RegistroAuditoria(new java.util.Date(), usuarioId, accionAuditoria, detalle));
        repositorio.guardar(siguiente);
        estado = siguiente;
    }

    /** El período AAAA-MM de una fecha. */
    static String periodo(java.util.Date fecha) {
        return new java.text.SimpleDateFormat("yyyy-MM").format(fecha);
    }

    /** Validación legal de fechas (S08 N.° 22 y 24): ni futura ni en un mes con contabilidad cerrada. */
    static void validarFechaOperacion(Estado e, java.util.Date fecha) {
        // Se tolera un minuto de diferencia de reloj
        if (fecha.after(new java.util.Date(System.currentTimeMillis() + 60_000))) {
            throw new IllegalArgumentException("No se permiten operaciones con fecha futura.");
        }
        if (e.mesesCerrados.contains(periodo(fecha))) {
            throw new IllegalArgumentException("El mes " + periodo(fecha)
                    + " tiene la contabilidad cerrada; no se pueden registrar operaciones.");
        }
    }

    /** Valida un texto obligatorio de máximo 120 caracteres. */
    static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        if (valor.length() > 120) {
            throw new IllegalArgumentException(campo + ": máximo 120 caracteres.");
        }
        return valor.trim();
    }

    /** Valida y normaliza un código (mayúsculas, letras, números, guiones). */
    static String codigo(String valor) {
        valor = texto(valor, "Código").toUpperCase(java.util.Locale.ROOT);
        if (!valor.matches("[A-Z0-9_-]{1,24}")) {
            throw new IllegalArgumentException("Código: usa hasta 24 letras, números, guiones o guion bajo.");
        }
        return valor;
    }

    /** Valida un importe entre 0 y 1 000 000 con máximo 2 decimales. */
    static void dinero(double valor) {
        if (!Double.isFinite(valor) || valor < 0 || valor > 1_000_000) {
            throw new IllegalArgumentException("El importe debe estar entre 0 y 1 000 000.");
        }
        if (java.math.BigDecimal.valueOf(valor).stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Usa como máximo dos decimales.");
        }
    }
}
