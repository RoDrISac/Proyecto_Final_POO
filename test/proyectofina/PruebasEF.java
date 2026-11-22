package proyectofina;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import proyectofina.persistencia.DatosDemo;
import proyectofina.persistencia.Estado;
import proyectofina.persistencia.Repositorio;
import proyectofina.servicios.PedidosServicio;
import proyectofina.servicios.ProduccionServicio;
import proyectofina.servicios.Sistema;

/**
 * Pruebas de aceptacion de los requerimientos completados en la Evaluacion Final.
 * Usan un repositorio en memoria (otra implementacion de la interfaz Repositorio).
 */
public final class PruebasEF {
    private static int ok;

    /** Repositorio en memoria: demuestra que Sistema funciona con cualquier implementacion. */
    static final class RepositorioMemoria implements Repositorio {
        Estado guardado = DatosDemo.crear();

        @Override
        public Estado cargar() {
            return guardado.copia();
        }

        @Override
        public void guardar(Estado estado) {
            guardado = estado.copia();
        }
    }

    /** Ejecuta las pruebas EF01–EF41 con el repositorio en memoria. */
    public static void main(String[] args) throws Exception {
        var repo = new RepositorioMemoria();
        var s = new Sistema(repo);
        s.ingresar("U001", "1234");

        // RF02 materias primas
        s.produccion.guardarMateria(new ProduccionServicio.DatosMateria("MP09", "Barniz mate", "litro", 4, 1, 30), true);
        verificar("EF01 - RF02 registrar materia prima", s.consultar().materias.stream().anyMatch(m -> m.getIdMateria().equals("MP09")));
        rechaza("EF02 - RF02 codigo de materia duplicado",
                () -> s.produccion.guardarMateria(new ProduccionServicio.DatosMateria("MP09", "X", "kg", 1, 1, 1), true));
        rechaza("EF03 - RF02 costo negativo",
                () -> s.produccion.guardarMateria(new ProduccionServicio.DatosMateria("MP10", "X", "kg", 1, 1, -5), true));
        s.produccion.ingresarMateria("MP09", 4, 40);
        var barniz = materia(s, "MP09");
        verificar("EF04 - Compra suma stock y promedia costo (4 a 30 + 4 a 40 = 35)",
                barniz.getStock() == 8 && barniz.getCostoUnitario() == 35);

        // RF03 + RF05 + RF11 produccion con calidad y costo
        double arcillaAntes = materia(s, "MP01").getStock();
        int platosAntes = producto(s, "P001").getStock();
        var pr = s.produccion.producir("P001", 10, 9, "1 pieza con grieta", 50);
        verificar("EF05 - RF03 producir descuenta materia prima de la receta (10 x 1.2 kg)",
                Math.abs(arcillaAntes - materia(s, "MP01").getStock() - 12) < 1e-9);
        verificar("EF06 - RF11 solo las aprobadas ingresan al inventario", producto(s, "P001").getStock() == platosAntes + 9);
        verificar("EF07 - RF05 costo del lote = materiales + mano de obra",
                Math.abs(pr.calcularCostoTotal() - (pr.calcularCostoMateriales() + 50)) < 0.01);
        verificar("EF08 - RF05 costo por pieza = lote / aprobadas",
                Math.abs(pr.calcularCostoUnitario() - Math.round(pr.calcularCostoTotal() / 9 * 100) / 100.0) < 0.01);
        rechaza("EF09 - RF11 rechazo sin observacion", () -> s.produccion.producir("P001", 5, 4, "", 0));
        rechaza("EF10 - RF03 materia prima insuficiente no produce", () -> s.produccion.producir("P002", 1000, 1000, "", 0));
        verificar("EF11 - Rechazo sin efectos parciales", producto(s, "P002").getStock() == 6);

        // RF04 falla
        double gasAntes = materia(s, "MP07").getStock();
        var falla = s.produccion.registrarFalla("P003", 2, "rotura al desmoldar");
        verificar("EF12 - RF04 falla descuenta materia prima (2 x 0.7 kg de gas)",
                Math.abs(gasAntes - materia(s, "MP07").getStock() - 1.4) < 1e-9);
        verificar("EF13 - Polimorfismo: FallaProduccion.getTipo() = FALLA",
                falla.getTipo().equals("FALLA") && pr.getTipo().equals("PRODUCCION"));

        // RF13 + RF14 pedidos y stock comprometido
        int disponibleAntes = s.consultar().stockDisponible(producto(s, "P004"));
        var pedido = s.pedidos.registrar("C006", LocalDate.now().plusDays(5), "Alta",
                List.of(new PedidosServicio.Linea("P004", 30, "Tazas con iniciales")));
        verificar("EF14 - RF14 el pedido compromete stock", s.consultar().stockDisponible(producto(s, "P004")) == disponibleAntes - 30);
        int disponible = s.consultar().stockDisponible(producto(s, "P004"));
        rechaza("EF15 - RF14 no se vende stock comprometido",
                () -> s.ventas.confirmar("C001", Map.of("P004", disponible + 1), "Yape", "Boleta"));
        rechaza("EF16 - Pedido con entrega pasada", () -> s.pedidos.registrar("C001", LocalDate.now().minusDays(1), "Normal",
                List.of(new PedidosServicio.Linea("P004", 1, ""))));
        rechaza("EF17 - RF12 no se entrega sin revision de calidad", () -> s.pedidos.entregar(pedido.getIdPedido(), "Yape", "Boleta"));
        rechaza("EF18 - RF12 observacion obligatoria si no cumple", () -> s.pedidos.revisarCalidad(pedido.getIdPedido(), false, ""));
        s.pedidos.revisarCalidad(pedido.getIdPedido(), true, "Conforme");
        int tazas = producto(s, "P004").getStock();
        var ventaPedido = s.pedidos.entregar(pedido.getIdPedido(), "Transferencia", "Recibo");
        verificar("EF19 - Entregar pedido genera la venta y descuenta stock",
                producto(s, "P004").getStock() == tazas - 30 && ventaPedido.getMontoTotal() == 30 * 28.0
                && s.consultar().pedidos.stream()
                .anyMatch(p -> p.getIdPedido().equals(pedido.getIdPedido()) && p.getEstado().equals(Pedido.ENTREGADO)));
        var pe3 = s.consultar().pedidos.stream().filter(p -> p.getIdPedido().equals("PE0003")).findFirst().orElseThrow();
        rechaza("EF20 - Calidad no aprueba si falta producir", () -> s.pedidos.revisarCalidad(pe3.getIdPedido(), true, ""));
        s.pedidos.cancelar("PE0003");
        verificar("EF21 - Cancelar libera el stock comprometido", s.consultar().stockComprometido("P003") == 0);

        // Alcance S07: descuento; RF41 cliente opcional; S10: boleta y recibo
        var conDescuento = s.ventas.confirmar("C001", Map.of("P006", 10), "Yape", "Recibo", 10);
        verificar("EF22 - Descuento 10 % (10 x 18 = 180 -> 162)",
                conDescuento.getMontoTotal() == 162.0 && conDescuento.calcularDescuento() == 18.0);
        rechaza("EF23 - Descuento mayor al 30 %", () -> s.ventas.confirmar("C001", Map.of("P006", 1), "Yape", "Boleta", 35));
        var anonima = s.ventas.confirmar(Cliente.ID_VARIOS, Map.of("P006", 1), "Efectivo", "Boleta");
        verificar("EF24 - RF41 boleta sin datos del cliente", anonima.getCliente().esClienteVarios()
                && new Comprobante(anonima).generarLineas().stream().anyMatch(l -> l.contains("CLIENTES VARIOS")));
        rechaza("EF25 - RF41 recibo exige cliente identificado",
                () -> s.ventas.confirmar(Cliente.ID_VARIOS, Map.of("P006", 1), "Efectivo", "Recibo"));
        verificar("EF26 - Numeracion R001 para recibos y B001 para boletas",
                new Comprobante(conDescuento).getIdComprobante().startsWith("R001-") && new Comprobante(anonima)
                .getIdComprobante().startsWith("B001-"));

        // S06 categoria de cliente
        s.clientes.registrar("C010", "Carla", "Rojas", "71234567", "Cajamarca", "987111222", "carla@example.com", "Coleccionista");
        s.clientes.editar("C010", "Carla", "Rojas", "71234567", "Cajamarca", "987111222", "carla@example.com", "Turista");
        verificar("EF27 - S06 registrar y editar categoria del cliente",
                s.consultar().clientes.stream().anyMatch(c -> c.getIdCliente().equals("C010") && c.getCategoria().equals("Turista")));
        s.clientes.eliminar("C010");
        rechaza("EF28 - No se elimina un cliente con compras", () -> s.clientes.eliminar("C001"));

        // RF22 / RF24 cierre contable
        rechaza("EF29 - RF22 no se cierra un mes futuro", () -> s.administracion.cerrarMes(YearMonth.now().plusMonths(1).toString()));
        s.administracion.cerrarMes(YearMonth.now().toString());
        rechaza("EF30 - RF24 no se vende en un mes cerrado", () -> s.ventas.confirmar("C001", Map.of("P006", 1), "Yape", "Boleta"));
        rechaza("EF31 - RF24 no se produce en un mes cerrado", () -> s.produccion.producir("P006", 1, 1, "", 0));
        rechaza("EF32 - Un mes no se cierra dos veces", () -> s.administracion.cerrarMes(YearMonth.now().toString()));

        // RF31 auditoria
        var auditoria = s.administracion.auditoria(LocalDate.now(), LocalDate.now(), "U001");
        verificar("EF33 - RF31 auditoria registra quien, que y cuando",
                auditoria.stream().anyMatch(r -> r.accion().equals("REGISTRAR_PRODUCCION"))
                && auditoria.stream().anyMatch(r -> r.accion().equals("CERRAR_MES")) && auditoria.stream()
                .allMatch(r -> r.fecha() != null));

        // S10 roles: contrasena del rol
        rechaza("EF34 - S10 asignar rol exige la contrasena del rol",
                () -> s.usuarios.registrar("U009", "Ana", "Lopez", "40404040", "abcd", "Vendedor", "mala"));
        s.usuarios.registrar("U009", "Ana", "Lopez", "40404040", "abcd", "Vendedor", "VEN-2026");
        verificar("EF35 - S10 rol asignado con su contrasena",
                s.consultar().usuarios.stream().anyMatch(u -> u.getIdUsuario().equals("U009")));

        // RF30 permisos sobre las funciones nuevas
        s.salir();
        s.ingresar("U002", "1234");
        rechaza("EF36 - RF30 vendedor no registra produccion", () -> s.produccion.producir("P001", 1, 1, "", 0));
        rechaza("EF37 - RF30 vendedor no ve la auditoria", () -> s.administracion.auditoria(LocalDate.now(), LocalDate.now(), ""));
        s.salir();
        s.ingresar("U003", "1234");
        rechaza("EF38 - RF30 supervisor no registra pedidos", () -> s.pedidos.registrar("C001", LocalDate.now().plusDays(2), "Normal",
                List.of(new PedidosServicio.Linea("P001", 1, ""))));

        // RF28 correo: sin configuracion se informa claramente
        rechaza("EF39 - RF28 correo sin configurar avisa",
                () -> CorreoSMTP.desdeConfiguracionDe(java.nio.file.Path.of("no-existe.properties")));
        verificar("EF40 - RF28 valida el correo del destinatario",
                CorreoSMTP.esCorreoValido("ana@example.com") && !CorreoSMTP.esCorreoValido("sin-arroba"));

        // Persistencia: lo guardado se recupera igual
        var recargado = new Sistema(repo);
        recargado.ingresar("U001", "1234");
        var e = recargado.consultar();
        verificar("EF41 - Se recuperan producciones, pedidos, auditoria y cierres",
                e.operaciones.size() == s.consultar().operaciones.size() && e.pedidos.size() == 4
                && e.mesesCerrados.contains(YearMonth.now().toString()) && !e.auditoria.isEmpty());
        System.out.println("TOTAL PASS: " + ok);
    }

    /** Materia prima actual por código. */
    private static MateriaPrima materia(Sistema s, String id) {
        return s.consultar().materias.stream().filter(m -> m.getIdMateria().equals(id)).findFirst().orElseThrow();
    }

    /** Producto actual por código. */
    private static Inventario producto(Sistema s, String id) {
        return s.consultar().productos.stream().filter(p -> p.getIdProducto().equals(id)).findFirst().orElseThrow();
    }

    interface Accion {
        void run() throws Exception;
    }

    /** Verifica que la acción sea rechazada con un error de negocio. */
    private static void rechaza(String nombre, Accion a) {
        try {
            a.run();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            ok++;
            System.out.println("PASS | " + nombre + "  (" + e.getMessage() + ")");
            return;
        } catch (Exception e) {
            throw new AssertionError("FALLA | " + nombre + ": excepcion inesperada " + e, e);
        }
        throw new AssertionError("FALLA | " + nombre + ": no se rechazo");
    }

    /** Registra una verificación aprobada o detiene la prueba si falla. */
    private static void verificar(String nombre, boolean cond) {
        if (!cond) {
            throw new AssertionError("FALLA | " + nombre);
        }
        ok++;
        System.out.println("PASS | " + nombre);
    }
}
