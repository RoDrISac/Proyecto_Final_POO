package proyectofina;

import java.sql.Connection;
import java.util.Date;
import proyectofina.persistencia.Estado;
import proyectofina.persistencia.sql.ClienteDao;
import proyectofina.persistencia.sql.ConexionBD;
import proyectofina.persistencia.sql.ProductoDao;
import proyectofina.persistencia.sql.RepositorioSQL;
import proyectofina.persistencia.sql.VentaDao;

/**
 * Prueba de integracion con MySQL (XAMPP): conexion, carga, CRUD de clientes y
 * productos, consultas con filtro y rollback. Requiere la BD instalada.
 */
public final class PruebaBD {
    private static int ok;

    /** Ejecuta las pruebas contra MySQL en orden. */
    public static void main(String[] args) throws Exception {
        ConexionBD bd = ConexionBD.desdeArchivo(ConexionBD.CONFIG);
        bd.probar();
        marcar(true, "Conexion JDBC a " + bd.getUrl().split(";")[0]);

        RepositorioSQL repo = new RepositorioSQL(bd);
        Estado e = repo.cargar();
        marcar(!e.usuarios.isEmpty() && !e.roles.isEmpty(), "Carga: " + e.roles.size() + " roles, " + e.usuarios.size()
                + " usuarios, " + e.clientes.size() + " clientes, " + e.productos.size() + " productos, "
                + e.ventas.size() + " ventas, " + e.movimientos.size() + " movimientos");

        ClienteDao clientes = new ClienteDao();
        ProductoDao productos = new ProductoDao();
        try (Connection c = bd.abrir()) {
            // CREATE
            Cliente nuevo = new Cliente("CPRUEBA", "Prueba", "JDBC", "11112222", "Cajamarca", "976000111", "prueba@correo.com");
            clientes.insertar(c, nuevo);
            marcar(clientes.buscar(c, "CPRUEBA").isPresent(), "INSERT cliente CPRUEBA");
            // UPDATE
            nuevo.setTelefono("976999888");
            clientes.actualizar(c, nuevo);
            marcar(clientes.buscar(c, "CPRUEBA").get().getTelefono().equals("976999888"), "UPDATE telefono del cliente");
            // DELETE
            clientes.eliminar(c, "CPRUEBA");
            marcar(clientes.buscar(c, "CPRUEBA").isEmpty(), "DELETE cliente CPRUEBA");

            Inventario p = new Inventario("PPRUEBA", "Producto prueba", "M1", "Cerámica decorativa", 10.5, "Prueba", 2, 5, 4, new Date());
            productos.insertar(c, p);
            marcar(productos.stockBajo(c).stream().anyMatch(x -> x.getIdProducto().equals("PPRUEBA")),
                    "SELECT con filtro: PPRUEBA aparece en stock bajo");
            productos.eliminar(c, "PPRUEBA");
            marcar(productos.buscar(c, "PPRUEBA").isEmpty(), "DELETE producto PPRUEBA");

            Date inicio = new Date(0);
            marcar(new VentaDao(e.roles).listarPorPeriodo(c, inicio,
                    new Date(System.currentTimeMillis() + 86_400_000L)).size() == e.ventas.size(),
                    "SELECT ventas por periodo con JOIN a detalle_venta");
        }

        // Transaccion: un cliente que rompe una restriccion CHECK no deja nada a medias
        Estado copia = e.copia();
        copia.clientes.add(new Cliente("CMALO", "Malo", "Dato", "ABC", "", "", ""));
        copia.productos.get(0).actualizarStock(copia.productos.get(0).getStock() + 1);
        try {
            repo.guardar(copia);
            marcar(false, "Rollback esperado");
        } catch (java.io.IOException ex) {
            Estado despues = repo.cargar();
            marcar(despues.clientes.stream().noneMatch(x -> x.getIdCliente().equals("CMALO"))
                    && despues.productos.get(0).getStock() == e.productos.get(0).getStock(),
                    "ROLLBACK: un error deja la BD sin cambios");
        }

        // Flujo completo del sistema sobre MySQL y recarga desde la BD
        var s = new proyectofina.servicios.Sistema(repo);
        s.ingresar("U001", "1234");
        int cantaros = s.consultar().productos.stream().filter(p -> p.getIdProducto().equals("P006")).findFirst().orElseThrow().getStock();
        var pr = s.produccion.producir("P006", 5, 4, "1 pieza deformada", 20);
        var pedido = s.pedidos.registrar("C003", java.time.LocalDate.now().plusDays(4), "Urgente",
                java.util.List.of(new proyectofina.servicios.PedidosServicio.Linea("P006", 2, "Con nombre grabado")));
        var venta = s.ventas.confirmar("C001", java.util.Map.of("P006", 1), "Yape", "Recibo", 5);
        var otro = new proyectofina.servicios.Sistema(new RepositorioSQL(bd));
        otro.ingresar("U001", "1234");
        var r = otro.consultar();
        marcar(r.productos.stream().anyMatch(p -> p.getIdProducto().equals("P006") && p.getStock() == cantaros + 4 - 1),
                "INSERT/UPDATE desde el sistema: produccion (+4) y venta (-1) quedan en productos");
        marcar(r.operaciones.stream().anyMatch(o -> o.getIdOperacion().equals(pr.getIdOperacion()) && o.getConsumos().size() == 4),
                "Produccion y sus consumos de materia prima guardados (operaciones_produccion + consumos_materia)");
        marcar(r.pedidos.stream().anyMatch(p -> p.getIdPedido().equals(pedido.getIdPedido()) && p.getDetalles()
                .get(0).personalizacion().equals("Con nombre grabado")),
                "Pedido con su detalle personalizado guardado (pedidos + detalle_pedido)");
        marcar(r.ventas.stream().anyMatch(v -> v.getIdVenta().equals(venta.getIdVenta())
                && v.getDescuentoPorcentaje() == 5 && v.getMontoTotal() == venta.getMontoTotal()),
                "Venta con descuento recuperada con el mismo total");
        marcar(r.auditoria.stream().anyMatch(a -> a.accion().equals("REGISTRAR_PEDIDO")), "Auditoria guardada en la tabla auditoria");
        s.pedidos.cancelar(pedido.getIdPedido());
        try (Connection c = bd.abrir(); var ps = c.prepareStatement(
                "SELECT p.estado, COUNT(d.linea) FROM pedidos p JOIN detalle_pedido d ON d.id_pedido = p.id_pedido "
                + "WHERE p.id_pedido = ? GROUP BY p.estado")) {
            ps.setString(1, pedido.getIdPedido());
            try (var rs = ps.executeQuery()) {
                marcar(rs.next() && rs.getString(1).equals("CANCELADO") && rs.getInt(2) == 1,
                        "UPDATE del estado del pedido verificado con SELECT + JOIN");
            }
        }
        System.out.println("TOTAL PASS: " + ok);
    }

    /** Registra una verificación aprobada o detiene la prueba si falla. */
    private static void marcar(boolean cond, String texto) {
        if (!cond) {
            throw new AssertionError("FALLA | " + texto);
        }
        ok++;
        System.out.println("PASS | " + texto);
    }
}
