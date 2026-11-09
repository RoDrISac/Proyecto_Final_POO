package proyectofina;
import java.nio.file.*;
import java.nio.channels.*;
import javax.swing.*;
import proyectofina.persistencia.*;
import proyectofina.servicios.*;
import proyectofina.ui.*;
/** Punto de entrada compatible con Java 17 o superior. */
public final class ProyectoFina {
    private static FileChannel canal;
    private static FileLock bloqueo;
    public static void main(String[] args) {
        Gui.tema();
        try {
            Path datos=Path.of(GestorArchivos.CARPETA_DATOS);
            Files.createDirectories(datos);
            canal=FileChannel.open(datos.resolve("aplicacion.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);
            bloqueo=canal.tryLock();
            if(bloqueo==null)throw new IllegalStateException("El proyecto ya está abierto. Usa la ventana existente.");
            Sistema s=new Sistema(new RepositorioArchivo(datos.resolve("estado-t3.dat")));
            SwingUtilities.invokeLater(()->new Aplicacion(s).setVisible(true));
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(null,"No se pudo iniciar: "+e.getMessage(),"Colores y Creaciones",JOptionPane.ERROR_MESSAGE);
        }
    }
}
