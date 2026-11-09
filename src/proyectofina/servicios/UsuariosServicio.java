package proyectofina.servicios;
import java.io.IOException;
import proyectofina.*;
public final class UsuariosServicio {
    private final Sistema s;
    UsuariosServicio(Sistema s) {
        this.s=s;
    }
    public void registrar(String id,String nombre,String apellido,String dni,String clave,String rol) throws IOException {
        String codigo=Sistema.codigo(id);
        Sistema.texto(nombre,"Nombre");
        Sistema.texto(apellido,"Apellido");
        if(!dni.matches("\\d{8}")) throw new IllegalArgumentException("DNI: 8 dígitos.");
        if(clave.length()<4) throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres.");
        s.cambiar("USUARIOS",e-> {
            if(e.usuarios.stream().anyMatch(u->u.getIdUsuario().equalsIgnoreCase(codigo)||u.getDni().equals(dni))) throw new IllegalArgumentException("El código o DNI ya existe.");              Rol r=e.roles.stream().filter(x->x.getNombreRol().equals(rol)).findFirst().orElseThrow();              e.usuarios.add(new Usuario(codigo,nombre.trim(),apellido.trim(),dni,r,clave));
        }
        );
    }
    public void cambiarRol(String id,String rol) throws IOException {
        if(s.usuario().getIdUsuario().equals(id)) throw new IllegalArgumentException("No puedes cambiar tu propio rol.");
        s.cambiar("USUARIOS",e-> {
            var u=e.usuarios.stream().filter(x->x.getIdUsuario().equals(id)).findFirst().orElseThrow();              var r=e.roles.stream().filter(x->x.getNombreRol().equals(rol)).findFirst().orElseThrow(); u.cambiarRol(r);
        }
        );
    }
}
