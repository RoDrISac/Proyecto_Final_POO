package proyectofina;
import java.nio.file.*;
import proyectofina.persistencia.*;
import proyectofina.servicios.*;

/** Lee los CSV ficticios de test/fixtures y escribe el nuevo estado solo en un directorio temporal. */
public final class PruebaImportacion {
    public static void main(String[] args) throws Exception {
        Path carpeta=Files.createTempDirectory("colores-importacion-");
        System.setProperty("app.datos",carpeta.toString());
        for(String nombre:java.util.List.of("usuarios.csv","clientes.csv","inventario.csv","ventas.csv"))
            Files.copy(Path.of("test/fixtures",nombre),carpeta.resolve(nombre));
        Path archivo=carpeta.resolve("estado.dat");
        var s=new Sistema(new RepositorioArchivo(archivo));s.ingresar("U001","1234");var e=s.consultar();
        if(e.productos.size()!=2||e.clientes.size()!=3||e.ventas.size()!=3||e.usuarios.size()!=3)throw new AssertionError("Importación inesperada");
        var otro=new Sistema(new RepositorioArchivo(archivo));otro.ingresar("U001","1234");
        if(otro.consultar().ventas.size()!=3)throw new AssertionError("Recuperación inesperada");
        Files.writeString(Path.of("docs/evidencias/importacion.txt"),"PASS | Importación de los CSV ficticios de prueba: 2 productos, 3 clientes, 3 ventas, 3 usuarios.\nPASS | Recuperación del estado nuevo después de reiniciar.\nLos CSV se leyeron; el estado de prueba se escribió en una carpeta temporal.\n");
        System.out.println("PASS | Importación y recuperación de los datos ficticios");
    }
}
