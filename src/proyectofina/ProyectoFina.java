/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package proyectofina;

import java.awt.Desktop;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Sistema de ventas e inventario "Colores y Creaciones" (menu principal).
 */
public class ProyectoFina {
    static Scanner scanner = new Scanner(System.in);
    static List<Rol> roles = new ArrayList<>();
    static List<Usuario> usuarios = new ArrayList<>();
    static List<Cliente> clientes = new ArrayList<>();
    static List<Inventario> inventario = new ArrayList<>();
    static List<Venta> ventas = new ArrayList<>();
    static Usuario usuarioActual;

    public static void main(String[] args) {
        inicializarRoles();
        cargarDatos();
        if (!iniciarSesion()) {
            System.out.println("Demasiados intentos fallidos. Saliendo del sistema...");
            return;
        }
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opcion: ", 0);
            try {
                switch (opcion) {
                    case 1 -> { if (tienePermiso("INVENTARIO")) registrarProducto(); }
                    case 2 -> { if (tienePermiso("INVENTARIO")) editarProducto(); }
                    case 3 -> { if (tienePermiso("INVENTARIO")) eliminarProducto(); }
                    case 4 -> { if (tienePermiso("INVENTARIO")) ingresarStock(); }
                    case 5 -> mostrarInventario();
                    case 6 -> buscarProductoPorCodigo();
                    case 7 -> { if (tienePermiso("VENTAS")) registrarCliente(); }
                    case 8 -> { if (tienePermiso("VENTAS")) mostrarPersonas(ordenarClientes()); }
                    case 9 -> { if (tienePermiso("VENTAS")) realizarVenta(); }
                    case 10 -> { if (tienePermiso("VENTAS")) mostrarVentas(); }
                    case 11 -> { if (tienePermiso("VENTAS")) mostrarHistorialCliente(); }
                    case 12 -> { if (tienePermiso("VENTAS")) reimprimirComprobante(); }
                    case 13 -> { if (tienePermiso("REPORTES")) generarReporte(); }
                    case 14 -> { if (tienePermiso("USUARIOS")) mostrarPersonas(usuarios); }
                    case 15 -> { if (tienePermiso("USUARIOS")) registrarUsuario(); }
                    case 16 -> { if (tienePermiso("USUARIOS")) cambiarRolUsuario(); }
                    case 0 -> {
                        guardarDatos();
                        System.out.println("Datos guardados. Hasta pronto, " + usuarioActual.getNombre() + ".");
                    }
                    default -> System.out.println("Opcion invalida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: se esperaba un numero valido.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    static void mostrarMenu() {
        System.out.println("\n========== COLORES Y CREACIONES ==========");
        System.out.println("Usuario: " + usuarioActual.getNombreCompleto() + " (" + usuarioActual.getRol().getNombreRol() + ")");
        System.out.println("--- Inventario ---");
        System.out.println(" 1. Registrar producto");
        System.out.println(" 2. Editar producto");
        System.out.println(" 3. Eliminar producto");
        System.out.println(" 4. Ingreso de stock (produccion o compra)");
        System.out.println(" 5. Ver inventario");
        System.out.println(" 6. Buscar producto por codigo (lector de barras)");
        System.out.println("--- Clientes y ventas ---");
        System.out.println(" 7. Registrar cliente");
        System.out.println(" 8. Ver clientes");
        System.out.println(" 9. Realizar venta");
        System.out.println("10. Ver ventas");
        System.out.println("11. Historial de compras de un cliente");
        System.out.println("12. Reimprimir comprobante");
        System.out.println("--- Reportes ---");
        System.out.println("13. Generar reporte");
        System.out.println("--- Usuarios ---");
        System.out.println("14. Ver usuarios");
        System.out.println("15. Registrar usuario");
        System.out.println("16. Cambiar rol de un usuario");
        System.out.println(" 0. Salir");
    }

    // ---------------- Inicio y datos ----------------
    static void inicializarRoles() {
        roles.add(new Rol("Administrador", Arrays.asList("INVENTARIO", "VENTAS", "REPORTES", "USUARIOS"), "ADM-2026"));
        roles.add(new Rol("Vendedor", Arrays.asList("VENTAS"), "VEN-2026"));
        roles.add(new Rol("Supervisor de almacen", Arrays.asList("INVENTARIO", "REPORTES"), "SUP-2026"));
    }

    static void cargarDatos() {
        GestorArchivos.crearCarpetas();
        try {
            usuarios = GestorArchivos.cargarUsuarios(roles);
            clientes = GestorArchivos.cargarClientes();
            inventario = GestorArchivos.cargarInventario();
            ventas = GestorArchivos.cargarVentas(clientes, usuarios, inventario);
        } catch (IOException e) {
            // Si se siguiera con listas vacias, el siguiente guardado borraria los archivos
            System.out.println("No se pudieron leer los datos guardados: " + e.getMessage());
            System.out.println("Cierre los programas que usen la carpeta " + GestorArchivos.CARPETA_DATOS
                    + " y vuelva a intentarlo. No se modifico ningun archivo.");
            System.exit(1);
        }
        if (usuarios.isEmpty()) { // primera ejecucion: usuarios iniciales
            usuarios.add(new Usuario("U001", "Admin", "Principal", "00000001", roles.get(0), "1234"));
            usuarios.add(new Usuario("U002", "Vendedor", "Tienda", "00000002", roles.get(1), "1234"));
            usuarios.add(new Usuario("U003", "Supervisor", "Almacen", "00000003", roles.get(2), "1234"));
            guardarDatos();
        }
        System.out.println("Datos cargados: " + inventario.size() + " productos, " + clientes.size()
                + " clientes, " + ventas.size() + " ventas, " + usuarios.size() + " usuarios.");
    }

    static void guardarDatos() {
        try {
            GestorArchivos.guardarUsuarios(usuarios);
            GestorArchivos.guardarClientes(clientes);
            GestorArchivos.guardarInventario(inventario);
            GestorArchivos.guardarVentas(ventas);
        } catch (IOException e) {
            System.out.println("Error al guardar los datos: " + e.getMessage());
        }
    }

    static boolean iniciarSesion() {
        System.out.println("=== INICIO DE SESION ===");
        for (int intento = 1; intento <= 3; intento++) {
            String codigo = leer("Codigo de usuario: ");
            String clave = leer("Contrasena: ");
            Usuario u = buscarUsuario(codigo);
            if (u != null && u.validarAcceso(clave)) {
                usuarioActual = u;
                System.out.println("Bienvenido(a), " + u.getNombreCompleto() + ".");
                return true;
            }
            System.out.println("Codigo o contrasena incorrectos (intento " + intento + " de 3).");
        }
        return false;
    }

    static boolean tienePermiso(String permiso) {
        if (usuarioActual.getRol().tenerPermiso(permiso)) {
            return true;
        }
        System.out.println("Acceso denegado: el rol " + usuarioActual.getRol().getNombreRol()
                + " no tiene permiso de " + permiso + ".");
        return false;
    }

    // ---------------- Inventario ----------------
    static void registrarProducto() {
        System.out.println("--- Registrar producto ---");
        String id = leer("Codigo del producto: ");
        if (!id.matches("[A-Za-z0-9_-]+")) {
            System.out.println("El codigo es obligatorio y solo puede tener letras, numeros, - y _.");
            return;
        }
        if (buscarProducto(id) != null) {
            System.out.println("Ya existe un producto con ese codigo.");
            return;
        }
        String nombre = leer("Nombre: ");
        if (nombre.isBlank()) {
            System.out.println("El nombre es obligatorio.");
            return;
        }
        String modelo = leer("Modelo: ");
        String categoria = leer("Categoria: ");
        String descripcion = leer("Descripcion: ");
        double precio = leerDecimal("Precio de venta (S/): ", 0);
        double costo = leerDecimal("Costo de produccion (S/): ", 0);
        int stock = leerEntero("Stock inicial: ", 0);
        int stockMinimo = leerEntero("Stock minimo: ", 0);
        Inventario p = new Inventario(id, nombre, modelo, categoria, precio, descripcion, stock, stockMinimo, costo, new Date());
        inventario.add(p);
        guardarDatos();
        System.out.println("Producto registrado: " + p.mostrarInfo());
    }

    static void editarProducto() {
        Inventario p = pedirProducto();
        if (p == null) {
            return;
        }
        System.out.println("Deje vacio para mantener el valor actual.");
        // Primero se leen y validan todos los datos; si alguno falla, el producto no cambia
        String nombre = leerOpcional("Nombre", p.getNombre());
        String modelo = leerOpcional("Modelo", p.getModelo());
        String categoria = leerOpcional("Categoria", p.getCategoria());
        String descripcion = leerOpcional("Descripcion", p.getDescripcion());
        double precio = leerOpcionalDecimal("Precio", p.getPrecio());
        double costo = leerOpcionalDecimal("Costo", p.getCosto());
        int stockMinimo = Integer.parseInt(leerOpcional("Stock minimo", String.valueOf(p.getStockMinimo())));
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (precio < 0 || costo < 0 || stockMinimo < 0) {
            throw new IllegalArgumentException("Precio, costo y stock minimo no pueden ser negativos.");
        }
        p.setNombre(nombre);
        p.setModelo(modelo);
        p.setCategoria(categoria);
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setCosto(costo);
        p.setStockMinimo(stockMinimo);
        guardarDatos();
        System.out.println("Producto actualizado: " + p.mostrarInfo());
    }

    static void eliminarProducto() {
        Inventario p = pedirProducto();
        if (p == null) {
            return;
        }
        if (leer("Eliminar " + p.getNombre() + "? (s/n): ").equalsIgnoreCase("s")) {
            inventario.remove(p);
            guardarDatos();
            System.out.println("Producto eliminado. Las ventas anteriores conservan su registro.");
        }
    }

    static void ingresarStock() {
        Inventario p = pedirProducto();
        if (p == null) {
            return;
        }
        int cantidad = leerEntero("Cantidad que ingresa: ", 1);
        double costo = leerOpcionalDecimal("Costo unitario", p.getCosto());
        if (costo < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo.");
        }
        p.setCosto(costo);
        p.actualizarStock(p.getStock() + cantidad);
        p.setFechaIngreso(new Date());
        guardarDatos();
        System.out.println("Ingreso registrado. " + p.mostrarInfo());
    }

    static void mostrarInventario() {
        System.out.println("--- Inventario ---");
        if (inventario.isEmpty()) {
            System.out.println("No hay productos registrados.");
        }
        int bajos = 0;
        for (Inventario p : inventario) {
            System.out.println(p.mostrarInfo());
            if (p.tieneStockBajo()) {
                bajos++;
            }
        }
        if (bajos > 0) {
            System.out.println("ALERTA: " + bajos + " producto(s) con stock bajo.");
        }
    }

    // Un lector de codigo de barras escribe el codigo y presiona Enter, igual que el teclado
    static void buscarProductoPorCodigo() {
        String codigo = leer("Escanee o escriba el codigo: ");
        Inventario p = buscarProducto(codigo);
        if (p == null) {
            System.out.println("No existe un producto con el codigo " + codigo + ".");
            return;
        }
        System.out.println(p.mostrarInfo());
        System.out.println("Descripcion: " + p.getDescripcion());
        System.out.println(String.format("Disponible: %s | Ganancia unitaria: S/ %.2f",
                p.estaDisponible() ? "si" : "no", p.calcularGananciaUnitaria()));
    }

    // ---------------- Clientes ----------------
    static void registrarCliente() {
        System.out.println("--- Registrar cliente ---");
        String id = leer("Codigo del cliente: ");
        if (buscarCliente(id) != null) {
            System.out.println("Ya existe un cliente con ese codigo.");
            return;
        }
        Cliente c = new Cliente(id, leer("Nombre: "), leer("Apellido: "), leer("DNI: "),
                leer("Direccion: "), leer("Telefono: "), leer("Correo electronico: "));
        c.registrarCliente(); // valida los datos; si hay error lanza IllegalArgumentException
        for (Cliente otro : clientes) {
            if (otro.getDni().equals(c.getDni())) {
                System.out.println("Ya existe un cliente con el DNI " + c.getDni() + ".");
                return;
            }
        }
        clientes.add(c);
        guardarDatos();
        System.out.println("Cliente registrado: " + c.mostrarPersona());
    }

    static List<Cliente> ordenarClientes() {
        clientes.sort(Comparator.comparing(Cliente::getApellido).thenComparing(Cliente::getNombre));
        return clientes;
    }

    // Polimorfismo: cada clase hija (Cliente o Usuario) muestra sus propios datos
    static void mostrarPersonas(List<? extends Persona> personas) {
        if (personas.isEmpty()) {
            System.out.println("No hay registros.");
        }
        for (Persona p : personas) {
            System.out.println(p.mostrarPersona());
        }
    }

    static void mostrarHistorialCliente() {
        Cliente c = pedirCliente();
        if (c == null) {
            return;
        }
        double total = 0;
        for (Venta v : c.obtenerHistorialVentas()) {
            System.out.println(v.getResumenVenta());
            total += v.getMontoTotal();
        }
        System.out.println(String.format("Compras: %d | Total comprado: S/ %.2f", c.obtenerHistorialVentas().size(), total));
    }

    // ---------------- Ventas ----------------
    static void realizarVenta() {
        System.out.println("--- Realizar venta ---");
        if (clientes.isEmpty() || inventario.isEmpty()) {
            System.out.println("Debe registrar clientes y productos primero.");
            return;
        }
        Cliente cliente = pedirCliente();
        if (cliente == null) {
            return;
        }
        String metodoPago = elegir("Metodo de pago", "Efectivo", "Yape", "Plin", "Transferencia", "Tarjeta");
        String tipoComprobante = elegir("Comprobante", "Boleta", "Recibo");
        Venta venta = new Venta(siguienteIdVenta(), new Date(), cliente, usuarioActual, metodoPago, tipoComprobante);

        Map<Inventario, Integer> separados = new HashMap<>();
        while (true) {
            String codigo = leer("Codigo del producto (escanee o escriba; Enter para terminar): ");
            if (codigo.isBlank()) {
                break;
            }
            Inventario p = buscarProducto(codigo);
            if (p == null) {
                System.out.println("No existe un producto con ese codigo.");
                continue;
            }
            int disponible = p.getStock() - separados.getOrDefault(p, 0);
            System.out.println(p.mostrarInfo() + " | Disponible: " + disponible);
            if (disponible <= 0) {
                System.out.println("Producto sin stock disponible.");
                continue;
            }
            int cantidad = leerEntero("Cantidad: ", 1);
            if (cantidad > disponible) {
                System.out.println("Stock insuficiente. Disponible: " + disponible);
                continue;
            }
            venta.agregarProducto(p, cantidad, p.getPrecio());
            separados.merge(p, cantidad, Integer::sum);
            System.out.println(String.format("Agregado. Subtotal de la venta: S/ %.2f", venta.getMontoTotal()));
        }
        if (venta.getProductosVendidos().isEmpty()) {
            System.out.println("No se agrego ningun producto. Venta cancelada.");
            return;
        }
        System.out.println(venta.getResumenVenta());
        if (!leer("Confirmar venta? (s/n): ").equalsIgnoreCase("s")) {
            System.out.println("Venta cancelada. No se modifico el stock.");
            return;
        }
        // Se descuenta el stock solo cuando la venta se confirma
        for (Map.Entry<Inventario, Integer> e : separados.entrySet()) {
            Inventario p = e.getKey();
            p.actualizarStock(p.getStock() - e.getValue());
            if (p.tieneStockBajo()) {
                System.out.println("ALERTA: stock bajo de " + p.getNombre() + " (quedan " + p.getStock()
                        + ", minimo " + p.getStockMinimo() + ").");
            }
        }
        ventas.add(venta);
        cliente.obtenerHistorialVentas().add(venta);
        Comprobante comprobante = emitirComprobante(venta);
        guardarDatos();
        if (!cliente.getCorreoElectronico().isBlank()
                && leer("Enviar comprobante a " + cliente.getCorreoElectronico() + "? (s/n): ").equalsIgnoreCase("s")) {
            comprobante.enviarPorCorreo(cliente.getCorreoElectronico());
        }
    }

    static Comprobante emitirComprobante(Venta venta) {
        Comprobante comprobante = new Comprobante(venta);
        try {
            comprobante.emitirPDF();
            venta.setEstado("Concluido");
            System.out.println("Comprobante " + comprobante.getTipo() + " " + comprobante.getIdComprobante()
                    + " emitido en: " + comprobante.getArchivo().getPath());
            abrirPDF(comprobante);
        } catch (IOException e) {
            System.out.println("La venta se registro, pero no se pudo crear el PDF: " + e.getMessage());
        }
        return comprobante;
    }

    // Abre el PDF con el visor predeterminado del sistema; si no se puede, basta con la ruta ya mostrada
    static void abrirPDF(Comprobante comprobante) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(comprobante.getArchivo());
            } else {
                System.out.println("No se puede abrir el PDF automaticamente; abralo desde la ruta indicada.");
            }
        } catch (IOException e) {
            System.out.println("No se pudo abrir el PDF: " + e.getMessage());
        }
    }

