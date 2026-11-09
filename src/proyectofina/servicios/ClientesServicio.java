package proyectofina.servicios;
import java.io.IOException;
import proyectofina.*;
public final class ClientesServicio {
    private final Sistema s;
    ClientesServicio(Sistema s) {
        this.s=s;
    }
    public void registrar(String codigo,String nombre,String apellido,String dni,String direccion,String telefono,String correo) throws IOException {
        Cliente c=new Cliente(Sistema.codigo(codigo),Sistema.texto(nombre,"Nombre"),Sistema.texto(apellido,"Apellido"),dni.trim(),direccion.trim(),telefono.trim(),correo.trim());
        c.registrarCliente();
        s.cambiar("VENTAS", e -> {
            if(e.clientes.stream().anyMatch(x->x.getIdCliente().equalsIgnoreCase(c.getIdCliente())||x.getDni().equals(c.getDni()))) throw new IllegalArgumentException("El código o DNI del cliente ya existe.");              e.clientes.add(c);
        }
        );
    }
}
