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
    public Sistema(Repositorio repositorio) throws IOException {
        this.repositorio = repositorio;
        estado = repositorio.cargar();
        inventario = new InventarioServicio(this);
        clientes = new ClientesServicio(this);
        ventas = new VentasServicio(this);
        usuarios = new UsuariosServicio(this);
    }
    public void ingresar(String codigo, String clave) {
        if (fallos >= 3) throw new IllegalArgumentException("Se agotaron los 3 intentos. Cierra y vuelve a abrir la aplicación.");
        var usuario = estado.usuarios.stream().filter(u -> u.getIdUsuario().equalsIgnoreCase(codigo.trim()) && u.validarAcceso(clave)).findFirst();
        if (usuario.isEmpty()) {
            fallos++;
            throw new IllegalArgumentException("Código o contraseña incorrectos. Intentos restantes: " + (3-fallos));
        }
        usuarioId = usuario.get().getIdUsuario();
        fallos = 0;
    }
    public void salir() {
        usuarioId = null;
    }
    public Usuario usuario() {
        if (usuarioId == null) throw new IllegalStateException("Inicia sesión para continuar.");
        return estado.copia().usuarios.stream().filter(u -> u.getIdUsuario().equals(usuarioId)).findFirst().orElseThrow();
    }
    public boolean permite(String permiso) {
        return usuario().getRol().tenerPermiso(permiso);
    }
    public void exigir(String permiso) {
        if (!permite(permiso)) throw new IllegalArgumentException("Tu rol no tiene permiso para esta operación.");
    }
    public Estado consultar() {
        usuario();
        return estado.copia();
    }
    void cambiar(String permiso, Consumer<Estado> accion) throws IOException {
        exigir(permiso);
        Estado siguiente = estado.copia();
        accion.accept(siguiente);
        repositorio.guardar(siguiente);
        estado = siguiente;
    }
    static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " es obligatorio.");
        if (valor.length() > 120) throw new IllegalArgumentException(campo + ": máximo 120 caracteres.");
        return valor.trim();
    }
    static String codigo(String valor) {
        valor = texto(valor,"Código").toUpperCase(java.util.Locale.ROOT);
        if (!valor.matches("[A-Z0-9_-]{1,24}")) throw new IllegalArgumentException("Código: usa hasta 24 letras, números, guiones o guion bajo.");
        return valor;
    }
    static void dinero(double valor) {
        if (!Double.isFinite(valor) || valor < 0 || valor > 1000000) throw new IllegalArgumentException("El importe debe estar entre 0 y 1 000 000.");
        if (java.math.BigDecimal.valueOf(valor).stripTrailingZeros().scale() > 2) throw new IllegalArgumentException("Usa como máximo dos decimales.");
    }
}
