package proyectofina.persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import proyectofina.*;

/**
 * Datos de demostracion del taller "Colores y Creaciones" (ceramica artesanal
 * inspirada en la cultura Cajamarca, segun S06). Se cargan una sola vez, cuando
 * la base de datos esta vacia. Todos los nombres y documentos son ficticios.
 * Los movimientos se generan en orden para que stock, kardex y costos cuadren.
 */
public final class DatosDemo {
    /** Clase utilitaria: no se instancia. */
    private DatosDemo() {
    }

    /** Construye el estado inicial: roles, usuarios, clientes, materias primas, productos con receta, producciones, ventas y pedidos. */
    public static Estado crear() {
        Estado e = new Estado();
        Rol admin = new Rol("Administrador", List.of("INVENTARIO", "VENTAS", "REPORTES", "USUARIOS"), "ADM-2026");
        Rol vendedor = new Rol("Vendedor", List.of("VENTAS"), "VEN-2026");
        Rol supervisor = new Rol("Supervisor de almacen", List.of("INVENTARIO", "REPORTES"), "SUP-2026");
        e.roles.addAll(List.of(admin, vendedor, supervisor));
        e.usuarios.add(new Usuario("U001", "Admin", "Principal", "00000001", admin, "1234"));
        e.usuarios.add(new Usuario("U002", "Vendedor", "Tienda", "00000002", vendedor, "1234"));
        e.usuarios.add(new Usuario("U003", "Supervisor", "Almacen", "00000003", supervisor, "1234"));

        e.clientes.add(new Cliente(Cliente.ID_VARIOS, "Clientes", "Varios", "00000000", "", "", ""));
        e.clientes.add(new Cliente("C001", "Ana", "Quispe", "12345678", "Jr. Junín 198, Cajamarca",
                "987654321", "ana.quispe@example.com", "Coleccionista"));
        e.clientes.add(new Cliente("C002", "Pedro", "Sandoval", "87654321", "Av. Atahualpa 540, Cajamarca",
                "912345678", "pedro.sandoval@example.com", "Decorador de interiores"));
        e.clientes.add(new Cliente("C003", "Lucía", "Manzilla", "34251678", "Jr. Del Comercio 310, Cajamarca",
                "946372645", "lucia.manzilla@example.com", "Turista"));
        e.clientes.add(new Cliente("C004", "Ricardo", "Salcedo", "45612378", "Av. Vía de Evitamiento 1200, Cajamarca",
                "976543210", "ricardo.salcedo@example.com", "Decorador de interiores"));
        e.clientes.add(new Cliente("C005", "Mariela", "Chávez", "70112233", "Lima", "955112233", "mariela.chavez@example.com", "Turista"));
        e.clientes.add(new Cliente("C006", "Jorge", "Tello", "41239876", "Jr. Amalia Puga 455, Cajamarca",
                "944556677", "", "Público general"));

        Date inicio = fecha(2026, 10, 1, 8);
        materia(e, "MP01", "Arcilla roja de Cajamarca", "kg", 200, 40, 1.20, inicio);
        materia(e, "MP02", "Caolín (arcilla blanca)", "kg", 50, 15, 2.50, inicio);
        materia(e, "MP03", "Esmalte transparente", "litro", 10, 3, 28.00, inicio);
        materia(e, "MP04", "Engobe blanco", "litro", 6, 2, 22.00, inicio);
        materia(e, "MP05", "Pigmento óxido de hierro", "kg", 2, 0.5, 35.00, inicio);
        materia(e, "MP06", "Pigmento óxido de cobalto", "kg", 0.5, 0.5, 120.00, inicio);
        materia(e, "MP07", "Gas GLP para horno", "kg", 50, 20, 4.20, inicio);
        materia(e, "MP08", "Caja y embalaje", "unidad", 150, 30, 0.80, inicio);

        producto(e, "P001", "Plato decorativo diseño Cajamarca", "PD-25 (25 cm)", 65, 22, 8, 6, inicio,
                "MP01", 1.2, "MP04", 0.05, "MP03", 0.08, "MP05", 0.01, "MP07", 0.4, "MP08", 1);
        producto(e, "P002", "Jarrón ceremonial", "JC-30 (30 cm)", 120, 45, 5, 4, inicio,
                "MP01", 2.5, "MP03", 0.15, "MP05", 0.02, "MP06", 0.005, "MP07", 0.9, "MP08", 1);
        producto(e, "P003", "Vasija tradicional cajamarquina", "VT-20 (20 cm)", 85, 30, 6, 4, inicio,
                "MP01", 1.8, "MP04", 0.08, "MP05", 0.02, "MP07", 0.7, "MP08", 1);
        producto(e, "P004", "Taza artesanal con asa", "TA-01 (350 ml)", 28, 9, 20, 12, inicio,
                "MP02", 0.35, "MP03", 0.05, "MP06", 0.002, "MP07", 0.15, "MP08", 1);
        producto(e, "P005", "Set de cuencos (3 unidades)", "SC-03", 75, 26, 8, 5, inicio,
                "MP02", 0.9, "MP03", 0.12, "MP07", 0.4, "MP08", 1);
        producto(e, "P006", "Cántaro miniatura de recuerdo", "CM-10 (10 cm)", 18, 5, 15, 15, inicio,
                "MP01", 0.25, "MP05", 0.003, "MP07", 0.08, "MP08", 1);

        produccion(e, "PR0001", fecha(2026, 10, 3, 16), "U003", "P001", 20, 19, "1 plato con grieta en el borde", 180);
        venta(e, "V0001", fecha(2026, 10, 8, 11), "C001", "U002", "Yape", "Boleta", 0, "P002", 1, "P001", 2);
        produccion(e, "PR0002", fecha(2026, 10, 12, 15), "U003", "P006", 40, 40, "", 120);
        venta(e, "V0002", fecha(2026, 10, 15, 12), "C003", "U002", "Efectivo", "Boleta", 0, "P006", 6, "P004", 2);
        venta(e, "V0003", fecha(2026, 10, 22, 17), "C002", "U001", "Transferencia", "Recibo", 10, "P001", 6, "P005", 2);
        venta(e, "V0004", fecha(2026, 10, 29, 10), Cliente.ID_VARIOS, "U002", "Efectivo", "Boleta", 0, "P006", 4);
        compra(e, "MP07", 30, 4.50, fecha(2026, 11, 1, 9), "U003");
        venta(e, "V0005", fecha(2026, 11, 3, 13), "C005", "U002", "Tarjeta", "Boleta", 0, "P003", 2, "P006", 3);
        produccion(e, "PR0003", fecha(2026, 11, 5, 16), "U003", "P004", 30, 28, "2 tazas con burbujas en el esmalte", 150);
        falla(e, "FA0001", fecha(2026, 11, 8, 18), "U003", "P003", 2, "se rajaron en el horno por exceso de temperatura");
        venta(e, "V0006", fecha(2026, 11, 10, 16), "C004", "U001", "Transferencia", "Recibo", 5, "P002", 2, "P001", 4);
        produccion(e, "PR0004", fecha(2026, 11, 14, 15), "U003", "P002", 4, 4, "", 140);
        venta(e, "V0007", fecha(2026, 11, 17, 11), "C006", "U002", "Plin", "Boleta", 0, "P004", 6);
        venta(e, "V0008", fecha(2026, 11, 20, 12), "C001", "U002", "Yape", "Boleta", 0, "P005", 1, "P003", 1);

        LocalDate hoy = LocalDate.now();
        pedido(e, "PE0001", fecha(2026, 11, 18, 10), hoy.plusDays(10), "C002", "Alta", "P001", 6,
                "Logo del hotel pintado al centro, tonos tierra");
        Pedido p2 = pedido(e, "PE0002", fecha(2026, 11, 19, 15), hoy.plusDays(3), "C001", "Urgente", "P002", 1,
                "Iconografía Cajamarca en azul cobalto");
        p2.revisarCalidad(true, "Diseño y acabado conformes");
        pedido(e, "PE0003", fecha(2026, 11, 21, 9), hoy.plusDays(15), "C005", "Normal", "P003", 10,
                "Nombre de la familia grabado en la base");

        e.auditoria.add(new RegistroAuditoria(new Date(), "SISTEMA", "CARGA_INICIAL", "Datos de demostración del taller de cerámica"));
        return e;
    }

