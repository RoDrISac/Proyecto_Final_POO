package proyectofina.servicios;

import java.io.IOException;
import proyectofina.*;
import proyectofina.persistencia.Estado;

/** Registro de clientes con categoria para segmentar campanas (S06, S08). */
public final class ClientesServicio {
    private final Sistema s;

    ClientesServicio(Sistema s) {
        this.s = s;
    }

    /** Registra un cliente con la categoría por defecto. */
    public void registrar(String codigo, String nombre, String apellido, String dni, String direccion,
            String telefono, String correo) throws IOException {
        registrar(codigo, nombre, apellido, dni, direccion, telefono, correo, Cliente.CATEGORIAS[0]);
    }

    /** Registra un cliente con su categoría; no permite código ni DNI repetidos. */
    public void registrar(String codigo, String nombre, String apellido, String dni, String direccion, String telefono,
            String correo, String categoria) throws IOException {
        Cliente c = validar(codigo, nombre, apellido, dni, direccion, telefono, correo, categoria);
        if (c.esClienteVarios()) {
            throw new IllegalArgumentException("El código " + Cliente.ID_VARIOS + " está reservado para clientes varios.");
        }
        s.cambiar("VENTAS", "REGISTRAR_CLIENTE", c.getIdCliente() + " " + c.getNombreCompleto(), e -> {
            if (e.clientes.stream().anyMatch(x -> x.getIdCliente().equalsIgnoreCase(c.getIdCliente()) || x.getDni().equals(c.getDni()))) {
                throw new IllegalArgumentException("El código o DNI del cliente ya existe.");
            }
            e.clientes.add(c);
        });
    }

    /** Editar datos de contacto y categoria; el codigo no cambia. */
    public void editar(String codigo, String nombre, String apellido, String dni, String direccion, String telefono,
            String correo, String categoria) throws IOException {
        Cliente nuevo = validar(codigo, nombre, apellido, dni, direccion, telefono, correo, categoria);
        s.cambiar("VENTAS", "EDITAR_CLIENTE", nuevo.getIdCliente(), e -> {
            Cliente c = buscar(e, nuevo.getIdCliente());
            if (e.clientes.stream().anyMatch(x -> x != c && x.getDni().equals(nuevo.getDni()))) {
                throw new IllegalArgumentException("Otro cliente ya tiene el DNI " + nuevo.getDni() + ".");
            }
            c.setNombre(nuevo.getNombre());
            c.setApellido(nuevo.getApellido());
            c.setDni(nuevo.getDni());
            c.setDireccion(nuevo.getDireccion());
            c.setTelefono(nuevo.getTelefono());
            c.setCorreoElectronico(nuevo.getCorreoElectronico());
            c.setCategoria(nuevo.getCategoria());
        });
    }

    /** Solo se elimina un cliente sin compras ni pedidos. */
    public void eliminar(String codigo) throws IOException {
        s.cambiar("VENTAS", "ELIMINAR_CLIENTE", codigo, e -> {
            Cliente c = buscar(e, codigo);
            boolean tieneMovimientos = e.ventas.stream().anyMatch(v -> v.getCliente().getIdCliente().equals(codigo))
                    || e.pedidos.stream().anyMatch(p -> p.getIdCliente().equals(codigo));
            if (tieneMovimientos || c.esClienteVarios()) {
                throw new IllegalArgumentException("El cliente tiene compras o pedidos registrados; no se puede eliminar.");
            }
            e.clientes.remove(c);
        });
    }

    /** Crea el cliente y aplica sus validaciones (DNI, teléfono, correo). */
    private static Cliente validar(String codigo, String nombre, String apellido, String dni, String direccion,
            String telefono, String correo, String categoria) {
        Cliente c = new Cliente(Sistema.codigo(codigo), Sistema.texto(nombre, "Nombre"), Sistema.texto(apellido, "Apellido"),
                dni.trim(), direccion.trim(), telefono.trim(), correo.trim(), categoria);
        c.registrarCliente();
        return c;
    }

    /** Busca un cliente por código o informa que no existe. */
    private static Cliente buscar(Estado e, String codigo) {
        return e.clientes.stream().filter(x -> x.getIdCliente().equals(codigo)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + codigo));
    }
}
