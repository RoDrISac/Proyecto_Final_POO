package proyectofina;

/**
 * Usuario del sistema (hereda de Persona). Ingresa con su codigo unico y contraseña.
 *
 * @author Alvaro
 */
public class Usuario extends Persona {
    private String idUsuario;
    private Rol rol;
    private String contraseña;

    /** Usuario del sistema con su rol. */
    public Usuario(String idUsuario, String nombre, String apellido, String dni, Rol rol, String contraseña) {
        super(nombre, apellido, dni);
        this.idUsuario = idUsuario;
        this.rol = rol;
        this.contraseña = contraseña;
    }

    /** Compara la contraseña ingresada al iniciar sesión. */
    public boolean validarAcceso(String contraseña) {
        return this.contraseña != null && this.contraseña.equals(contraseña);
    }

    /** Asigna otro rol (la contraseña del rol se valida en UsuariosServicio). */
    public void cambiarRol(Rol nuevoRol) {
        this.rol = nuevoRol;
    }

    /** Sobrescribe Persona.mostrarPersona() agregando código y rol (polimorfismo). */
    @Override
    public String mostrarPersona() {
        return idUsuario + " - " + super.mostrarPersona() + " | Rol: " + rol.getNombreRol();
    }

    // ---------- Getters y setters ----------
    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Rol getRol() {
        return rol;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }
}
