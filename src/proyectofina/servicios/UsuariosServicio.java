package proyectofina.servicios;

import java.io.IOException;
import proyectofina.*;
import proyectofina.persistencia.Estado;

/** Gestion de usuarios y roles (S08 N.° 30; S10: se pide la contrasena del rol). */
public final class UsuariosServicio {
    private final Sistema s;

    UsuariosServicio(Sistema s) {
        this.s = s;
    }

    /** Registra un usuario; exige la contraseña del rol elegido. */
    public void registrar(String id, String nombre, String apellido, String dni, String clave, String rol,
            String claveRol) throws IOException {
        String codigo = Sistema.codigo(id);
        Sistema.texto(nombre, "Nombre");
        Sistema.texto(apellido, "Apellido");
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("DNI: 8 dígitos.");
        }
        if (clave == null || clave.length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres.");
        }
        s.cambiar("USUARIOS", "REGISTRAR_USUARIO", codigo + " con rol " + rol, e -> {
            if (e.usuarios.stream().anyMatch(u -> u.getIdUsuario().equalsIgnoreCase(codigo) || u.getDni().equals(dni))) {
                throw new IllegalArgumentException("El código o DNI ya existe.");
            }
            Rol r = rolValidado(e, rol, claveRol);
            e.usuarios.add(new Usuario(codigo, nombre.trim(), apellido.trim(), dni, r, clave));
        });
    }

    /** Cambia el rol de otro usuario; nadie puede cambiar su propio rol. */
    public void cambiarRol(String id, String rol, String claveRol) throws IOException {
        if (s.usuario().getIdUsuario().equals(id)) {
            throw new IllegalArgumentException("No puedes cambiar tu propio rol.");
        }
        s.cambiar("USUARIOS", "CAMBIAR_ROL", id + " -> " + rol, e -> {
            Usuario u = e.usuarios.stream().filter(x -> x.getIdUsuario().equals(id)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
            u.cambiarRol(rolValidado(e, rol, claveRol));
        });
    }

    /** Para asignar un rol se exige la contrasena de ese rol. */
    private static Rol rolValidado(Estado e, String rol, String claveRol) {
        Rol r = e.roles.stream().filter(x -> x.getNombreRol().equals(rol)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rol no válido."));
        if (!r.validarContraseñaRol(claveRol)) {
            throw new IllegalArgumentException("La contraseña del rol " + rol + " es incorrecta.");
        }
        return r;
    }
}
