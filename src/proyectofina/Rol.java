/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectofina;

import java.util.List;

/**
 * Rol de usuario con sus permisos. La contraseña del rol se pide para asignar ese rol a un usuario.
 *
 * @author Alvaro
 */
public class Rol {
    private String nombreRol;
    private List<String> permiso;
    private String contraseñaRol;

    public Rol(String nombreRol, List<String> permiso, String contraseñaRol) {
        this.nombreRol = nombreRol;
        this.permiso = permiso;
        this.contraseñaRol = contraseñaRol;
    }

    public boolean tenerPermiso(String permiso) {
        return this.permiso != null && this.permiso.contains(permiso);
    }

    public boolean validarContraseñaRol(String clave) {
        return contraseñaRol != null && contraseñaRol.equals(clave);
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public List<String> getPermiso() {
        return permiso;
    }
}
