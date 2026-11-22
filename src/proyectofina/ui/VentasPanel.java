package proyectofina.ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import proyectofina.*;
import proyectofina.servicios.*;

/** Formulario de venta (S08 N.° 16, 38-44): carrito, comprobante, pago, descuento y confirmacion. */
public final class VentasPanel extends JPanel {
    private final Sistema s;
    private final LinkedHashMap<String, Integer> carrito = new LinkedHashMap<>();
    private final JTable tabla = Gui.tabla("Código", "Producto", "Modelo", "Cantidad", "Precio", "Subtotal");
    private final JComboBox<String> clientes = new JComboBox<>(), producto = new JComboBox<>(),
            pago = new JComboBox<>(VentasServicio.PAGOS), tipo = new JComboBox<>(VentasServicio.TIPOS_COMPROBANTE);
    private final JTextField codigo = new JTextField(12);
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
    private final JSpinner descuento = new JSpinner(new SpinnerNumberModel(0, 0, (int) Venta.DESCUENTO_MAXIMO, 1));
    private final JLabel total = new JLabel("Total: S/ 0.00"),
            estado = new JLabel("Agrega productos. El stock se descuenta solo al confirmar.");

    /** Arma el formulario de venta, el carrito y el total. */
    public VentasPanel(Sistema s) {
        super(new BorderLayout());
        this.s = s;
        var p = Gui.pagina("Nueva venta", "Cliente, productos, pago y descuento. Revisa el detalle antes de confirmar.");
        var c = new JPanel(new BorderLayout(8, 14));
        var superior = new JPanel(new GridLayout(0, 1, 0, 8));
        clientes.setPreferredSize(new Dimension(320, 34));
        producto.setPreferredSize(new Dimension(300, 34));
        pago.setPreferredSize(new Dimension(155, 34));
        tipo.setPreferredSize(new Dimension(120, 34));
        superior.add(Gui.fila(new JLabel("Cliente"), clientes, new JLabel("Comprobante"), tipo));
        superior.add(Gui.fila(new JLabel("Pago"), pago, new JLabel("Descuento %"), descuento));
        superior.add(Gui.fila(new JLabel("Producto"), producto, Gui.boton("Elegir", () -> {
            if (producto.getSelectedItem() != null) {
                codigo.setText(id(producto));
            }
        })));
        superior.add(Gui.fila(new JLabel("Código / lector"), codigo, new JLabel("Cantidad"), cantidad,
                Gui.boton("Agregar", this::agregar)));
        codigo.setName("codigoVenta");
        cantidad.setName("cantidadVenta");
        codigo.addActionListener(e -> Gui.intentar(this, this::agregar));
        descuento.addChangeListener(e -> actualizarCarrito());
        clientes.addActionListener(e -> {
            if (clientes.getSelectedItem() != null && id(clientes).equals(Cliente.ID_VARIOS)) {
                tipo.setSelectedItem("Boleta");
            }
        });
        c.add(superior, BorderLayout.NORTH);
        c.add(new JScrollPane(tabla));
        var inferior = new JPanel(new GridLayout(0, 1, 0, 8));
        total.setFont(new Font("Segoe UI", Font.BOLD, 20));
        inferior.add(Gui.fila(Gui.boton("Quitar seleccionado", () -> {
            carrito.remove(Gui.elegido(tabla));
            actualizarCarrito();
        }), Gui.boton("Vaciar", () -> {
            if (Gui.confirmar(this, "¿Vaciar el carrito?")) {
                carrito.clear();
                actualizarCarrito();
            }
        }), total, Gui.boton("Confirmar venta", this::confirmar)));
        inferior.add(estado);
        c.add(inferior, BorderLayout.SOUTH);
        p.add(c);
        add(p);
        actualizarCatalogos();
    }

    /** Código del elemento elegido en una lista (texto antes de " · "). */
    private static String id(JComboBox<String> c) {
        Object o = c.getSelectedItem();
        if (o == null) {
            throw new IllegalArgumentException("Selecciona un registro.");
        }
        return o.toString().split(" · ")[0];
    }

    /** Recarga clientes y productos sin perder la selección actual. */
    public void actualizarCatalogos() {
        var cliente = clientes.getSelectedItem();
        var prod = producto.getSelectedItem();
        clientes.removeAllItems();
        producto.removeAllItems();
        var e = s.consultar();
        clientes.addItem(Cliente.ID_VARIOS + " · Clientes varios (sin datos, solo boleta)");
        for (var c : e.clientes) {
            if (!c.esClienteVarios()) {
                clientes.addItem(c.getIdCliente() + " · " + c.getNombreCompleto());
            }
        }
        for (var p : e.productos) {
            producto.addItem(p.getIdProducto() + " · " + p.getNombre() + " (disp. " + e.stockDisponible(p) + ")");
        }
        if (cliente != null) {
            clientes.setSelectedItem(cliente);
        }
        if (prod != null) {
            producto.setSelectedItem(prod);
        }
        actualizarCarrito();
    }