    /** Fecha y hora fijas para que los datos de demostración siempre sean los mismos. */
    private static Date fecha(int anio, int mes, int dia, int hora) {
        return Date.from(LocalDateTime.of(anio, mes, dia, hora, 0).atZone(ZoneId.systemDefault()).toInstant());
    }

    /** Agrega una materia prima con su movimiento inicial en el kardex. */
    private static void materia(Estado e, String id, String nombre, String unidad, double stock, double minimo, double costo, Date f) {
        e.materias.add(new MateriaPrima(id, nombre, unidad, stock, minimo, costo));
        e.movimientosMateria.add(new Estado.MovimientoMateria(f, "U003", id, "INICIAL", stock, stock, ""));
    }

    /** Agrega un producto con su stock inicial y su receta (pares materia, cantidad). */
    private static void producto(Estado e, String id, String nombre, String modelo, double precio, double costo, int stock, int minimo,
            Date f, Object... receta) {
        Inventario p = new Inventario(id, nombre, modelo, "Cerámica decorativa", precio,
                "Cerámica artesanal hecha a mano", stock, minimo, costo, f);
        p.setUnidad("Unidad");
        e.productos.add(p);
        e.movimientos.add(new Estado.Movimiento(f, "U003", id, "INICIAL", stock, stock));
        Receta r = new Receta(id);
        for (int i = 0; i < receta.length; i += 2) {
            r.agregarInsumo((String) receta[i], ((Number) receta[i + 1]).doubleValue());
        }
        e.recetas.add(r);
    }

