package proyectofina;

import java.util.Date;

/**
 * Producto en inventario (hereda de Producto): agrega stock, costo y fecha de ingreso.
 *
 * @author Rodrigo
 */
public class Inventario extends Producto {
    private int stock;
    private int stockMinimo;
    private double costo;
    private Date fechaIngreso;

    /** Crea un producto con sus existencias; el stock inicial se valida. */
    public Inventario(String idProducto, String nombre, String modelo, String categoria, double precio,
            String descripcion, int stock, int stockMinimo, double costo, Date fechaIngreso) {
        super(idProducto, nombre, modelo, categoria, precio, descripcion);
        actualizarStock(stock);
        this.stockMinimo = stockMinimo;
        this.costo = costo;
        this.fechaIngreso = fechaIngreso;
    }

    /** Cambia el stock; nunca permite valores negativos. */
    public void actualizarStock(int nuevoStock) {
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        this.stock = nuevoStock;
    }

    /** Precio de venta menos costo por pieza. */
    public double calcularGananciaUnitaria() {
        return getPrecio() - costo;
    }

    /** Indica si queda al menos una unidad. */
    public boolean estaDisponible() {
        return stock > 0;
    }
    // Alerta de stock bajo (historia de usuario del supervisor de almacen)
    public boolean tieneStockBajo() {
        return stock <= stockMinimo;
    }

    /** Sobrescribe Producto.mostrarInfo() agregando el stock y la alerta (polimorfismo). */
    @Override
    public String mostrarInfo() {
        return super.mostrarInfo() + " | Stock: " + stock + (tieneStockBajo() ? "  <-- STOCK BAJO" : "");
    }

    // ---------- Getters y setters ----------
    public int getStock() {
        return stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
}
