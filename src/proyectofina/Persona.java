package proyectofina;

/**
 * Clase padre de Usuario y Cliente (diagrama de clases S09).
 *
 * @author Rodrigo
 */
public abstract class Persona implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private String apellido;
    private String dni;

    /** Datos de identidad comunes a usuarios y clientes. */
    public Persona(String nombre, String apellido, String dni) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
    }

    /** Texto de la persona; las clases hijas lo sobrescriben (polimorfismo). */
    public String mostrarPersona() {
        return nombre + " " + apellido + " (DNI " + dni + ")";
    }

    // ---------- Getters y setters ----------
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener 8 digitos.");
        }
        this.dni = dni;
    }
}
