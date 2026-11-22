package proyectofina;

import java.util.ArrayList;
import java.util.List;

/**
 * Cliente del negocio (hereda de Persona).
 *
 * @author Alvaro
 */
public class Cliente extends Persona {
    private String idCliente;
    private String direccion;
    private String telefono;
    private String correoElectronico;
    private String categoria = "Público general";
    private List<Venta> historialVentas = new ArrayList<>();

    /** Tipos de publico para segmentar campanas (problema identificado en S06). */
    public static final String[] CATEGORIAS = {"Público general", "Coleccionista", "Decorador de interiores", "Turista"};

    /** Cliente generico para boletas sin datos del comprador (S08 N.° 41). */
    public static final String ID_VARIOS = "C000";

    /** Crea un cliente con la categoría por defecto (público general). */
    public Cliente(String idCliente, String nombre, String apellido, String dni,
            String direccion, String telefono, String correoElectronico) {
        super(nombre, apellido, dni);
        this.idCliente = idCliente;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
    }

    /** Crea un cliente indicando su categoría para campañas. */
    public Cliente(String idCliente, String nombre, String apellido, String dni,
            String direccion, String telefono, String correoElectronico, String categoria) {
        this(idCliente, nombre, apellido, dni, direccion, telefono, correoElectronico);
        setCategoria(categoria);
    }

    /** Ventas realizadas por este cliente (se completa al cargar los datos). */
    public List<Venta> obtenerHistorialVentas() {
        return historialVentas;
    }
    // Valida los datos antes de registrar al cliente; si algo esta mal lanza una excepcion
    public void registrarCliente() {
        if (idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("El código del cliente es obligatorio.");
        }
        if (getNombre().isBlank() || getApellido().isBlank()) {
            throw new IllegalArgumentException("El nombre y el apellido son obligatorios.");
        }
        if (!getDni().matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener 8 dígitos.");
        }
        if (!telefono.isBlank() && !telefono.matches("\\d{6,9}")) {
            throw new IllegalArgumentException("El teléfono solo debe tener numeros (6 a 9 dígitos).");
        }
        if (!correoElectronico.isBlank() && !correoElectronico.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
    }

    /** Sobrescribe el método de Persona agregando código, categoría y contacto (polimorfismo). */
    @Override
    public String mostrarPersona() {
        return idCliente + " - " + super.mostrarPersona() + " | " + getCategoria() + " | Tel: " + telefono + " | " + correoElectronico;
    }

    // ---------- Getters y setters ----------
    public String getCategoria() {
        return categoria == null ? CATEGORIAS[0] : categoria;
    }

    public void setCategoria(String categoria) {
        if (!java.util.List.of(CATEGORIAS).contains(categoria)) {
            throw new IllegalArgumentException("Categoría de cliente no válida.");
        }
        this.categoria = categoria;
    }

    /** Indica si es el cliente genérico usado en boletas sin datos del comprador. */
    public boolean esClienteVarios() {
        return ID_VARIOS.equals(idCliente);
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }
}
