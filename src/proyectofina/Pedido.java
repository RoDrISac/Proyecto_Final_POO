package proyectofina;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Pedido de un cliente con modelos personalizados (S08 N.° 12, 13 y 14; S06).
 * Mientras esta activo, sus cantidades quedan como STOCK COMPROMETIDO y no se
 * pueden vender a otros clientes. Antes de entregarlo pasa la revision de calidad.
 */
public class Pedido implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static final String PENDIENTE = "PENDIENTE", APROBADO = "APROBADO",
            ENTREGADO = "ENTREGADO", CANCELADO = "CANCELADO";
    public static final String[] PRIORIDADES = {"Normal", "Alta", "Urgente"};

    private final String idPedido;
    private final Date fecha;
    private final Date fechaEntrega;
    private final String idCliente;
    private final String nombreCliente;
    private final String prioridad;
    private final List<DetallePedido> detalles = new ArrayList<>();
    private String estado = PENDIENTE;
    private String observacionCalidad = "";
    private String idVenta = "";

    /** Linea del pedido: producto, cantidad, precio y detalle de personalizacion. */
    public record DetallePedido(String idProducto, String nombre, int cantidad, double precioUnitario, String personalizacion)
            implements java.io.Serializable {
        public DetallePedido {
            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad del pedido debe ser mayor a cero.");
            }
            personalizacion = personalizacion == null ? "" : personalizacion.trim();
        }

        /** Cantidad por precio unitario, redondeado a centimos. */
        public double subtotal() {
            return Math.round(cantidad * precioUnitario * 100.0) / 100.0;
        }
    }

    /** Crea un pedido PENDIENTE; la fecha de entrega y la prioridad son obligatorias. */
    public Pedido(String idPedido, Date fecha, Date fechaEntrega, String idCliente, String nombreCliente, String prioridad) {
        if (fechaEntrega == null) {
            throw new IllegalArgumentException("Indica la fecha de entrega.");
        }
        if (!List.of(PRIORIDADES).contains(prioridad)) {
            throw new IllegalArgumentException("Prioridad no válida.");
        }
        this.idPedido = idPedido;
        this.fecha = fecha;
        this.fechaEntrega = fechaEntrega;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.prioridad = prioridad;
    }

    /** Agrega un producto con su cantidad, precio actual y diseño pedido. */
    public void agregarDetalle(Producto p, int cantidad, String personalizacion) {
        detalles.add(new DetallePedido(p.getIdProducto(), p.getNombre(), cantidad, p.getPrecio(), personalizacion));
    }

    /** Usado al leer desde la base de datos. */
    public void agregarDetalle(DetallePedido d) {
        detalles.add(d);
    }

    /** Suma de los subtotales del pedido. */
    public double calcularTotal() {
        double total = 0;
        for (DetallePedido d : detalles) {
            total += d.subtotal();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    /** Un pedido activo compromete stock. */
    public boolean estaActivo() {
        return estado.equals(PENDIENTE) || estado.equals(APROBADO);
    }

    /** Unidades de un producto que este pedido reserva mientras está activo. */
    public int cantidadComprometida(String idProducto) {
        if (!estaActivo()) {
            return 0;
        }
        return detalles.stream().filter(d -> d.idProducto().equals(idProducto)).mapToInt(DetallePedido::cantidad).sum();
    }

    /** Revision de calidad antes del envio (S08 N.° 12). */
    public void revisarCalidad(boolean cumple, String observacion) {
        if (!estaActivo()) {
            throw new IllegalStateException("Solo se revisan pedidos pendientes o aprobados.");
        }
        if (!cumple && (observacion == null || observacion.isBlank())) {
            throw new IllegalArgumentException("Indica qué detalle del diseño no se cumple.");
        }
        observacionCalidad = observacion == null ? "" : observacion.trim();
        estado = cumple ? APROBADO : PENDIENTE;
    }

    /** Cierra el pedido con la venta generada; exige la revisión de calidad aprobada. */
    public void marcarEntregado(String idVenta) {
        if (!estado.equals(APROBADO)) {
            throw new IllegalStateException("El pedido debe aprobar la revisión de calidad antes de entregarse.");
        }
        this.idVenta = idVenta;
        estado = ENTREGADO;
    }

    /** Cancela un pedido activo y libera su stock comprometido. */
    public void cancelar() {
        if (!estaActivo()) {
            throw new IllegalStateException("El pedido ya fue " + estado.toLowerCase() + ".");
        }
        estado = CANCELADO;
    }

    // ---------- Getters y setters ----------
    /** Texto del pedido con sus lineas, total, observacion de calidad y venta generada. */
    public String getResumen() {
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Pedido %s | Cliente: %s | Registrado: %s | Entrega: %s | Prioridad: %s | Estado: %s%n",
                idPedido, nombreCliente, f.format(fecha), f.format(fechaEntrega), prioridad, estado));
        for (DetallePedido d : detalles) {
            sb.append(String.format("   %-24s x%-3d S/ %8.2f = S/ %9.2f%s%n", d.nombre(), d.cantidad(), d.precioUnitario(),
                    d.subtotal(), d.personalizacion().isEmpty() ? "" : "  | Diseno: " + d.personalizacion()));
        }
        sb.append(String.format("   Total: S/ %.2f", calcularTotal()));
        if (!observacionCalidad.isEmpty()) {
            sb.append("\n   Calidad: ").append(observacionCalidad);
        }
        if (!idVenta.isEmpty()) {
            sb.append("\n   Venta generada: ").append(idVenta);
        }
        return sb.toString();
    }

    public String getIdPedido() {
        return idPedido;
    }

    public Date getFecha() {
        return fecha;
    }

    public Date getFechaEntrega() {
        return fechaEntrega;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public List<DetallePedido> getDetalles() {
        return List.copyOf(detalles);
    }

    public String getEstado() {
        return estado;
    }

    /** Usado al leer desde la base de datos. */
    public void restaurar(String estado, String observacionCalidad, String idVenta) {
        this.estado = estado;
        this.observacionCalidad = observacionCalidad == null ? "" : observacionCalidad;
        this.idVenta = idVenta == null ? "" : idVenta;
    }

    public String getObservacionCalidad() {
        return observacionCalidad;
    }

    public String getIdVenta() {
        return idVenta;
    }
}
