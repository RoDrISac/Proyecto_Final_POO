package proyectofina.ui;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import proyectofina.*;
import proyectofina.servicios.*;

/**
 * Pedidos personalizados (S06; S08 N.° 12, 13, 14): registro con prioridad,
 * stock comprometido, revision de calidad antes del envio y entrega como venta.
 */
public final class PedidosPanel extends JPanel {
    private final Sistema s;
    private final JTable tabla = Gui.tabla("Pedido", "Cliente", "Entrega", "Prioridad", "Estado", "Total", "Falta producir");
    private final JComboBox<String> filtro = new JComboBox<>(
            new String[]{"Activos", "Todos", Pedido.PENDIENTE, Pedido.APROBADO, Pedido.ENTREGADO, Pedido.CANCELADO});

    /** Arma el filtro, las acciones según el rol y la tabla. */
    public PedidosPanel(Sistema s) {
        super(new BorderLayout());
        this.s = s;
        var p = Gui.pagina("Pedidos",
                "Modelos personalizados. Lo pedido queda reservado (stock comprometido) hasta entregarlo o cancelarlo.");
        var c = new JPanel(new BorderLayout(8, 12));
        var barra = Gui.fila(new JLabel("Mostrar"), filtro, Gui.boton("Ver detalle", this::detalle));
        if (s.permite("VENTAS")) {
            barra.add(Gui.boton("Nuevo pedido", this::nuevo));
        }
        if (s.permite("INVENTARIO")) {
            barra.add(Gui.boton("Revisión de calidad", this::calidad));
        }
        if (s.permite("VENTAS")) {
            barra.add(Gui.boton("Entregar", this::entregar));
            barra.add(Gui.boton("Cancelar pedido", this::cancelar));
        }
        filtro.addActionListener(e -> actualizar());
        c.add(barra, BorderLayout.NORTH);
        c.add(new JScrollPane(tabla));
        c.add(new JLabel("Orden: primero Urgente, luego Alta y Normal; dentro de cada prioridad, la fecha de entrega más próxima."),
                BorderLayout.SOUTH);
        p.add(c);
        add(p);
        actualizar();
    }

    /** Lista los pedidos del filtro, ordenados por prioridad y fecha de entrega. */
    public void actualizar() {
        var e = s.consultar();
        var f = new SimpleDateFormat("dd/MM/yyyy");
        String sel = (String) filtro.getSelectedItem();
        Gui.datos(tabla, e.pedidos.stream()
                .filter(p -> sel.equals("Todos") || (sel.equals("Activos") ? p.estaActivo() : p.getEstado().equals(sel)))
                .sorted((a, b) -> {
                    int pa = List.of(Pedido.PRIORIDADES).indexOf(a.getPrioridad()),
                            pb = List.of(Pedido.PRIORIDADES).indexOf(b.getPrioridad());
                    return pa != pb ? pb - pa : a.getFechaEntrega().compareTo(b.getFechaEntrega());
                })
                .map(p -> new Object[]{p.getIdPedido(), p.getNombreCliente(), f.format(p.getFechaEntrega()),
                        p.getPrioridad(), p.getEstado(),
                    Gui.soles(p.calcularTotal()), faltante(e, p)})
                .toList());
    }

    /** Texto con lo que falta producir para cubrir el pedido. */
    private String faltante(proyectofina.persistencia.Estado e, Pedido p) {
        if (!p.estaActivo()) {
            return "";
        }
        var partes = new ArrayList<String>();
        for (var d : p.getDetalles()) {
            int falta = s.pedidos.faltante(e, d.idProducto());
            if (falta > 0) {
                partes.add(d.nombre() + ": " + falta);
            }
        }
        return partes.isEmpty() ? "Nada" : String.join(", ", partes);
    }

    /** Pedido de la fila seleccionada. */
    private Pedido seleccion() {
        String id = Gui.elegido(tabla);
        return s.consultar().pedidos.stream().filter(p -> p.getIdPedido().equals(id)).findFirst().orElseThrow();
    }

    /** Muestra el detalle del pedido seleccionado. */
    private void detalle() {
        var t = Gui.texto();
        t.setText(seleccion().getResumen());
        var scroll = new JScrollPane(t);
        scroll.setPreferredSize(new Dimension(860, 300));
        JOptionPane.showMessageDialog(this, scroll, "Detalle del pedido", JOptionPane.PLAIN_MESSAGE);
    }

