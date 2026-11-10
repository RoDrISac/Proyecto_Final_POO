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
* Genera documentos A4 con encabezado, filas destacadas y numeración.
* Se usa para los comprobantes y para exportar reportes.
*
* @author Rodrigo
*/
public class ArchivoPDF {
    private static final int LINEAS_POR_PAGINA = 34;
    private static final Charset WIN_ANSI = Charset.forName("windows-1252");
    public static void escribir(File archivo, List<String> lineas) throws IOException {
        List<String> ajustadas = new ArrayList<>();
        for (String linea : lineas) {
            while (linea.length() > 82) {
                ajustadas.add(linea.substring(0,82));
                linea = linea.substring(82);
            }
            ajustadas.add(linea);
        }
        lineas = ajustadas;
        List<List<String>> paginas = new ArrayList<>();
        for (int i = 0;  i < lineas.size();  i += LINEAS_POR_PAGINA) {
            paginas.add(lineas.subList(i, Math.min(i + LINEAS_POR_PAGINA, lineas.size())));
        }
        if (paginas.isEmpty()) {
            paginas.add(new ArrayList<>());
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> posiciones = new ArrayList<>();
        // posicion de cada objeto, para la tabla xref
        ascii(out, "%PDF-1.4\n");
        // Objetos 1 (catalogo), 2 (lista de paginas) y 3 (fuente); luego 2 objetos por pagina
        StringBuilder kids = new StringBuilder();
        for (int i = 0;  i < paginas.size();  i++) {
            kids.append(5 + 2 * i).append(" 0 R ");
        }
        posiciones.add(out.size());
        ascii(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        posiciones.add(out.size());
        ascii(out, "2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count " + paginas.size() + " >>\nendobj\n");
        posiciones.add(out.size());
        ascii(out, "3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>\nendobj\n");
        for (int i = 0;  i < paginas.size();  i++) {
            StringBuilder texto = new StringBuilder();
            rectangulo(texto,0,724,595,118,"0.106 0.180 0.220");
            rotulo(texto,"COLORES Y CREACIONES",40,792,"F3",21,"1 1 1");
            rotulo(texto,"REPORTES Y COMPROBANTES",40,765,"F2",10,"0.55 0.84 0.75");
            rectangulo(texto,40,742,44,3,"0.55 0.84 0.75");
            int y=699;
            int fila=0;
            for (String linea : paginas.get(i)) {
                boolean cabecera=linea.startsWith("Cod.") || linea.startsWith("Fecha y hora |");
                boolean alerta=linea.stripTrailing().endsWith(" *");
                boolean resumen=linea.startsWith("Productos listados:") || linea.startsWith("Ventas:");
                if(cabecera)rectangulo(texto,36,y-6,523,20,"0.086 0.412 0.369");
                else if(alerta)rectangulo(texto,36,y-6,523,20,"1 0.945 0.820");
                else if(resumen)rectangulo(texto,36,y-6,523,20,"0.87 0.94 0.92");
                else if(!linea.isBlank() && fila%2==0)rectangulo(texto,36,y-6,523,20,"0.955 0.965 0.968");
                rotulo(texto,linea,43,y,"F1",10,cabecera?"1 1 1":alerta?"0.48 0.28 0.06":"0.106 0.180 0.220");
                y-=18;
                fila++;
            }
            rectangulo(texto,40,52,515,1,"0.80 0.85 0.85");
            rotulo(texto,"Colores y Creaciones | Gestión comercial",40,35,"F2",9,"0.36 0.44 0.47");
            rotulo(texto,"Página "+(i+1)+" de "+paginas.size(),475,35,"F2",9,"0.36 0.44 0.47");
            byte[] contenido = texto.toString().getBytes(WIN_ANSI);
            posiciones.add(out.size());
            ascii(out, (4 + 2 * i) + " 0 obj\n<< /Length " + contenido.length + " >>\nstream\n");
            out.write(contenido);
            ascii(out, "\nendstream\nendobj\n");
            posiciones.add(out.size());
            ascii(out, (5 + 2 * i) + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "                     + "/Resources << /Font << /F1 3 0 R /F2 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >> /F3 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >> >> >> /Contents " + (4 + 2 * i) + " 0 R >>\nendobj\n");
        }
        int inicioXref = out.size();
        StringBuilder xref = new StringBuilder("xref\n0 " + (posiciones.size() + 1) + "\n0000000000 65535 f \n");
        for (int pos : posiciones) {
            xref.append(String.format("%010d 00000 n \n", pos));
        }
        xref.append("trailer\n<< /Size ").append(posiciones.size() + 1).append(" /Root 1 0 R >>\n")             .append("startxref\n").append(inicioXref).append("\n%%EOF\n");
        ascii(out, xref.toString());
        archivo.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            out.writeTo(fos);
        }
    }
    private static void rectangulo(StringBuilder b,int x,int y,int w,int h,String color) {
        b.append(color).append(" rg ").append(x).append(' ').append(y).append(' ').append(w).append(' ').append(h).append(" re f\n");
    }
    private static void rotulo(StringBuilder b,String valor,int x,int y,String fuente,int tam,String color) {
        b.append("BT /").append(fuente).append(' ').append(tam).append(" Tf ").append(color).append(" rg ")
            .append(x).append(' ').append(y).append(" Td (").append(escapar(valor)).append(") Tj ET\n");
    }
    private static void ascii(ByteArrayOutputStream out, String texto) throws IOException {
        out.write(texto.getBytes(StandardCharsets.ISO_8859_1));
    }
    // En un PDF los caracteres \ ( ) deben escaparse dentro del texto
    private static String escapar(String linea) {
        return linea.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
