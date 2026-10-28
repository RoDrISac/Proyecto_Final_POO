/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectofina;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Venta realizada por un vendedor a un cliente. Las tres listas van en paralelo:
 * la posicion i de cada una corresponde al mismo producto vendido.
 *
 * @author Alvaro
 */
public class Venta {
    private String idVenta;
    private Date fecha;
    private String hora;
    private Cliente cliente;
    private Usuario vendedor;
    private List<Producto> productosVendidos = new ArrayList<>();
    private List<Integer> cantidades = new ArrayList<>();
    private List<Double> preciosUnitarios = new ArrayList<>();
    private int cantidadProductos;
    private double montoTotal;
    private String metodoPago;
    private String tipoComprobante;
    private String estado = "Pendiente";

    public Venta(String idVenta, Date fecha, Cliente cliente, Usuario vendedor, String metodoPago, String tipoComprobante) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.hora = new SimpleDateFormat("HH:mm:ss").format(fecha);
        this.cliente = cliente;
        this.vendedor = vendedor;
        this.metodoPago = metodoPago;
        this.tipoComprobante = tipoComprobante;
    }

    public void agregarProducto(Producto producto, int cantidad, double precio) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        // Se agrega en las tres listas a la vez para que no se desordenen
        productosVendidos.add(producto);
        cantidades.add(cantidad);
        preciosUnitarios.add(precio);
        cantidadProductos += cantidad;
        calcularMontoTotal();
    }

    public double calcularMontoTotal() {
        double suma = 0;
        for (int i = 0; i < productosVendidos.size(); i++) {
            suma += cantidades.get(i) * preciosUnitarios.get(i);
        }
        montoTotal = suma;
        return montoTotal;
    }

    public int getCantidadProductos() {
        return cantidadProductos;
    }

    public String getResumenVenta() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Venta %s | %s %s | Cliente: %s | Vendedor: %s%n", idVenta,
                new SimpleDateFormat("dd/MM/yyyy").format(fecha), hora,
                cliente.getNombreCompleto(), vendedor.getNombreCompleto()));
        for (int i = 0; i < productosVendidos.size(); i++) {
            sb.append(String.format("   %-22s x%-3d S/ %8.2f = S/ %9.2f%n", productosVendidos.get(i).getNombre(),
                    cantidades.get(i), preciosUnitarios.get(i), cantidades.get(i) * preciosUnitarios.get(i)));
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
        return Collections.unmodifiableList(productosVendidos);
    }

    public List<Integer> getCantidades() {
        return Collections.unmodifiableList(cantidades);
    }

    public List<Double> getPreciosUnitarios() {
        return Collections.unmodifiableList(preciosUnitarios);
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
