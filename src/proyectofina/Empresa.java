package proyectofina;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Datos de la empresa para el encabezado del comprobante (S08 N.° 17).
 * Se leen de config/empresa.properties para poder cambiarlos sin programar.
 */
public final class Empresa {
    public static final Path CONFIG = Path.of("config", "empresa.properties");
    private final String razonSocial;
    private final String ruc;
    private final String direccion;
    private final String telefono;
    private final String lema;

    /** Toma los datos del archivo de configuración o usa valores por defecto. */
    private Empresa(Properties p) {
        razonSocial = p.getProperty("razonSocial", "COLORES Y CREACIONES").trim();
        ruc = p.getProperty("ruc", "").trim();
        direccion = p.getProperty("direccion", "Cajamarca - Perú").trim();
        telefono = p.getProperty("telefono", "").trim();
        lema = p.getProperty("lema", "Cerámica artesanal inspirada en la cultura Cajamarca").trim();
    }

    /** Lee config/empresa.properties (UTF-8); si no existe, usa los datos por defecto. */
    public static Empresa cargar() {
        Properties p = new Properties();
        if (Files.exists(CONFIG)) {
            try (InputStream in = Files.newInputStream(CONFIG)) {
                p.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            } catch (IOException e) {
                // Si no se puede leer se usan los datos por defecto.
            }
        }
        return new Empresa(p);
    }

    // ---------- Getters y setters ----------
    public String getRazonSocial() {
        return razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getLema() {
        return lema;
    }
}
