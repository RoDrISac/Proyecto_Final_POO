package proyectofina.servicios;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import proyectofina.*;
import proyectofina.persistencia.Estado;

/**
 * Pedidos personalizados de clientes (S06; S08 N.° 12, 13 y 14; HU 16 prioridad).
 * Un pedido activo compromete stock; pasa revision de calidad y al entregarse
 * se convierte en una venta con su comprobante.
 */
public final class PedidosServicio {
    private final Sistema s;

    PedidosServicio(Sistema s) {
        this.s = s;
    }

    public record Linea(String idProducto, int cantidad, String personalizacion) {
    }

    /** Registra un pedido PENDIENTE; la entrega no puede ser anterior a hoy. */
    public Pedido registrar(String clienteId, LocalDate entrega, String prioridad, List<Linea> lineas) throws IOException {
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos un producto al pedido.");
        }
        if (entrega == null || entrega.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de entrega no puede ser anterior a hoy.");
        }
        if (Cliente.ID_VARIOS.equals(clienteId)) {
            throw new IllegalArgumentException("Un pedido necesita un cliente registrado para contactarlo.");
        }
        String[] id = {null};
        s.cambiar("VENTAS", "REGISTRAR_PEDIDO", "Cliente " + clienteId + " | " + lineas.size() + " lineas | " + prioridad, e -> {
            Cliente c = VentasServicio.buscarCliente(e, clienteId);
            int n = e.pedidos.stream().mapToInt(p -> Integer.parseInt(p.getIdPedido().substring(2))).max().orElse(0) + 1;
            Pedido p = new Pedido(String.format("PE%04d", n), new Date(),
                    Date.from(entrega.atStartOfDay(ZoneId.systemDefault()).toInstant()), c.getIdCliente(),
                    c.getNombreCompleto(), prioridad);
            for (Linea l : lineas) {
                if (l.cantidad() <= 0 || l.cantidad() > 100_000) {
                    throw new IllegalArgumentException("Cantidad invalida para " + l.idProducto() + ".");
                }
                p.agregarDetalle(InventarioServicio.buscar(e, l.idProducto()), l.cantidad(), l.personalizacion());
            }
            e.pedidos.add(p);
            id[0] = p.getIdPedido();
        });
        return buscar(s.consultar(), id[0]);
    }

    /** Revision de calidad del pedido antes de enviarlo (S08 N.° 12). */
    public void revisarCalidad(String idPedido, boolean cumple, String observacion) throws IOException {
        s.cambiar("INVENTARIO", "CALIDAD_PEDIDO", idPedido + (cumple ? " aprobado" : " observado: " + observacion), e -> {
            Pedido p = buscar(e, idPedido);
            if (cumple) {
                for (var d : p.getDetalles()) {
                    Inventario prod = InventarioServicio.buscar(e, d.idProducto());
                    int otros = e.stockComprometido(d.idProducto()) - p.cantidadComprometida(d.idProducto());
                    if (prod.getStock() - Math.max(0, otros) < p.cantidadComprometida(d.idProducto())) {
                        throw new IllegalArgumentException("Aún no hay stock suficiente de " + d.nombre()
                                + " para este pedido. Registra la producción primero.");
                    }
                }
            }
            p.revisarCalidad(cumple, observacion);
        });
    }

    /** Entrega: registra la venta con el stock que el pedido tenia comprometido. */
    public Venta entregar(String idPedido, String pago, String tipo) throws IOException {
        String vendedor = s.usuario().getIdUsuario();
        String[] idVenta = {null};
        s.cambiar("VENTAS", "ENTREGAR_PEDIDO", idPedido, e -> {
            Pedido p = buscar(e, idPedido);
            if (!p.getEstado().equals(Pedido.APROBADO)) {
                throw new IllegalArgumentException("El pedido debe aprobar la revisión de calidad antes de entregarse.");
            }
            Map<String, Integer> carrito = new LinkedHashMap<>();
            for (var d : p.getDetalles()) {
                carrito.merge(d.idProducto(), d.cantidad(), Integer::sum);
            }
            Venta v = VentasServicio.registrar(e, vendedor, p.getIdCliente(), carrito, pago, tipo, 0, p);
            p.marcarEntregado(v.getIdVenta());
            idVenta[0] = v.getIdVenta();
        });
        return s.consultar().ventas.stream().filter(v -> v.getIdVenta().equals(idVenta[0])).findFirst().orElseThrow();
    }

    /** Cancela el pedido y libera su stock comprometido. */
    public void cancelar(String idPedido) throws IOException {
        s.cambiar("VENTAS", "CANCELAR_PEDIDO", idPedido, e -> buscar(e, idPedido).cancelar());
    }

    /** Unidades que faltan producir para cubrir los pedidos activos de un producto. */
    public int faltante(Estado e, String idProducto) {
        Inventario p = InventarioServicio.buscar(e, idProducto);
        return Math.max(0, e.stockComprometido(idProducto) - p.getStock());
    }

    /** Busca un pedido por código o informa que no existe. */
    static Pedido buscar(Estado e, String id) {
        return e.pedidos.stream().filter(p -> p.getIdPedido().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));
    }
}
