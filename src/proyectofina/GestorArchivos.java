/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectofina;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Manejo de archivos (S09): guarda y carga usuarios, clientes, inventario y ventas
 * en archivos de texto separados por ";" dentro de la carpeta "datos".
 *
 * @author Rodrigo
 */
public class GestorArchivos {

    public static final String CARPETA_DATOS = "datos";
    public static final String CARPETA_COMPROBANTES = CARPETA_DATOS + File.separator + "comprobantes";
    public static final String CARPETA_REPORTES = CARPETA_DATOS + File.separator + "reportes";
    private static final String SEP = ";";

    public static void crearCarpetas() {
        new File(CARPETA_COMPROBANTES).mkdirs();
        new File(CARPETA_REPORTES).mkdirs();
    }

    // ---------- Usuarios: id;nombre;apellido;dni;rol;contraseña ----------
    public static void guardarUsuarios(List<Usuario> usuarios) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Usuario u : usuarios) {
            lineas.add(unir(u.getIdUsuario(), u.getNombre(), u.getApellido(), u.getDni(),
                    u.getRol().getNombreRol(), u.getContraseña()));
        }
        escribirLineas(archivo("usuarios.csv"), lineas);
    }

    public static List<Usuario> cargarUsuarios(List<Rol> roles) throws IOException {
        List<Usuario> usuarios = new ArrayList<>();
        for (String[] c : leer("usuarios.csv", 6)) {
            Rol rol = buscarRol(roles, c[4]);
            if (rol != null) {
                usuarios.add(new Usuario(c[0], c[1], c[2], c[3], rol, c[5]));
            }
        }
        return usuarios;
    }

    // ---------- Clientes: id;nombre;apellido;dni;direccion;telefono;correo ----------
    public static void guardarClientes(List<Cliente> clientes) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Cliente c : clientes) {
            lineas.add(unir(c.getIdCliente(), c.getNombre(), c.getApellido(), c.getDni(),
                    c.getDireccion(), c.getTelefono(), c.getCorreoElectronico()));
        }
        escribirLineas(archivo("clientes.csv"), lineas);
    }

    public static List<Cliente> cargarClientes() throws IOException {
        List<Cliente> clientes = new ArrayList<>();
        for (String[] c : leer("clientes.csv", 7)) {
            clientes.add(new Cliente(c[0], c[1], c[2], c[3], c[4], c[5], c[6]));
        }
        return clientes;
    }

    // ---------- Inventario: id;nombre;modelo;categoria;precio;descripcion;stock;stockMin;costo;fechaIngreso ----------
    public static void guardarInventario(List<Inventario> inventario) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Inventario p : inventario) {
            lineas.add(unir(p.getIdProducto(), p.getNombre(), p.getModelo(), p.getCategoria(),
                    String.valueOf(p.getPrecio()), p.getDescripcion(), String.valueOf(p.getStock()),
                    String.valueOf(p.getStockMinimo()), String.valueOf(p.getCosto()),
                    String.valueOf(p.getFechaIngreso().getTime())));
        }
        escribirLineas(archivo("inventario.csv"), lineas);
    }

    public static List<Inventario> cargarInventario() throws IOException {
        List<Inventario> inventario = new ArrayList<>();
        for (String[] c : leer("inventario.csv", 10)) {
            inventario.add(new Inventario(c[0], c[1], c[2], c[3], Double.parseDouble(c[4]), c[5],
                    Integer.parseInt(c[6]), Integer.parseInt(c[7]), Double.parseDouble(c[8]),
                    new Date(Long.parseLong(c[9]))));
        }
        return inventario;
    }

    // ---------- Ventas: id;fecha;idCliente;idVendedor;pago;comprobante;estado;idProd:nombre:cant:precio|... ----------
    public static void guardarVentas(List<Venta> ventas) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Venta v : ventas) {
            List<String> items = new ArrayList<>();
            for (int i = 0; i < v.getProductosVendidos().size(); i++) {
                Producto p = v.getProductosVendidos().get(i);
                items.add(p.getIdProducto() + ":" + p.getNombre().replace(":", "-").replace("|", "/") + ":"
                        + v.getCantidades().get(i) + ":" + v.getPreciosUnitarios().get(i));
            }
            lineas.add(unir(v.getIdVenta(), String.valueOf(v.getFecha().getTime()), v.getCliente().getIdCliente(),
                    v.getVendedor().getIdUsuario(), v.getMetodoPago(), v.getTipoComprobante(), v.getEstado())
                    + SEP + String.join("|", items));
        }
        escribirLineas(archivo("ventas.csv"), lineas);
    }

    public static List<Venta> cargarVentas(List<Cliente> clientes, List<Usuario> usuarios, List<Inventario> inventario) throws IOException {
        List<Venta> ventas = new ArrayList<>();
        for (String[] c : leer("ventas.csv", 8)) {
            Cliente cliente = buscarCliente(clientes, c[2]);
            Usuario vendedor = buscarUsuario(usuarios, c[3]);
            if (cliente == null || vendedor == null) {
                System.out.println("Aviso: la venta " + c[0] + " tiene cliente o vendedor inexistente y no se cargo.");
                continue;
            }
            Venta venta = new Venta(c[0], new Date(Long.parseLong(c[1])), cliente, vendedor, c[4], c[5]);
            for (String item : c[7].split("\\|")) {
                String[] d = item.split(":");
                Producto producto = buscarProducto(inventario, d[0]);
                if (producto == null) { // producto eliminado: se conserva el dato historico de la venta
                    producto = new Producto(d[0], d[1], "-", "-", Double.parseDouble(d[3]), "(eliminado)");
                }
                venta.agregarProducto(producto, Integer.parseInt(d[2]), Double.parseDouble(d[3]));
            }
            venta.setEstado(c[6]);
            cliente.obtenerHistorialVentas().add(venta);
            ventas.add(venta);
        }
        return ventas;
    }

    // ---------- Utilidades ----------
    public static void escribirTexto(File archivo, String texto) throws IOException {
        archivo.getParentFile().mkdirs();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
            bw.write(texto);
        }
    }

    private static void escribirLineas(File archivo, List<String> lineas) throws IOException {
        archivo.getParentFile().mkdirs();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
            for (String linea : lineas) {
                bw.write(linea);
                bw.newLine();
            }
        }
    }

    private static List<String[]> leer(String nombre, int columnas) throws IOException {
        List<String[]> filas = new ArrayList<>();
        File archivo = archivo(nombre);
        if (!archivo.exists()) {
            return filas; // primera ejecucion: todavia no hay datos guardados
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
            String linea;
            int numero = 0;
            while ((linea = br.readLine()) != null) {
                numero++;
                if (linea.isBlank()) {
                    continue;
                }
                String[] c = linea.split(SEP, -1);
                if (c.length < columnas) {
                    System.out.println("Aviso: linea " + numero + " de " + nombre + " incompleta, se omite.");
                    continue;
                }
                filas.add(c);
            }
        }
        return filas;
    }

    private static File archivo(String nombre) {
        return new File(CARPETA_DATOS, nombre);
    }

    // Une los campos con ";" quitando ese caracter de los textos para no romper las columnas
    private static String unir(String... campos) {
        List<String> limpios = new ArrayList<>();
        for (String c : campos) {
            limpios.add(c == null ? "" : c.replace(SEP, ",").replace("\n", " "));
        }
        return String.join(SEP, limpios);
    }

    private static Rol buscarRol(List<Rol> roles, String nombre) {
        for (Rol r : roles) {
            if (r.getNombreRol().equals(nombre)) {
                return r;
            }
        }
        return null;
    }

    private static Cliente buscarCliente(List<Cliente> clientes, String id) {
        for (Cliente c : clientes) {
            if (c.getIdCliente().equals(id)) {
                return c;
            }
        }
        return null;
    }

    private static Usuario buscarUsuario(List<Usuario> usuarios, String id) {
        for (Usuario u : usuarios) {
            if (u.getIdUsuario().equals(id)) {
                return u;
            }
        }
        return null;
    }

    private static Inventario buscarProducto(List<Inventario> inventario, String id) {
        for (Inventario p : inventario) {
            if (p.getIdProducto().equals(id)) {
                return p;
            }
        }
        return null;
    }
}