    static void mostrarVentas() {
        System.out.println("--- Ventas ---");
        if (ventas.isEmpty()) {
            System.out.println("No hay ventas registradas.");
        }
        for (Venta v : ventas) {
            System.out.println(v.getResumenVenta());
        }
    }

    static void reimprimirComprobante() {
        String id = leer("Codigo de la venta (ej. V0001): ");
        for (Venta v : ventas) {
            if (v.getIdVenta().equalsIgnoreCase(id)) {
                emitirComprobante(v);
                guardarDatos();
                return;
            }
        }
        System.out.println("No existe la venta " + id + ".");
    }

    static String siguienteIdVenta() {
        int mayor = 0;
        for (Venta v : ventas) {
            mayor = Math.max(mayor, Integer.parseInt(v.getIdVenta().substring(1)));
        }
        return String.format("V%04d", mayor + 1);
    }

    // ---------------- Reportes ----------------
    static void generarReporte() {
        String tipo = elegir("Tipo de reporte", "INVENTARIO", "STOCK_BAJO", "VENTAS", "MAS_VENDIDOS");
        Reporte reporte = new Reporte(ventas, inventario).generar(tipo);
        System.out.println();
        System.out.println(reporte.getContenido());
        String formato = elegir("Exportar", "PDF", "TXT", "No exportar");
        if (!formato.equals("No exportar")) {
            try {
                reporte.exportar(formato);
            } catch (IOException e) {
                System.out.println("No se pudo exportar el reporte: " + e.getMessage());
            }
        }
    }

