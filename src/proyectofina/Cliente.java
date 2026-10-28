/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
    private List<Venta> historialVentas = new ArrayList<>();

    public Cliente(String idCliente, String nombre, String apellido, String dni,
                   String direccion, String telefono, String correoElectronico) {
        super(nombre, apellido, dni);
        this.idCliente = idCliente;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
    }

    public List<Venta> obtenerHistorialVentas() {
        return historialVentas;
    }

    // Valida los datos antes de registrar al cliente; si algo esta mal lanza una excepcion
    public void registrarCliente() {
        if (idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("El codigo del cliente es obligatorio.");
        }
        if (getNombre().isBlank() || getApellido().isBlank()) {
            throw new IllegalArgumentException("El nombre y el apellido son obligatorios.");
        }
        if (!getDni().matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener 8 digitos.");
        }
        if (!telefono.isBlank() && !telefono.matches("\\d{6,9}")) {
            throw new IllegalArgumentException("El telefono solo debe tener numeros (6 a 9 digitos).");
        }
        if (!correoElectronico.isBlank() && !correoElectronico.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("El correo electronico no es valido.");
        }
    }

    @Override
    public String mostrarPersona() {
        return idCliente + " - " + super.mostrarPersona() + " | Tel: " + telefono + " | " + correoElectronico;
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
