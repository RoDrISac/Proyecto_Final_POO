package proyectofina.servicios;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import proyectofina.*;
import proyectofina.persistencia.Estado;

/** Registro de ventas (S08 N.° 9, 16, 38-44), comprobantes y envio por correo. */
public final class VentasServicio {
    public static final String COMPROBANTE = "Boleta";
    public static final String[] TIPOS_COMPROBANTE = {"Boleta", "Recibo"};
    public static final String[] PAGOS = {"Efectivo", "Yape", "Plin", "Transferencia", "Tarjeta"};
    private final Sistema s;

    VentasServicio(Sistema s) {
        this.s = s;
    }

    /** Confirma una venta sin descuento. */
    public Venta confirmar(String clienteId, Map<String, Integer> carrito, String pago, String tipo) throws IOException {
        return confirmar(clienteId, carrito, pago, tipo, 0);
    }

    /**
     * Confirma la venta: valida todo, crea la venta con sus detalles, aplica el
     * descuento, descuenta el stock y registra los movimientos. Todo se guarda
     * junto; si algo falla no queda nada a medias.
     */
    public Venta confirmar(String clienteId, Map<String, Integer> carrito, String pago, String tipo, double descuento) throws IOException {
        if (carrito.isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos un producto.");
        }
        if (!List.of(PAGOS).contains(pago) || !List.of(TIPOS_COMPROBANTE).contains(tipo)) {
            throw new IllegalArgumentException("Pago o comprobante inválido.");
        }
        if (Cliente.ID_VARIOS.equals(clienteId) && !tipo.equals("Boleta")) {
            throw new IllegalArgumentException("El recibo necesita los datos del cliente. Usa boleta para clientes varios.");
        }
        String vendedor = s.usuario().getIdUsuario();
        String[] nuevoId = {null};
        s.cambiar("VENTAS", "REGISTRAR_VENTA", "Cliente " + clienteId + " | " + carrito + " | dscto " + descuento + "%", e -> {
            nuevoId[0] = registrar(e, vendedor, clienteId, carrito, pago, tipo, descuento, null).getIdVenta();
        });
        return s.consultar().ventas.stream().filter(v -> v.getIdVenta().equals(nuevoId[0])).findFirst().orElseThrow();
    }

    /**
     * Logica comun a una venta en tienda y a la entrega de un pedido. Si viene de
     * un pedido, puede usar el stock que ese mismo pedido tenia comprometido.
     */
    static Venta registrar(Estado e, String idVendedor, String clienteId, Map<String, Integer> carrito, String pago,
            String tipo, double descuento, Pedido pedido) {
        Date ahora = new Date();
        Sistema.validarFechaOperacion(e, ahora);
        Cliente cliente = buscarCliente(e, clienteId);
        Usuario vendedor = e.usuarios.stream().filter(u -> u.getIdUsuario().equals(idVendedor)).findFirst().orElseThrow();
        int numero = e.ventas.stream().mapToInt(v -> Integer.parseInt(v.getIdVenta().substring(1))).max().orElse(0) + 1;
        Venta v = new Venta(String.format("V%04d", numero), ahora, cliente, vendedor, pago, tipo);
        for (var item : carrito.entrySet()) {
            Inventario p = InventarioServicio.buscar(e, item.getKey());
            int cantidad = item.getValue();
            int reservadoParaEste = pedido == null ? 0 : pedido.cantidadComprometida(p.getIdProducto());
            int disponible = Math.min(p.getStock(), e.stockDisponible(p) + reservadoParaEste);
            if (cantidad <= 0 || cantidad > disponible) {
                int comprometido = e.stockComprometido(p.getIdProducto()) - reservadoParaEste;
                throw new IllegalArgumentException("Stock insuficiente para " + p.getNombre() + ". Disponible para vender: " + disponible
                        + (comprometido > 0 ? " (hay " + comprometido + " comprometidas en pedidos)" : ""));
            }
            v.agregarProducto(p, cantidad, p.getPrecio());
            p.actualizarStock(p.getStock() - cantidad);
            e.movimientos.add(new Estado.Movimiento(ahora, idVendedor, p.getIdProducto(), "VENTA", -cantidad, p.getStock()));
        }
        v.aplicarDescuento(descuento);
        v.setEstado("Concluido");
        e.ventas.add(v);
        cliente.obtenerHistorialVentas().add(v);
        return v;
    }

    /** Busca el cliente; el cliente generico "Clientes varios" se crea la primera vez. */
    static Cliente buscarCliente(Estado e, String clienteId) {
        if (Cliente.ID_VARIOS.equals(clienteId) && e.clientes.stream().noneMatch(Cliente::esClienteVarios)) {
            e.clientes.add(new Cliente(Cliente.ID_VARIOS, "Clientes", "Varios", "00000000", "", "", ""));
        }
        return e.clientes.stream().filter(c -> c.getIdCliente().equals(clienteId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Selecciona un cliente."));
    }

    /** Genera el PDF del comprobante de una venta. */
    public File comprobante(Venta v) throws IOException {
        s.exigir("VENTAS");
        Comprobante c = new Comprobante(v);
        c.emitirPDF();
        return c.getArchivo();
    }

    /** Envio virtual del comprobante al correo del cliente (S08 N.° 28). */
    public String enviarPorCorreo(Venta v) throws IOException {
        s.exigir("VENTAS");
        String correo = v.getCliente().getCorreoElectronico();
        if (v.getCliente().esClienteVarios() || !CorreoSMTP.esCorreoValido(correo)) {
            throw new IllegalArgumentException("El cliente de esta venta no tiene un correo válido registrado.");
        }
        new Comprobante(v).enviarPorCorreo(correo);
        s.cambiar("VENTAS", "ENVIAR_COMPROBANTE", v.getIdVenta() + " a " + correo, e -> { });
        return correo;
    }
}