    // ---------------- Usuarios ----------------
    static void registrarUsuario() {
        System.out.println("--- Registrar usuario ---");
        String id = leer("Codigo unico de ingreso: ");
        if (id.isBlank() || buscarUsuario(id) != null) {
            System.out.println("El codigo esta vacio o ya existe.");
            return;
        }
        String nombre = leer("Nombre: ");
        String apellido = leer("Apellido: ");
        if (nombre.isBlank() || apellido.isBlank()) {
            System.out.println("El nombre y el apellido son obligatorios.");
            return;
        }
        String dni = leer("DNI: ");
        if (!dni.matches("\\d{8}")) {
            System.out.println("El DNI debe tener 8 digitos.");
            return;
        }
        for (Usuario otro : usuarios) {
            if (otro.getDni().equals(dni)) {
                System.out.println("Ya existe un usuario con el DNI " + dni + ".");
                return;
            }
        }
        Rol rol = pedirRol();
        if (rol == null) {
            return;
        }
        String clave = leer("Contrasena del nuevo usuario (minimo 4 caracteres): ");
        if (clave.length() < 4) {
            System.out.println("La contrasena es muy corta.");
            return;
        }
        Usuario u = new Usuario(id, nombre, apellido, dni, rol, clave);
        usuarios.add(u);
        guardarDatos();
        System.out.println("Usuario registrado: " + u.mostrarPersona());
    }

