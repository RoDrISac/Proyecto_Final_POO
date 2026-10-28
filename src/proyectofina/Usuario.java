/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    public Usuario(String idUsuario, String nombre, String apellido, String dni, Rol rol, String contraseña) {
        super(nombre, apellido, dni);
        this.idUsuario = idUsuario;
        this.rol = rol;
        this.contraseña = contraseña;
    }

    public boolean validarAcceso(String contraseña) {
        return this.contraseña != null && this.contraseña.equals(contraseña);
    }

    public void cambiarRol(Rol nuevoRol) {
        this.rol = nuevoRol;
    }

    @Override
    public String mostrarPersona() {
        return idUsuario + " - " + super.mostrarPersona() + " | Rol: " + rol.getNombreRol();
    }

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
