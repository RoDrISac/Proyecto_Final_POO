package proyectofina;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Properties;
import javax.net.ssl.SSLSocketFactory;

/**
 * Envio de correo con adjunto PDF (S08 N.° 28) usando el protocolo SMTP sobre
 * SSL, solo con la biblioteca estandar de Java. Los datos de la cuenta se
 * configuran en config/correo.properties (por ejemplo, Gmail con "contraseña de aplicacion").
 */
public final class CorreoSMTP {
    public static final Path CONFIG = Path.of("config", "correo.properties");
    private final String host;
    private final int puerto;
    private final String usuario;
    private final String clave;
    private final String remitente;

    /** Crea el cliente SMTP con los datos de la cuenta de correo. */
    public CorreoSMTP(String host, int puerto, String usuario, String clave, String remitente) {
        this.host = host;
        this.puerto = puerto;
        this.usuario = usuario;
        this.clave = clave;
        this.remitente = remitente.isBlank() ? usuario : remitente;
    }

    /** Lee la cuenta desde config/correo.properties. */
    public static CorreoSMTP desdeConfiguracion() throws IOException {
        return desdeConfiguracionDe(CONFIG);
    }

    /** Lee la cuenta desde el archivo indicado; si falta usuario o clave, avisa qué completar. */
    public static CorreoSMTP desdeConfiguracionDe(Path archivo) throws IOException {
        Properties p = new Properties();
        if (Files.exists(archivo)) {
            try (InputStream in = Files.newInputStream(archivo)) {
                p.load(in);
            }
        }
        String usuario = p.getProperty("usuario", "").trim();
        if (usuario.isEmpty() || p.getProperty("clave", "").isBlank()) {
            throw new IllegalStateException("El envio por correo no está configurado. Completa usuario y clave en "
                    + archivo.toAbsolutePath() + " (ver manual de instalación).");
        }
        int puerto;
        try {
            puerto = Integer.parseInt(p.getProperty("puerto", "465").trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("El puerto de correo no es válido.");
        }
        return new CorreoSMTP(p.getProperty("host", "smtp.gmail.com").trim(), puerto, usuario,
                p.getProperty("clave", "").trim(), p.getProperty("remitente", "").trim());
    }

    /** Valida el formato básico de un correo (texto@dominio.ext). */
    public static boolean esCorreoValido(String correo) {
        return correo != null && correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    }

    /** Envia un mensaje de texto con un archivo adjunto. */
    public void enviar(String destino, String asunto, String cuerpo, File adjunto) throws IOException {
        if (!esCorreoValido(destino)) {
            throw new IllegalArgumentException("El correo del destinatario no es válido: " + destino);
        }
        try (Socket s = SSLSocketFactory.getDefault().createSocket(host, puerto)) {
            s.setSoTimeout(20000);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            OutputStream out = s.getOutputStream();
            respuesta(in, 220);
            comando(out, in, "EHLO coloresycreaciones", 250);
            comando(out, in, "AUTH LOGIN", 334);
            comando(out, in, b64(usuario), 334);
            comando(out, in, b64(clave), 235);
            comando(out, in, "MAIL FROM:<" + remitente + ">", 250);
            comando(out, in, "RCPT TO:<" + destino + ">", 250);
            comando(out, in, "DATA", 354);
            escribir(out, mensaje(destino, asunto, cuerpo, adjunto) + "\r\n.");
            respuesta(in, 250);
            comando(out, in, "QUIT", 221);
        }
    }

    /** Arma el mensaje MIME: texto en UTF-8 y el PDF adjunto en Base64. */
    private String mensaje(String destino, String asunto, String cuerpo, File adjunto) throws IOException {
        String limite = "----=_ColoresYCreaciones_" + System.nanoTime();
        StringBuilder m = new StringBuilder();
        m.append("From: Colores y Creaciones <").append(remitente).append(">\r\n");
        m.append("To: <").append(destino).append(">\r\n");
        m.append("Subject: =?UTF-8?B?").append(b64(asunto)).append("?=\r\n");
        m.append("MIME-Version: 1.0\r\n");
        m.append("Content-Type: multipart/mixed; boundary=\"").append(limite).append("\"\r\n\r\n");
        m.append("--").append(limite).append("\r\n");
        m.append("Content-Type: text/plain; charset=UTF-8\r\nContent-Transfer-Encoding: base64\r\n\r\n");
        m.append(lineas(b64(cuerpo))).append("\r\n");
        if (adjunto != null) {
            m.append("--").append(limite).append("\r\n");
            m.append("Content-Type: application/pdf; name=\"").append(adjunto.getName()).append("\"\r\n");
            m.append("Content-Transfer-Encoding: base64\r\n");
            m.append("Content-Disposition: attachment; filename=\"").append(adjunto.getName()).append("\"\r\n\r\n");
            m.append(lineas(Base64.getEncoder().encodeToString(Files.readAllBytes(adjunto.toPath())))).append("\r\n");
        }
        m.append("--").append(limite).append("--");
        return m.toString();
    }

    /** Divide el Base64 en líneas de 76 caracteres, como exige el estándar MIME. */
    private static String lineas(String base64) {
        return base64.replaceAll("(.{76})", "$1\r\n");
    }

    /** Codifica un texto en Base64 (UTF-8). */
    private static String b64(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    /** Envía una línea al servidor terminada en CRLF. */
    private static void escribir(OutputStream out, String linea) throws IOException {
        out.write((linea + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    /** Envía un comando SMTP y verifica el código de respuesta esperado. */
    private static void comando(OutputStream out, BufferedReader in, String linea, int esperado) throws IOException {
        escribir(out, linea);
        respuesta(in, esperado);
    }

    /** Lee la respuesta (puede tener varias lineas) y verifica el codigo. */
    private static void respuesta(BufferedReader in, int esperado) throws IOException {
        String linea;
        do {
            linea = in.readLine();
            if (linea == null) {
                throw new IOException("El servidor de correo cerro la conexion.");
            }
        } while (linea.length() > 3 && linea.charAt(3) == '-');
        if (!linea.startsWith(String.valueOf(esperado))) {
            throw new IOException("El servidor de correo respondio: " + linea);
        }
    }
}
