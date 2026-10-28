/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectofina;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reportes del sistema: inventario, ventas, productos mas vendidos del mes y stock bajo.
 *
 * @author Alvaro
 */
public class Reporte {
    private String idReporte;
    private Date fechaGeneracion;
    private String tipo;
    private String contenido;
    private final List<Venta> ventas;
    private final List<Inventario> inventario;

    public Reporte(List<Venta> ventas, List<Inventario> inventario) {
        this.ventas = ventas;
        this.inventario = inventario;
    }

    public Reporte generar(String tipo) {
        this.tipo = tipo;
        this.fechaGeneracion = new Date();
        this.idReporte = "REP-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(fechaGeneracion) + "-" + tipo;
        StringBuilder sb = new StringBuilder();
        sb.append("COLORES Y CREACIONES - REPORTE DE ").append(tipo.replace('_', ' ')).append("\n");
        sb.append("Generado: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fechaGeneracion)).append("\n\n");
        switch (tipo) {
            case "INVENTARIO" -> reporteInventario(sb, false);
            case "STOCK_BAJO" -> reporteInventario(sb, true);
            case "VENTAS" -> reporteVentas(sb);
            case "MAS_VENDIDOS" -> reporteMasVendidos(sb);
            default -> throw new IllegalArgumentException("Tipo de reporte no valido: " + tipo);
        }
        this.contenido = sb.toString();
        return this;
    }

    public void exportar(String formato) throws IOException {
        if (contenido == null) {
            throw new IllegalStateException("Primero genere el reporte.");
        }
        File archivo = new File(GestorArchivos.CARPETA_REPORTES, idReporte + "." + formato.toLowerCase());
        if (formato.equalsIgnoreCase("PDF")) {
            ArchivoPDF.escribir(archivo, Arrays.asList(contenido.split("\n")));
        } else {
            GestorArchivos.escribirTexto(archivo, contenido);
        }
        System.out.println("Reporte exportado en: " + archivo.getPath());
    }

    private void reporteInventario(StringBuilder sb, boolean soloStockBajo) {
        sb.append(String.format("%-6s %-22s %-10s %6s %6s %9s %9s%n", "Cod.", "Producto", "Modelo", "Stock", "Min.", "Precio", "Ganancia"));
        int n = 0;
        for (Inventario p : inventario) {
            if (soloStockBajo && !p.tieneStockBajo()) {
                continue;
            }
            sb.append(String.format("%-6s %-22s %-10s %6d %6d %9.2f %9.2f%s%n", p.getIdProducto(), corto(p.getNombre(), 22),
                    corto(p.getModelo(), 10), p.getStock(), p.getStockMinimo(), p.getPrecio(),
                    p.calcularGananciaUnitaria(), p.tieneStockBajo() ? " *" : ""));
            n++;
        }
        sb.append("\nProductos listados: ").append(n).append("   (* = stock bajo)\n");
    }

    private void reporteVentas(StringBuilder sb) {
        double total = 0;
        for (Venta v : ventas) {
            sb.append(v.getResumenVenta()).append("\n\n");
            total += v.getMontoTotal();
        }
        sb.append(String.format("Ventas: %d | Monto total vendido: S/ %.2f%n", ventas.size(), total));
    }

    private void reporteMasVendidos(StringBuilder sb) {
        Calendar hoy = Calendar.getInstance();
        Map<String, Integer> unidades = new LinkedHashMap<>();
        for (Venta v : ventas) {
            Calendar c = Calendar.getInstance();
            c.setTime(v.getFecha());
            if (c.get(Calendar.YEAR) != hoy.get(Calendar.YEAR) || c.get(Calendar.MONTH) != hoy.get(Calendar.MONTH)) {
                continue; // solo las ventas del mes actual
            }
            for (int i = 0; i < v.getProductosVendidos().size(); i++) {
                unidades.merge(v.getProductosVendidos().get(i).getNombre(), v.getCantidades().get(i), Integer::sum);
            }
        }
        sb.append("Mes: ").append(new SimpleDateFormat("MM/yyyy").format(hoy.getTime())).append("\n");
        if (unidades.isEmpty()) {
            sb.append("No hay ventas en el mes.\n");
            return;
        }
        int puesto = 1;
        for (Map.Entry<String, Integer> e : unidades.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).toList()) {
            sb.append(String.format("%2d. %-30s %5d unidades%n", puesto++, e.getKey(), e.getValue()));
        }
    }

    private String corto(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max);
    }

    public String getIdReporte() {
        return idReporte;
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getContenido() {
        return contenido;
    }
}
