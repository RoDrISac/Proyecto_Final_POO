/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectofina;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera un PDF simple (texto en fuente Courier, hoja A4) sin librerias externas.
 * Se usa para los comprobantes y para exportar reportes.
 *
 * @author Rodrigo
 */
public class ArchivoPDF {

    private static final int LINEAS_POR_PAGINA = 55;
    private static final Charset WIN_ANSI = Charset.forName("windows-1252");

    public static void escribir(File archivo, List<String> lineas) throws IOException {
        List<List<String>> paginas = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i += LINEAS_POR_PAGINA) {
            paginas.add(lineas.subList(i, Math.min(i + LINEAS_POR_PAGINA, lineas.size())));
        }
        if (paginas.isEmpty()) {
            paginas.add(new ArrayList<>());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> posiciones = new ArrayList<>(); // posicion de cada objeto, para la tabla xref
        ascii(out, "%PDF-1.4\n");

        // Objetos 1 (catalogo), 2 (lista de paginas) y 3 (fuente); luego 2 objetos por pagina
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
            StringBuilder texto = new StringBuilder("BT\n/F1 10 Tf\n13 TL\n45 800 Td\n");
            for (String linea : paginas.get(i)) {
                texto.append('(').append(escapar(linea)).append(") Tj T*\n");
            }
            texto.append("ET");
            byte[] contenido = texto.toString().getBytes(WIN_ANSI);

            posiciones.add(out.size());
            ascii(out, (4 + 2 * i) + " 0 obj\n<< /Length " + contenido.length + " >>\nstream\n");
            out.write(contenido);
            ascii(out, "\nendstream\nendobj\n");
            posiciones.add(out.size());
            ascii(out, (5 + 2 * i) + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + (4 + 2 * i) + " 0 R >>\nendobj\n");
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

    private static void ascii(ByteArrayOutputStream out, String texto) throws IOException {
        out.write(texto.getBytes(StandardCharsets.ISO_8859_1));
    }

    // En un PDF los caracteres \ ( ) deben escaparse dentro del texto
    private static String escapar(String linea) {
        return linea.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
