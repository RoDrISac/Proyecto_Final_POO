package proyectofina.servicios;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import proyectofina.*;
public final class ReportesServicio {
    private final Sistema s;
    public static final String[] TIPOS= {
        "INVENTARIO","STOCK_BAJO","VENTAS","MAS_VENDIDOS","VENTAS_DIARIAS","VENTAS_MENSUALES","MOVIMIENTOS"
    }
    ;
    public ReportesServicio(Sistema s) {
        this.s=s;
    }
    public String contenido(String tipo,LocalDate desde,LocalDate hasta) {
        s.exigir("REPORTES");
        if(desde.isAfter(hasta)) throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
        var e=s.consultar();
        if(!tipo.equals("MOVIMIENTOS")) return new Reporte(e.ventas,e.productos).generar(tipo).getContenido();
        var b=new StringBuilder("COLORES Y CREACIONES - MOVIMIENTOS\nPeriodo: "+desde+" a "+hasta+"\n\nFecha y hora | Usuario | Producto | Tipo | Cantidad | Saldo\n");
        for(var m:e.movimientos) {
            var fecha=m.fecha().toInstant().atZone(ZoneId.systemDefault());
            if(fecha.toLocalDate().isBefore(desde)||fecha.toLocalDate().isAfter(hasta))continue;
            b.append(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append(" | ").append(m.usuario()).append(" | ").append(m.producto()).append(" | ").append(m.tipo()).append(" | ").append(m.cantidad()).append(" | ").append(m.saldo()).append('\n');
        }
        return b.toString();
    }
    public File exportar(String tipo,String contenido,String formato) throws IOException {
        s.exigir("REPORTES");
        File f=new File(GestorArchivos.CARPETA_REPORTES,"REP-"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"))+"-"+tipo+"."+formato.toLowerCase());
        if(formato.equals("PDF")) ArchivoPDF.escribir(f,Arrays.asList(contenido.split("\n")));
        else GestorArchivos.escribirTexto(f,contenido);
        return f;
    }
    public void automaticos() throws IOException {
        if(!s.permite("REPORTES"))return;
        var e=s.consultar();
        for(String tipo:List.of("VENTAS_DIARIAS","VENTAS_MENSUALES")) new Reporte(e.ventas,e.productos).generar(tipo).exportar("TXT");
    }
}
