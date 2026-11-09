package proyectofina.persistencia;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import proyectofina.*;
/** Un único reemplazo atómico evita ventas guardadas sin su descuento de stock. */
public final class RepositorioArchivo implements Repositorio {
    private final Path archivo;
    public RepositorioArchivo(Path archivo) {
        this.archivo = archivo.toAbsolutePath();
    }
    public Estado cargar() throws IOException {
        if (!Files.exists(archivo)) return inicial();
        try (var in = new ObjectInputStream(Files.newInputStream(archivo))) {
            in.setObjectInputFilter(ObjectInputFilter.Config.createFilter("maxdepth=40;maxrefs=200000;maxbytes=50000000;proyectofina.**;java.base/*;!*"));
            return (Estado) in.readObject();
        }
        catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("Archivo de datos incompatible", e);
        }
    }
    public void guardar(Estado estado) throws IOException {
        Files.createDirectories(archivo.getParent());
        Path temporal = Files.createTempFile(archivo.getParent(), "estado-", ".tmp");
        try {
            try (var out = new ObjectOutputStream(Files.newOutputStream(temporal))) {
                out.writeObject(estado);
            }
            // No se hace fallback no atómico: si no es posible, se conserva el estado anterior.
            Files.move(temporal, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
        finally {
            Files.deleteIfExists(temporal);
        }
    }
    private Estado inicial() throws IOException {
        Estado e = new Estado();
        e.roles.add(new Rol("Administrador", List.of("INVENTARIO","VENTAS","REPORTES","USUARIOS"), "ADM-2026"));
        e.roles.add(new Rol("Vendedor", List.of("VENTAS"), "VEN-2026"));
        e.roles.add(new Rol("Supervisor de almacen", List.of("INVENTARIO","REPORTES"), "SUP-2026"));
        e.usuarios = GestorArchivos.cargarUsuarios(e.roles);
        e.clientes = GestorArchivos.cargarClientes();
        e.productos = GestorArchivos.cargarInventario();
        e.ventas = GestorArchivos.cargarVentas(e.clientes,e.usuarios,e.productos);
        if (e.usuarios.isEmpty()) {
            e.usuarios.add(new Usuario("U001","Admin","Principal","00000001",e.roles.get(0),"1234"));
            e.usuarios.add(new Usuario("U002","Vendedor","Tienda","00000002",e.roles.get(1),"1234"));
            e.usuarios.add(new Usuario("U003","Supervisor","Almacen","00000003",e.roles.get(2),"1234"));
        }
        guardar(e);
        return e;
    }
}
