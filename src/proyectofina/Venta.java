package proyectofina;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
    public Venta(String idVenta, Date fecha, Cliente cliente, Usuario vendedor, String metodoPago, String tipoComprobante) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.hora = new SimpleDateFormat("HH:mm:ss").format(fecha);
        this.cliente = new Cliente(cliente.getIdCliente(), cliente.getNombre(), cliente.getApellido(), cliente.getDni(), cliente.getDireccion(), cliente.getTelefono(), cliente.getCorreoElectronico());
        this.vendedor = new Usuario(vendedor.getIdUsuario(), vendedor.getNombre(), vendedor.getApellido(), vendedor.getDni(), vendedor.getRol(), "");
        this.metodoPago = metodoPago;
        this.tipoComprobante = tipoComprobante;
    }
    public void agregarProducto(Producto producto, int cantidad, double precio) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        // La venta es dueña de sus detalles históricos.
        if (!Double.isFinite(precio) || precio < 0) throw new IllegalArgumentException("Precio inválido");
        detalles.add(new DetalleVenta(producto, cantidad, precio));
        cantidadProductos += cantidad;
        calcularMontoTotal();
    }
    public double calcularMontoTotal() {
        java.math.BigDecimal suma = java.math.BigDecimal.ZERO;
        for (DetalleVenta d : detalles) suma = suma.add(java.math.BigDecimal.valueOf(d.getPrecioUnitario()).multiply(java.math.BigDecimal.valueOf(d.getCantidad())));
        montoTotal = suma.setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
        return montoTotal;
    }
    public int getCantidadProductos() {
        return cantidadProductos;
    }
    public String getResumenVenta() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Venta %s | %s %s | Cliente: %s | Vendedor: %s%n", idVenta,                 new SimpleDateFormat("dd/MM/yyyy").format(fecha), hora,                 cliente.getNombreCompleto(), vendedor.getNombreCompleto()));
        for (DetalleVenta d : getDetalles()) {
            sb.append(String.format("   %-22s x%-3d S/ %8.2f = S/ %9.2f%n", d.getProducto().getNombre(),                     d.getCantidad(), d.getPrecioUnitario(), d.calcularSubtotal()));
        }
        sb.append(String.format("   Productos: %d | Total: S/ %.2f | Pago: %s | %s | Estado: %s",                 cantidadProductos, montoTotal, metodoPago, tipoComprobante, estado));
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
