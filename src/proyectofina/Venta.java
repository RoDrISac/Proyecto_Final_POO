package proyectofina;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Venta compuesta por detalles propios. Conserva instantáneas del cliente y vendedor.
 *
 * @author Alvaro
 */
public class Venta implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String idVenta;
    private Date fecha;
    private String hora;
    private Cliente cliente;
    private Usuario vendedor;
    private final List<DetalleVenta> detalles = new ArrayList<>();
    private int cantidadProductos;
    private double montoTotal;
    private String metodoPago;
    private String tipoComprobante;
    private String estado = "Pendiente";
    private double descuentoPorcentaje;

    /** Descuento máximo permitido en una venta (alcance S07). */
    public static final double DESCUENTO_MAXIMO = 30;

    /** Crea la venta guardando copias del cliente y del vendedor (instantáneas históricas). */
    public Venta(String idVenta, Date fecha, Cliente cliente, Usuario vendedor, String metodoPago, String tipoComprobante) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.hora = new SimpleDateFormat("HH:mm:ss").format(fecha);
        this.cliente = new Cliente(cliente.getIdCliente(), cliente.getNombre(), cliente.getApellido(), cliente.getDni(),
                cliente.getDireccion(), cliente.getTelefono(), cliente.getCorreoElectronico(), cliente.getCategoria());
        this.vendedor = new Usuario(vendedor.getIdUsuario(), vendedor.getNombre(), vendedor.getApellido(),
                vendedor.getDni(), vendedor.getRol(), "");
        this.metodoPago = metodoPago;
        this.tipoComprobante = tipoComprobante;
    }

    /** Agrega una línea de detalle (composición) y recalcula el total. */
    public void agregarProducto(Producto producto, int cantidad, double precio) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (!Double.isFinite(precio) || precio < 0) {
            throw new IllegalArgumentException("Precio inválido");
        }
        // La venta es dueña de sus detalles: guardan el precio de ese momento
        detalles.add(new DetalleVenta(producto, cantidad, precio));
        cantidadProductos += cantidad;
        calcularMontoTotal();
    }

    /** Suma de los subtotales, antes del descuento. */
    public double calcularSubtotal() {
        BigDecimal suma = BigDecimal.ZERO;
        for (DetalleVenta d : detalles) {
            suma = suma.add(BigDecimal.valueOf(d.getPrecioUnitario()).multiply(BigDecimal.valueOf(d.getCantidad())));
        }
        return suma.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /** Importe del descuento sobre el subtotal, redondeado a 2 decimales. */
    public double calcularDescuento() {
        return BigDecimal.valueOf(calcularSubtotal() * descuentoPorcentaje / 100.0).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /** Subtotal menos descuento; se guarda en montoTotal. */
    public double calcularMontoTotal() {
        montoTotal = BigDecimal.valueOf(calcularSubtotal())
                .subtract(BigDecimal.valueOf(calcularDescuento()))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        return montoTotal;
    }

    /** Descuento en porcentaje sobre el subtotal (0 a 30 %). */
    public void aplicarDescuento(double porcentaje) {
        if (!Double.isFinite(porcentaje) || porcentaje < 0 || porcentaje > DESCUENTO_MAXIMO) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 % y " + (int) DESCUENTO_MAXIMO + " %.");
        }
        descuentoPorcentaje = porcentaje;
        calcularMontoTotal();
    }

    // ---------- Getters y setters ----------
    public double getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public int getCantidadProductos() {
        return cantidadProductos;
    }

    /** Texto de la venta con su detalle, descuento y total. */
    public String getResumenVenta() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Venta %s | %s %s | Cliente: %s | Vendedor: %s%n", idVenta,
                new SimpleDateFormat("dd/MM/yyyy").format(fecha), hora, cliente.getNombreCompleto(),
                vendedor.getNombreCompleto()));
        for (DetalleVenta d : getDetalles()) {
            sb.append(String.format("   %-22s x%-3d S/ %8.2f = S/ %9.2f%n", d.getProducto().getNombre(),
                    d.getCantidad(), d.getPrecioUnitario(), d.calcularSubtotal()));
        }
        if (descuentoPorcentaje > 0) {
            sb.append(String.format("   Subtotal: S/ %.2f | Descuento %.0f %%: -S/ %.2f%n", calcularSubtotal(),
                    descuentoPorcentaje, calcularDescuento()));
        }
        sb.append(String.format("   Productos: %d | Total: S/ %.2f | Pago: %s | %s | Estado: %s",
                cantidadProductos, montoTotal, metodoPago, tipoComprobante, estado));
        return sb.toString();
    }

    public String getIdVenta() {
        return idVenta;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public List<Producto> getProductosVendidos() {
        return detalles.stream().map(DetalleVenta::getProducto).toList();
    }

    public List<Integer> getCantidades() {
        return detalles.stream().map(DetalleVenta::getCantidad).toList();
    }

    public List<Double> getPreciosUnitarios() {
        return detalles.stream().map(DetalleVenta::getPrecioUnitario).toList();
    }
    // Colección inmodificable de detalles.
    public List<DetalleVenta> getDetalles() {
        return List.copyOf(detalles);
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
