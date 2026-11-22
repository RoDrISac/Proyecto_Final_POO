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

    /** Prepara el comprobante de una venta: serie B001 para boletas y R001 para recibos. */
    public Comprobante(Venta venta) {
        this.venta = venta;
        this.tipo = venta.getTipoComprobante();
        // Numeracion unica: serie segun el tipo + numero de la venta
        this.idComprobante = (tipo.equals("Boleta") ? "B001-" : "R001-") + venta.getIdVenta().substring(1);
        this.fechaEmision = venta.getFecha();
        // al reimprimir se conserva la fecha de la venta
        this.montoTotal = venta.getMontoTotal();
        this.formato = "PDF";
    }

    /** Genera el archivo PDF en la carpeta de comprobantes. */
    public void emitirPDF() throws IOException {
        archivo = new File(GestorArchivos.CARPETA_COMPROBANTES, idComprobante + ".pdf");
        ArchivoPDF.escribir(archivo, generarLineas());
    }

    /** Envio virtual del comprobante al cliente (S08 N.° 28): genera el PDF y lo adjunta. */
    public void enviarPorCorreo(String destino) throws IOException {
        if (!CorreoSMTP.esCorreoValido(destino)) {
            throw new IllegalArgumentException("El cliente no tiene un correo válido: " + destino);
        }
        if (archivo == null) {
            emitirPDF();
        }
        String cuerpo = "Estimado(a) " + venta.getCliente().getNombreCompleto() + ":\n\n"
                + "Adjuntamos su " + tipo.toLowerCase() + " " + idComprobante + " por S/ " + String.format("%.2f", montoTotal) + ".\n\n"
                + "Gracias por su preferencia.\n" + Empresa.cargar().getRazonSocial();
        CorreoSMTP.desdeConfiguracion().enviar(destino, tipo + " " + idComprobante + " - Colores y Creaciones", cuerpo, archivo);
    }

    /** Arma el texto del comprobante: empresa, cliente, detalle, descuento, IGV (18 %) y total. */
    public List<String> generarLineas() {
        // Los precios incluyen IGV (18%)
        double opGravada = montoTotal / 1.18;
        double igv = montoTotal - opGravada;
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Cliente c = venta.getCliente();
        Empresa e = Empresa.cargar();
        List<String> l = new ArrayList<>();
        l.add("==========================================================");
        l.add(centrar(e.getRazonSocial().toUpperCase()));
        l.add(centrar(e.getLema()));
        if (!e.getRuc().isEmpty()) {
            l.add(centrar("RUC: " + e.getRuc()));
        }
        l.add(centrar(e.getDireccion() + (e.getTelefono().isEmpty() ? "" : "  Tel. " + e.getTelefono())));
        l.add("==========================================================");
        l.add(tipo.toUpperCase() + " DE VENTA  N° " + idComprobante);
        l.add("Fecha de emision: " + f.format(fechaEmision));
        l.add("Venta: " + venta.getIdVenta() + "   Vendedor: " + venta.getVendedor().getNombreCompleto());
        l.add("");
        if (c.esClienteVarios()) {
            l.add("Cliente:   CLIENTES VARIOS");
        } else {
            l.add("Cliente:   " + c.getNombreCompleto());
            l.add("DNI:       " + c.getDni());
            l.add("Direccion: " + c.getDireccion());
        }
        l.add("----------------------------------------------------------");
        l.add(String.format("%-26s %5s %10s %12s", "Producto", "Cant.", "P. Unit.", "Importe"));
        l.add("----------------------------------------------------------");
        for (DetalleVenta d : venta.getDetalles()) {
            l.add(String.format("%-26s %5d %10.2f %12.2f", recortar(d.getProducto().getNombre(), 26),
                    d.getCantidad(), d.getPrecioUnitario(), d.calcularSubtotal()));
        }
        l.add("----------------------------------------------------------");
        if (venta.getDescuentoPorcentaje() > 0) {
            l.add(String.format("%45s %12.2f", "Subtotal  S/", venta.calcularSubtotal()));
            l.add(String.format("%45s %12.2f",
                    String.format("Descuento %.0f%%  S/", venta.getDescuentoPorcentaje()), -venta.calcularDescuento()));
        }
        l.add(String.format("%45s %12.2f", "Op. gravada  S/", opGravada));
        l.add(String.format("%45s %12.2f", "IGV (18%)  S/", igv));
        l.add(String.format("%45s %12.2f", "TOTAL  S/", montoTotal));
        l.add("");
        l.add("Metodo de pago: " + venta.getMetodoPago());
        l.add("==========================================================");
        l.add("               Gracias por su preferencia");
        return l;
    }

    /** Centra un texto en el ancho del comprobante (58 caracteres). */
    private static String centrar(String texto) {
        int espacios = Math.max(0, (58 - texto.length()) / 2);
        return " ".repeat(espacios) + texto;
    }

    /** Corta un texto largo para que no desalinee las columnas. */
    private String recortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max);
    }

    // ---------- Getters y setters ----------
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