    /** Diálogo para registrar un pedido con varias líneas y su personalización. */
    private void nuevo() {
        var e = s.consultar();
        var d = new JDialog(SwingUtilities.getWindowAncestor(this), "Nuevo pedido", Dialog.ModalityType.APPLICATION_MODAL);
        var cliente = new JComboBox<String>();
        e.clientes.stream().filter(c -> !c.esClienteVarios())
                .forEach(c -> cliente.addItem(c.getIdCliente() + " · " + c.getNombreCompleto()));
        var entrega = new JTextField(LocalDate.now().plusDays(7).toString(), 10);
        var prioridad = new JComboBox<>(Pedido.PRIORIDADES);
        var producto = new JComboBox<String>();
        e.productos.forEach(p -> producto.addItem(p.getIdProducto() + " · " + p.getNombre() + " · " + Gui.soles(p.getPrecio())));
        var cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
        var personalizacion = new JTextField(26);
        var lineas = new ArrayList<PedidosServicio.Linea>();
        var tablaLineas = Gui.tabla("Producto", "Cantidad", "Personalización");
        var form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.setBorder(BorderFactory.createEmptyBorder(14, 18, 8, 18));
        form.add(Gui.fila(new JLabel("Cliente"), cliente));
        form.add(Gui.fila(new JLabel("Entrega (AAAA-MM-DD)"), entrega, new JLabel("Prioridad"), prioridad));
        form.add(Gui.fila(new JLabel("Producto"), producto, new JLabel("Cantidad"), cantidad));
        form.add(Gui.fila(new JLabel("Diseño / personalización"), personalizacion, Gui.boton("Agregar línea", () -> {
            if (producto.getSelectedItem() == null) {
                throw new IllegalArgumentException("No hay productos registrados.");
            }
            cantidad.commitEdit();
            lineas.add(new PedidosServicio.Linea(producto.getSelectedItem().toString().split(" · ")[0],
                    ((Number) cantidad.getValue()).intValue(), personalizacion.getText()));
            Gui.datos(tablaLineas, lineas.stream().map(l -> new Object[]{l.idProducto(), l.cantidad(), l.personalizacion()}).toList());
            personalizacion.setText("");
        })));
        var scroll = new JScrollPane(tablaLineas);
        scroll.setPreferredSize(new Dimension(700, 160));
        var centro = new JPanel(new BorderLayout());
        centro.add(form, BorderLayout.NORTH);
        centro.add(scroll);
        d.add(centro);
        d.add(Gui.fila(Gui.boton("Cancelar", d::dispose), Gui.boton("Guardar pedido", () -> {
            if (cliente.getSelectedItem() == null) {
                throw new IllegalArgumentException("Registra primero al cliente.");
            }
            LocalDate fecha;
            try {
                fecha = LocalDate.parse(entrega.getText().trim());
            } catch (java.time.format.DateTimeParseException ex) {
                throw new IllegalArgumentException("Usa una fecha válida con formato AAAA-MM-DD.");
            }
            var p = s.pedidos.registrar(cliente.getSelectedItem().toString().split(" · ")[0], fecha,
                    (String) prioridad.getSelectedItem(), lineas);
            d.dispose();
            actualizar();
            JOptionPane.showMessageDialog(this, "Pedido " + p.getIdPedido() + " registrado por " + Gui.soles(p.calcularTotal())
                    + ".\nLas cantidades quedan comprometidas en el inventario.");
        })), BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    /** Formulario de revisión de calidad del pedido. */
    private void calidad() {
        var p = seleccion();
        var f = new Gui.Formulario().opciones("Resultado", new String[]{"Cumple el diseño", "No cumple (observado)"}, "Cumple el diseño")
                .campo("Observación", p.getObservacionCalidad());
        f.mostrar(this, "Revisión de calidad · " + p.getIdPedido(), () -> {
            s.pedidos.revisarCalidad(p.getIdPedido(), f.valor("Resultado").startsWith("Cumple"), f.valor("Observación"));
            actualizar();
        });
    }

    /** Entrega el pedido: genera la venta y abre el comprobante. */
    private void entregar() {
        var p = seleccion();
        var f = new Gui.Formulario().opciones("Pago", VentasServicio.PAGOS, "Efectivo")
                .opciones("Comprobante", VentasServicio.TIPOS_COMPROBANTE, "Boleta");
        f.mostrar(this, "Entregar " + p.getIdPedido() + " · " + Gui.soles(p.calcularTotal()), () -> {
            Venta v = s.pedidos.entregar(p.getIdPedido(), f.valor("Pago"), f.valor("Comprobante"));
            actualizar();
            Gui.abrirPdfGuardado(this, s.ventas.comprobante(v));
        });
    }

    /** Pide confirmación y cancela el pedido seleccionado. */
    private void cancelar() throws Exception {
        var p = seleccion();
        if (Gui.confirmar(this, "¿Cancelar el pedido " + p.getIdPedido() + "? Se libera el stock comprometido.")) {
            s.pedidos.cancelar(p.getIdPedido());
            actualizar();
        }
    }
}
