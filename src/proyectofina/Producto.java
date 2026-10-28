/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectofina;

/**
 * Producto artesanal. El modelo permite un codigo unico por modelo (historia de usuario S08).
 *
 * @author Alvaro
 */
public class Producto {
    private String idProducto;
    private String nombre;
    private String modelo;
    private String categoria;
    private double precio;
    private String descripcion;

    public Producto(String idProducto, String nombre, String modelo, String categoria, double precio, String descripcion) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.modelo = modelo;
        this.categoria = categoria;
        setPrecio(precio);
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        this.precio = precio;
    }

    public String mostrarInfo() {
        return String.format("%s - %s (modelo %s, %s) S/ %.2f", idProducto, nombre, modelo, categoria, precio);
    }

    public String getIdProducto() {
        return idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
