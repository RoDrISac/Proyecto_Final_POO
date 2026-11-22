package proyectofina;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Genera documentos A4 con encabezado, filas destacadas y numeración.
 * Se usa para los comprobantes y para exportar reportes.
 *
 * @author Rodrigo
 */
public class ArchivoPDF {
    private static final int LINEAS_POR_PAGINA = 34;
    private static final Charset WIN_ANSI = Charset.forName("windows-1252");

    // Paleta "porcelana y cobalto" de la interfaz (ver Gui), en RGB 0-1 para el PDF
    private static final String COBALTO = "0.137 0.251 0.604";      // Gui.ACENTO
    private static final String COBALTO_SUAVE = "0.914 0.933 0.980"; // Gui.ACENTO_SUAVE
    private static final String PORCELANA = "0.965 0.973 0.988";     // Gui.FONDO
    private static final String ARCILLA = "0.710 0.396 0.227";       // Gui.ARCILLA
    private static final String TINTA = "0.118 0.141 0.200";         // Gui.TINTA
    private static final String TEXTO_SUAVE = "0.396 0.431 0.522";   // Gui.TEXTO_SUAVE
    private static final String BORDE = "0.867 0.890 0.933";         // Gui.BORDE
    private static final String BLANCO = "1 1 1";

    /**
     * Escribe un PDF A4 con las líneas recibidas: corta entre palabras las líneas de más de 82 caracteres,
     * pagina cada 34 líneas y arma a mano la estructura del PDF (catálogo, páginas, fuentes y xref).
     */
    public static void escribir(File archivo, List<String> lineas) throws IOException {
        List<String> ajustadas = new ArrayList<>();
        for (String linea : lineas) {
            while (linea.length() > 82) {
                // Corta en el último espacio para no partir palabras; la continuación va con sangría
                int corte = linea.lastIndexOf(' ', 82);
                if (corte < 40) {
                    corte = 82;
                }
                ajustadas.add(linea.substring(0, corte).stripTrailing());
                linea = "    " + linea.substring(corte).strip();
            }
            ajustadas.add(linea);
        }
        lineas = ajustadas;
        List<List<String>> paginas = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i += LINEAS_POR_PAGINA) {
            paginas.add(lineas.subList(i, Math.min(i + LINEAS_POR_PAGINA, lineas.size())));
        }
        if (paginas.isEmpty()) {
            paginas.add(new ArrayList<>());
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Posición en bytes de cada objeto, necesaria para la tabla xref del final
        List<Integer> posiciones = new ArrayList<>();
        ascii(out, "%PDF-1.4\n");
        // Objetos 1 (catálogo), 2 (lista de páginas) y 3 (fuente); luego 2 objetos por página
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < paginas.size(); i++) {
            kids.append(5 + 2 * i).append(" 0 R ");
        }
        posiciones.add(out.size());
        ascii(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        posiciones.add(out.size());
        ascii(out, "2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count " + paginas.size() + " >>\nendobj\n");
        posiciones.add(out.size());
        ascii(out, "3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>\nendobj\n");
        for (int i = 0; i < paginas.size(); i++) {
            // Encabezado de la página
            StringBuilder texto = new StringBuilder();
            encabezado(texto);
            // Cuerpo: las líneas de "=" y "-" se dibujan como filetes; el resto como texto con fondo según su tipo
            int y = 688;
            boolean enTabla = false;
            boolean trasCabecera = false;
            int fila = 0;
            for (String linea : paginas.get(i)) {
                String limpia = linea.strip();
                if (!limpia.isEmpty() && limpia.chars().allMatch(ch -> ch == '=')) {
                    rectangulo(texto, 36, y + 2, 523, 1, COBALTO);
                    enTabla = false;
                    y -= 10;
                    continue;
                }
                if (!limpia.isEmpty() && limpia.chars().allMatch(ch -> ch == '-')) {
                    // Justo bajo la cabecera de tabla el filete sobra; en otro lugar cierra la tabla
                    if (!trasCabecera) {
                        rectangulo(texto, 36, y + 2, 523, 0.6, BORDE);
                        enTabla = false;
                        y -= 10;
                    }
                    trasCabecera = false;
                    continue;
                }
                // Cabeceras de tabla: reportes ("Cod.", "Fecha y hora |") y detalle del comprobante
                boolean cabecera = linea.startsWith("Cod.") || linea.startsWith("Fecha y hora |")
                        || (linea.startsWith("Producto ") && linea.contains("Cant.") && linea.contains("Importe"));
                trasCabecera = cabecera;
                boolean alerta = linea.stripTrailing().endsWith(" *");
                boolean resumen = linea.startsWith("Productos listados:") || linea.startsWith("Ventas:");
                boolean total = limpia.startsWith("TOTAL ");
                boolean titulo = linea.contains(" DE VENTA  N") || linea.startsWith("COLORES Y CREACIONES -");
                String fuente = "F1";
                String color = TINTA;
                if (cabecera) {
                    rectangulo(texto, 36, y - 6, 523, 20, COBALTO);
                    color = BLANCO;
                    fuente = "F4";
                    enTabla = true;
                    fila = 0;
                } else if (alerta) {
                    rectangulo(texto, 36, y - 6, 523, 20, "1 0.945 0.820");
                    rectangulo(texto, 36, y - 6, 3, 20, ARCILLA);
                    color = "0.48 0.28 0.06";
                } else if (total) {
                    rectangulo(texto, 250, y - 7, 309, 22, COBALTO_SUAVE);
                    rectangulo(texto, 250, y - 7, 3, 22, COBALTO);
                    color = COBALTO;
                    fuente = "F4";
                } else if (resumen) {
                    rectangulo(texto, 36, y - 6, 523, 20, COBALTO_SUAVE);
                    color = COBALTO;
                    fuente = "F4";
                } else if (titulo) {
                    color = COBALTO;
                    fuente = "F4";
                } else if (linea.isBlank()) {
                    enTabla = false;
                } else if (enTabla && fila % 2 == 1) {
                    rectangulo(texto, 36, y - 6, 523, 20, PORCELANA);
                }
                rotulo(texto, linea, 43, y, fuente, 10, color);
                y -= 18;
                fila++;
            }
            // Pie de página
            rectangulo(texto, 40, 52, 515, 0.6, BORDE);
            rotulo(texto, "Colores y Creaciones | Cerámica artesanal de Cajamarca", 40, 36, "F2", 8, TEXTO_SUAVE);
            rotulo(texto, "Página " + (i + 1) + " de " + paginas.size(), 490, 36, "F2", 8, TEXTO_SUAVE);
            byte[] contenido = texto.toString().getBytes(WIN_ANSI);
            posiciones.add(out.size());
            ascii(out, (4 + 2 * i) + " 0 obj\n<< /Length " + contenido.length + " >>\nstream\n");
            out.write(contenido);
            ascii(out, "\nendstream\nendobj\n");
            posiciones.add(out.size());
            String fuentes = "/F1 3 0 R"
                    + " /F2 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"
                    + " /F3 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>"
                    + " /F4 << /Type /Font /Subtype /Type1 /BaseFont /Courier-Bold /Encoding /WinAnsiEncoding >>";
            ascii(out, (5 + 2 * i) + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << " + fuentes + " >> >> /Contents " + (4 + 2 * i) + " 0 R >>\nendobj\n");
        }
        int inicioXref = out.size();
        StringBuilder xref = new StringBuilder("xref\n0 " + (posiciones.size() + 1) + "\n0000000000 65535 f \n");
        for (int pos : posiciones) {
            xref.append(String.format("%010d 00000 n \n", pos));
        }
        xref.append("trailer\n<< /Size ").append(posiciones.size() + 1).append(" /Root 1 0 R >>\n")
                .append("startxref\n").append(inicioXref).append("\n%%EOF\n");
        ascii(out, xref.toString());
        archivo.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            out.writeTo(fos);
        }
    }