    static void cambiarRolUsuario() {
        Usuario u = buscarUsuario(leer("Codigo del usuario: "));
        if (u == null) {
            System.out.println("No existe ese usuario.");
            return;
        }
        if (u == usuarioActual) {
            System.out.println("No puede cambiar su propio rol.");
            return;
        }
        Rol rol = pedirRol();
        if (rol != null) {
            u.cambiarRol(rol);
            guardarDatos();
            System.out.println("Rol actualizado: " + u.mostrarPersona());
        }
    }

    // Para asignar un rol se pide la contraseña de ese rol
    static Rol pedirRol() {
        String nombre = elegir("Rol", roles.stream().map(Rol::getNombreRol).toArray(String[]::new));
        Rol rol = roles.stream().filter(r -> r.getNombreRol().equals(nombre)).findFirst().orElseThrow();
        if (!rol.validarContraseñaRol(leer("Contrasena del rol " + nombre + ": "))) {
            System.out.println("Contrasena del rol incorrecta.");
            return null;
        }
        return rol;
    }

    // ---------------- Busquedas ----------------
    static Inventario pedirProducto() {
        String codigo = leer("Codigo del producto: ");
        Inventario p = buscarProducto(codigo);
        if (p == null) {
            System.out.println("No existe un producto con el codigo " + codigo + ".");
        }
        return p;
    }