    /** Agrega el producto del código (o del lector) al carrito, sin superar el stock disponible. */
    private void agregar() throws java.text.ParseException {
        cantidad.commitEdit();
        String id = codigo.getText().trim();
        var e = s.consultar();
        var p = e.productos.stream().filter(x -> x.getIdProducto().equalsIgnoreCase(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe el código ingresado."));
        int disponible = e.stockDisponible(p);
        int n = ((Number) cantidad.getValue()).intValue() + carrito.getOrDefault(p.getIdProducto(), 0);
        if (n > disponible) {
            int comprometido = e.stockComprometido(p.getIdProducto());
            throw new IllegalArgumentException("Stock insuficiente. Disponible para vender: " + disponible
                    + (comprometido > 0 ? " (" + comprometido + " comprometidas en pedidos)" : "")
                    + "; ya agregado: " + carrito.getOrDefault(p.getIdProducto(), 0));
        }
        carrito.put(p.getIdProducto(), n);
        actualizarCarrito();
        estado.setText("Agregado: " + p.getNombre() + " · " + p.getModelo());
        codigo.setText("");
        codigo.requestFocusInWindow();
    }

    /** Recalcula la tabla del carrito, el descuento y el total. */
    private void actualizarCarrito() {
        var filas = new ArrayList<Object[]>();
        double suma = 0;
        var e = s.consultar();
        for (var item : carrito.entrySet()) {
            var p = e.productos.stream().filter(x -> x.getIdProducto().equals(item.getKey())).findFirst().orElse(null);
            if (p == null) {
                continue;
            }
            double sub = p.getPrecio() * item.getValue();
            suma += sub;
            filas.add(new Object[]{p.getIdProducto(), p.getNombre(), p.getModelo(), item.getValue(),
                    Gui.soles(p.getPrecio()), Gui.soles(sub)});
        }
        Gui.datos(tabla, filas);
        int d = ((Number) descuento.getValue()).intValue();
        double desc = Math.round(suma * d) / 100.0;
        total.setText(d == 0 ? "Total: " + Gui.soles(suma)
                : "<html>Subtotal " + Gui.soles(suma) + " · Dscto " + d + "% -" + Gui.soles(desc) + " · <b>Total "
                + Gui.soles(suma - desc) + "</b></html>");
    }

    /** Confirma la venta, abre el PDF y ofrece enviarlo por correo. */
    private void confirmar() throws Exception {
        if (carrito.isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos un producto.");
        }
        String cliente = id(clientes);
        int d = ((Number) descuento.getValue()).intValue();
        if (!Gui.confirmar(this, "¿Registrar la venta (" + tipo.getSelectedItem() + ")?\n" + total.getText().replaceAll("<[^>]+>", "")
                + "\nSe descontará el stock de los productos.")) {
            return;
        }
        Venta v = s.ventas.confirmar(cliente, carrito, (String) pago.getSelectedItem(), (String) tipo.getSelectedItem(), d);
        carrito.clear();
        descuento.setValue(0);
        actualizarCatalogos();
        estado.setText("Venta " + v.getIdVenta() + " registrada · " + Gui.soles(v.getMontoTotal()));
        try {
            var pdf = s.ventas.comprobante(v);
            estado.setText("Venta " + v.getIdVenta() + " registrada · PDF: " + pdf.getName());
            Gui.abrirPdfGuardado(this, pdf);
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this, "La venta SÍ se guardó. No repitas la venta.\n"
                    + "El PDF no pudo generarse; reintenta desde Historial.\n" + e.getMessage());
            return;
        }
        if (CorreoSMTP.esCorreoValido(v.getCliente().getCorreoElectronico()) && !v.getCliente().esClienteVarios()
                && Gui.confirmar(this, "¿Enviar el comprobante a " + v.getCliente().getCorreoElectronico() + "?")) {
            Gui.intentar(this, () -> {
                s.ventas.enviarPorCorreo(v);
                estado.setText("Venta " + v.getIdVenta() + " · comprobante enviado a " + v.getCliente().getCorreoElectronico());
            });
        }
    }
}