    /**
     * Encabezado en porcelana con el medallón de cerámica (anillos cobalto y arcilla, como el
     * plato decorativo de la interfaz), el nombre de la empresa y un filete cobalto con remate de arcilla.
     */
    private static void encabezado(StringBuilder b) {
        rectangulo(b, 0, 728, 595, 114, PORCELANA);
        // Medallón: plato pintado a mano visto desde arriba
        int cx = 520, cy = 785;
        circulo(b, cx, cy, 38, BLANCO, BORDE, 1);
        circulo(b, cx, cy, 33, null, COBALTO, 2.2);
        circulo(b, cx, cy, 27, null, ARCILLA, 0.8);
        circulo(b, cx, cy, 12, COBALTO_SUAVE, COBALTO, 1.2);
        for (int k = 0; k < 8; k++) {
            double a = Math.PI / 4 * k;
            circulo(b, cx + 20 * Math.cos(a), cy + 20 * Math.sin(a), 2.4, k % 2 == 0 ? COBALTO : ARCILLA, null, 0);
        }
        circulo(b, cx, cy, 3, COBALTO, null, 0);
        rotulo(b, "Colores y Creaciones", 40, 790, "F3", 24, COBALTO);
        rotulo(b, "Cerámica artesanal de Cajamarca", 40, 768, "F2", 10, TEXTO_SUAVE);
        rectangulo(b, 0, 728, 595, 2, COBALTO);
        rectangulo(b, 40, 728, 60, 2, ARCILLA);
    }

