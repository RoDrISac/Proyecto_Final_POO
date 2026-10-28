/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectofina;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Comprobante de pago (boleta o recibo) de una venta. Se emite como archivo PDF.
 *
 * @author Alvaro
 */
public class Comprobante {
    private String idComprobante;
    private String tipo;
    private Date fechaEmision;
    private double montoTotal;
    private String formato;
    private Venta venta;
    private File archivo;

    public Comprobante(Venta venta) {
        this.venta = venta;
        this.tipo = venta.getTipoComprobante();
        // Numeracion unica: serie segun el tipo + numero de la venta
        this.idComprobante = (tipo.equals("Boleta") ? "B001-" : "R001-") + venta.getIdVenta().substring(1);
        this.fechaEmision = new Date();
        this.montoTotal = venta.getMontoTotal();
        this.formato = "PDF";
    }

    public void emitirPDF() throws IOException {
        archivo = new File(GestorArchivos.CARPETA_COMPROBANTES, idComprobante + ".pdf");
        ArchivoPDF.escribir(archivo, generarLineas());
    }

    // El envio por correo es opcional en el alcance del proyecto (S07); por ahora solo valida el correo
    public void enviarPorCorreo(String destino) {
        if (destino == null || !destino.contains("@")) {
            System.out.println("Correo no valido: " + destino);
            return;
        }
        System.out.println("Envio por correo no disponible todavia (funcion opcional). "
                + "Adjunte manualmente el archivo: " + (archivo != null ? archivo.getPath() : "(emita primero el PDF)"));
    }

    public List<String> generarLineas() {
        // Los precios incluyen IGV (18%)
        double opGravada = montoTotal / 1.18;
        double igv = montoTotal - opGravada;
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Cliente c = venta.getCliente();
        List<String> l = new ArrayList<>();
        l.add("==========================================================");
        l.add("                  COLORES Y CREACIONES");
        l.add("            Productos artesanales - Cajamarca");
        l.add("==========================================================");
        l.add(tipo.toUpperCase() + " DE VENTA  N° " + idComprobante);
        l.add("Fecha de emision: " + f.format(fechaEmision));
        l.add("Venta: " + venta.getIdVenta() + "   Vendedor: " + venta.getVendedor().getNombreCompleto());
        l.add("");
        l.add("Cliente:   " + c.getNombreCompleto());
        l.add("DNI:       " + c.getDni());
        l.add("Direccion: " + c.getDireccion());
        l.add("----------------------------------------------------------");
        l.add(String.format("%-26s %5s %10s %12s", "Producto", "Cant.", "P. Unit.", "Importe"));
        l.add("----------------------------------------------------------");
        List<Producto> productos = venta.getProductosVendidos();
        for (int i = 0; i < productos.size(); i++) {
            int cant = venta.getCantidades().get(i);
            double precio = venta.getPreciosUnitarios().get(i);
            l.add(String.format("%-26s %5d %10.2f %12.2f", recortar(productos.get(i).getNombre(), 26), cant, precio, cant * precio));
        }
        l.add("----------------------------------------------------------");
        l.add(String.format("%45s %12.2f", "Op. gravada  S/", opGravada));
        l.add(String.format("%45s %12.2f", "IGV (18%)  S/", igv));
        l.add(String.format("%45s %12.2f", "TOTAL  S/", montoTotal));
        l.add("");
        l.add("Metodo de pago: " + venta.getMetodoPago());
        l.add("==========================================================");
        l.add("               Gracias por su preferencia");
        return l;
    }

    private String recortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max);
    }

    public String getIdComprobante() {
        return idComprobante;
    }

    public String getTipo() {
        return tipo;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public String getFormato() {
        return formato;
    }

    public File getArchivo() {
        return archivo;
    }
}
