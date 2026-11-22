package proyectofina;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import proyectofina.persistencia.Repositorio;
import proyectofina.persistencia.RepositorioArchivo;
import proyectofina.persistencia.sql.ConexionBD;
import proyectofina.persistencia.sql.RepositorioSQL;
import proyectofina.servicios.Sistema;
import proyectofina.ui.Aplicacion;
import proyectofina.ui.Gui;

/** Punto de entrada del sistema (Java 17 o superior). */
public final class ProyectoFina {
    private static FileChannel canal;
    private static FileLock bloqueo;

    /** Clase utilitaria: no se instancia. */
    private ProyectoFina() {
    }

    /** Aplica el tema visual, evita dos instancias abiertas, elige el repositorio y abre la ventana. */
    public static void main(String[] args) {
        Gui.tema();
        try {
            Path datos = Path.of(GestorArchivos.CARPETA_DATOS);
            Files.createDirectories(datos);
            // Evita abrir dos ventanas que escriban al mismo tiempo
            canal = FileChannel.open(datos.resolve("aplicacion.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            bloqueo = canal.tryLock();
            if (bloqueo == null) {
                throw new IllegalStateException("El sistema ya está abierto. Usa la ventana existente.");
            }
            Sistema sistema = new Sistema(elegirRepositorio(datos));
            SwingUtilities.invokeLater(() -> new Aplicacion(sistema).setVisible(true));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No se pudo iniciar: " + e.getMessage(), "Colores y Creaciones", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Usa MySQL de XAMPP (JDBC). Si no hay conexión, explica el motivo y ofrece el modo archivo local. */
    private static Repositorio elegirRepositorio(Path datos) throws Exception {
        Path archivoLocal = datos.resolve("estado-local.dat");
        try {
            ConexionBD bd = ConexionBD.desdeArchivo(ConexionBD.CONFIG);
            bd.probar();
            return new RepositorioSQL(bd);
        } catch (Exception e) {
            int respuesta = JOptionPane.showConfirmDialog(null,
                    "No se pudo conectar a MySQL:\n" + e.getMessage()
                    + "\n\nRevisa que MySQL esté iniciado en el XAMPP Control Panel (botón Start)\n"
                    + "y los datos de config/bd.properties (ver docs/MANUAL_INSTALACION.md).\n\n"
                    + "¿Abrir en modo archivo local (sin base de datos)?",
                    "Colores y Creaciones - Base de datos", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) {
                throw new IllegalStateException("Sin conexión a la base de datos.");
            }
            return new RepositorioArchivo(archivoLocal);
        }
    }
}