    /** Círculo aproximado con cuatro curvas Bézier; relleno y/o contorno según los colores recibidos. */
    private static void circulo(StringBuilder b, double cx, double cy, double r, String relleno, String trazo, double grosor) {
        double k = 0.5523 * r;
        String c = n(cx + r) + " " + n(cy) + " m\n"
                + n(cx + r) + " " + n(cy + k) + " " + n(cx + k) + " " + n(cy + r) + " " + n(cx) + " " + n(cy + r) + " c\n"
                + n(cx - k) + " " + n(cy + r) + " " + n(cx - r) + " " + n(cy + k) + " " + n(cx - r) + " " + n(cy) + " c\n"
                + n(cx - r) + " " + n(cy - k) + " " + n(cx - k) + " " + n(cy - r) + " " + n(cx) + " " + n(cy - r) + " c\n"
                + n(cx + k) + " " + n(cy - r) + " " + n(cx + r) + " " + n(cy - k) + " " + n(cx + r) + " " + n(cy) + " c\n";
        if (relleno != null) {
            b.append(relleno).append(" rg\n").append(c).append("f\n");
        }
        if (trazo != null) {
            b.append(trazo).append(" RG ").append(n(grosor)).append(" w\n").append(c).append("S\n");
        }
    }

    /** Número con punto decimal y dos decimales, como exige el formato PDF. */
    private static String n(double v) {
        return String.format(Locale.ROOT, "%.2f", v);
    }

    /** Comando PDF para dibujar un rectángulo relleno (fondos, filetes y franjas). */
    private static void rectangulo(StringBuilder b, double x, double y, double w, double h, String color) {
        b.append(color).append(" rg ").append(n(x)).append(' ').append(n(y)).append(' ')
                .append(n(w)).append(' ').append(n(h)).append(" re f\n");
    }

    /** Comando PDF para escribir un texto en la posición indicada. */
    private static void rotulo(StringBuilder b, String valor, int x, int y, String fuente, int tam, String color) {
        b.append("BT /").append(fuente).append(' ').append(tam).append(" Tf ").append(color).append(" rg ")
                .append(x).append(' ').append(y).append(" Td (").append(escapar(valor)).append(") Tj ET\n");
    }

    /** Escribe texto ASCII en el flujo del PDF (estructura del archivo). */
    private static void ascii(ByteArrayOutputStream out, String texto) throws IOException {
        out.write(texto.getBytes(StandardCharsets.ISO_8859_1));
    }

    /** En un PDF los caracteres \ ( ) deben escaparse dentro del texto. */
    private static String escapar(String linea) {
        return linea.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