    static Cliente pedirCliente() {
        String dato = leer("Codigo o DNI del cliente (Enter para ver la lista): ");
        if (dato.isBlank()) {
            mostrarPersonas(ordenarClientes());
            dato = leer("Codigo o DNI del cliente: ");
        }
        for (Cliente c : clientes) {
            if (c.getIdCliente().equalsIgnoreCase(dato) || c.getDni().equals(dato)) {
                return c;
            }
        }
        System.out.println("No se encontro el cliente " + dato + ".");
        return null;
    }

    static Inventario buscarProducto(String codigo) {
        for (Inventario p : inventario) {
            if (p.getIdProducto().equalsIgnoreCase(codigo)) {
                return p;
            }
        }
        return null;
    }

    static Cliente buscarCliente(String id) {
        for (Cliente c : clientes) {
            if (c.getIdCliente().equalsIgnoreCase(id)) {
                return c;
            }
        }
        return null;
    }

    static Usuario buscarUsuario(String id) {
        for (Usuario u : usuarios) {
            if (u.getIdUsuario().equalsIgnoreCase(id)) {
                return u;
            }
        }
        return null;
    }

    // ---------------- Lectura de datos con manejo de errores ----------------
    static String leer(String mensaje) {
        System.out.print(mensaje);
        if (!scanner.hasNextLine()) { // la entrada termino (por ejemplo, al cerrar la consola)
            guardarDatos();
            System.out.println("\nFin de la entrada. Datos guardados.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    static int leerEntero(String mensaje, int minimo) {
        while (true) {
            try {
                int valor = Integer.parseInt(leer(mensaje));
                if (valor >= minimo) {
                    return valor;
                }
                System.out.println("El valor debe ser mayor o igual a " + minimo + ".");
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero entero valido.");
            }
        }
    }

    static double leerDecimal(String mensaje, double minimo) {
        while (true) {
            try {
                double valor = Double.parseDouble(leer(mensaje).replace(',', '.'));
                if (valor >= minimo && !Double.isInfinite(valor)) {
                    return valor;
                }
                System.out.println("El valor debe ser mayor o igual a " + minimo + ".");
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero valido (ej. 25.50).");
            }
        }
    }

    static String leerOpcional(String campo, String actual) {
        String valor = leer(campo + " [" + actual + "]: ");
        return valor.isBlank() ? actual : valor;
    }

    static double leerOpcionalDecimal(String campo, double actual) {
        double valor = Double.parseDouble(leerOpcional(campo, String.valueOf(actual)).replace(',', '.'));
        if (Double.isNaN(valor) || Double.isInfinite(valor)) { // parseDouble acepta "NaN" e "Infinity"
            throw new NumberFormatException();
        }
        return valor;
    }

    static String elegir(String titulo, String... opciones) {
        StringBuilder sb = new StringBuilder(titulo + " (");
        for (int i = 0; i < opciones.length; i++) {
            sb.append(i + 1).append(". ").append(opciones[i]).append(i < opciones.length - 1 ? ", " : "): ");
        }
        while (true) {
            int n = leerEntero(sb.toString(), 1);
            if (n <= opciones.length) {
                return opciones[n - 1];
            }
            System.out.println("Opcion invalida.");
        }
    }
}