    /** Busca un producto de la demostración. */
    private static Inventario prod(Estado e, String id) {
        return e.productos.stream().filter(p -> p.getIdProducto().equals(id)).findFirst().orElseThrow();
    }

    /** Busca una materia prima de la demostración. */
    private static MateriaPrima mp(Estado e, String id) {
        return e.materias.stream().filter(m -> m.getIdMateria().equals(id)).findFirst().orElseThrow();
    }

    /** Descuenta la materia prima de la receta y la registra en la operación y el kardex. */
    private static void consumir(Estado e, OperacionProduccion op, String idProducto, int piezas) {
        Receta r = e.recetas.stream().filter(x -> x.getIdProducto().equals(idProducto)).findFirst().orElseThrow();
        for (var n : r.calcularNecesidades(piezas).entrySet()) {
            MateriaPrima m = mp(e, n.getKey());
            op.agregarConsumo(m, n.getValue());
            m.descontar(n.getValue());
            e.movimientosMateria.add(new Estado.MovimientoMateria(op.getFecha(), op.getUsuario(), m.getIdMateria(),
                    op.getTipo().equals("FALLA") ? "FALLA" : "CONSUMO", -n.getValue(), m.getStock(), op.getIdOperacion()));
        }
    }

    /** Simula una producción: consume materia prima y suma las piezas aprobadas. */
    private static void produccion(Estado e, String id, Date f, String usuario, String idProducto, int cantidad,
            int aprobadas, String obs, double manoObra) {
        Inventario p = prod(e, idProducto);
        Produccion pr = new Produccion(id, f, usuario, idProducto, p.getNombre(), cantidad, aprobadas, obs, manoObra);
        consumir(e, pr, idProducto, cantidad);
        p.actualizarStock(p.getStock() + aprobadas);
        p.setCosto(pr.calcularCostoUnitario());
        e.operaciones.add(pr);
        e.movimientos.add(new Estado.Movimiento(f, usuario, idProducto, "PRODUCCION", aprobadas, p.getStock()));
    }

    /** Simula una falla de producción. */
    private static void falla(Estado e, String id, Date f, String usuario, String idProducto, int piezas, String motivo) {
        FallaProduccion fa = new FallaProduccion(id, f, usuario, prod(e, idProducto).getNombre() + " x" + piezas + ": " + motivo);
        consumir(e, fa, idProducto, piezas);
        e.operaciones.add(fa);
    }

    /** Simula una compra de materia prima. */
    private static void compra(Estado e, String idMateria, double cantidad, double costo, Date f, String usuario) {
        MateriaPrima m = mp(e, idMateria);
        m.ingresar(cantidad, costo);
        e.movimientosMateria.add(new Estado.MovimientoMateria(f, usuario, idMateria, "INGRESO", cantidad, m.getStock(), "Compra"));
    }

    /** Simula una venta con sus productos (pares código, cantidad) descontando stock. */
    private static void venta(Estado e, String id, Date f, String idCliente, String idVendedor, String pago,
            String tipo, double descuento, Object... items) {
        Cliente c = e.clientes.stream().filter(x -> x.getIdCliente().equals(idCliente)).findFirst().orElseThrow();
        Usuario u = e.usuarios.stream().filter(x -> x.getIdUsuario().equals(idVendedor)).findFirst().orElseThrow();
        Venta v = new Venta(id, f, c, u, pago, tipo);
        for (int i = 0; i < items.length; i += 2) {
            Inventario p = prod(e, (String) items[i]);
            int cantidad = (Integer) items[i + 1];
            v.agregarProducto(p, cantidad, p.getPrecio());
            p.actualizarStock(p.getStock() - cantidad);
            e.movimientos.add(new Estado.Movimiento(f, idVendedor, p.getIdProducto(), "VENTA", -cantidad, p.getStock()));
        }
        v.aplicarDescuento(descuento);
        v.setEstado("Concluido");
        e.ventas.add(v);
        c.obtenerHistorialVentas().add(v);
    }

    /** Agrega un pedido de un solo producto. */
    private static Pedido pedido(Estado e, String id, Date f, LocalDate entrega, String idCliente, String prioridad,
            String idProducto, int cantidad, String personalizacion) {
        Cliente c = e.clientes.stream().filter(x -> x.getIdCliente().equals(idCliente)).findFirst().orElseThrow();
        Pedido p = new Pedido(id, f, Date.from(entrega.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                idCliente, c.getNombreCompleto(), prioridad);
        p.agregarDetalle(prod(e, idProducto), cantidad, personalizacion);
        e.pedidos.add(p);
        return p;
    }
}
