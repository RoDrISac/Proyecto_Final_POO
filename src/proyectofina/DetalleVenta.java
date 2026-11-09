package proyectofina;
/**
* Linea del detalle de una venta: producto, cantidad y precio unitario.
*
* @author Rodrigo
*/
public class DetalleVenta implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final Producto producto;
    private final int cantidad;
    private final double precioUnitario;
    public DetalleVenta(Producto producto, int cantidad, double precioUnitario) {
        this.producto = new Producto(producto.getIdProducto(), producto.getNombre(), producto.getModelo(), producto.getCategoria(), precioUnitario, producto.getDescripcion());
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }
    public double calcularSubtotal() {
        return cantidad * precioUnitario;
    }
    public Producto getProducto() {
        return new Producto(producto.getIdProducto(), producto.getNombre(), producto.getModelo(), producto.getCategoria(), precioUnitario, producto.getDescripcion());
    }
    public int getCantidad() {
        return cantidad;
    }
    public double getPrecioUnitario() {
        return precioUnitario;
    }
}
