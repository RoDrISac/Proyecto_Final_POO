package proyectofina.persistencia.sql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fabrica de conexiones JDBC. Lee url, usuario y clave de config/bd.properties
 * para no dejar los datos de conexion escritos en el codigo.
 */
public final class ConexionBD {
    public static final Path CONFIG = Path.of("config", "bd.properties");
    private final String url;
    private final String usuario;
    private final String clave;

    /** Guarda los datos de conexión; no abre la conexión todavía. */
    public ConexionBD(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    /** Crea la fabrica a partir del archivo de configuracion. */
    public static ConexionBD desdeArchivo(Path archivo) throws IOException {
        if (!Files.exists(archivo)) {
            throw new IOException("No existe " + archivo.toAbsolutePath() + ". Revisa el manual de instalacion.");
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(archivo)) {
            p.load(in);
        }
        String url = p.getProperty("url", "").trim();
        if (url.isEmpty()) {
            throw new IOException("Falta la propiedad 'url' en " + archivo);
        }
        return new ConexionBD(url, p.getProperty("usuario", "").trim(), p.getProperty("clave", ""));
    }

    /** Abre una conexion nueva; quien la pide la cierra (try-with-resources). */
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    /** Comprueba que el servidor responde; se usa al iniciar la aplicacion. */
    public void probar() throws SQLException {
        try (Connection c = abrir()) {
            if (!c.isValid(5)) {
                throw new SQLException("El servidor no respondio a tiempo.");
            }
        }
    }

    // ---------- Getters y setters ----------
    public String getUrl() {
        return url;
    }
}
