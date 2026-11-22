package proyectofina.ui;

import javax.swing.*;
import java.awt.*;
import proyectofina.Cliente;
import proyectofina.servicios.Sistema;

/** Clientes: registro, edicion, eliminacion, categoria (segmentacion S06) e historial. */
public final class ClientesPanel extends JPanel {
    private final Sistema s;
    private final JTable tabla = Gui.tabla("Código", "Nombre", "DNI", "Categoría", "Teléfono", "Correo", "Compras");
    private final JComboBox<String> filtro = new JComboBox<>();

    /** Arma la barra de acciones, el filtro por categoría y la tabla. */
    public ClientesPanel(Sistema s) {
        super(new BorderLayout());
        this.s = s;
        var p = Gui.pagina("Clientes", "Datos de contacto, categoría para campañas e historial de compras.");
        var c = new JPanel(new BorderLayout(8, 12));
        filtro.addItem("Todas las categorías");
        for (String cat : Cliente.CATEGORIAS) {
            filtro.addItem(cat);
        }
        filtro.addActionListener(e -> actualizar());
        var barras = new JPanel(new GridLayout(0, 1, 0, 4));
        barras.add(Gui.fila(Gui.boton("Nuevo cliente", () -> formulario(null)), Gui.boton("Editar", () -> formulario(seleccion())),
                Gui.boton("Eliminar", this::eliminar), Gui.boton("Historial de compras", this::historial)));
        barras.add(Gui.fila(new JLabel("Segmentar por categoría"), filtro));
        c.add(barras, BorderLayout.NORTH);
        c.add(new JScrollPane(tabla));
        p.add(c);
        add(p);
        actualizar();
    }

    /** Recarga la tabla aplicando el filtro de categoría. */
    public void actualizar() {
        String cat = filtro.getSelectedIndex() <= 0 ? null : (String) filtro.getSelectedItem();
        var e = s.consultar();
        Gui.datos(tabla, e.clientes.stream().filter(c -> !c.esClienteVarios()).filter(c -> cat == null || c.getCategoria().equals(cat))
                .map(c -> new Object[]{c.getIdCliente(), c.getNombreCompleto(), c.getDni(), c.getCategoria(), c.getTelefono(),
                    c.getCorreoElectronico(), e.ventas.stream()
                    .filter(v -> v.getCliente().getIdCliente().equals(c.getIdCliente())).count()})
                .toList());
    }

    /** Cliente de la fila seleccionada. */
    private Cliente seleccion() {
        String id = Gui.elegido(tabla);
        return s.consultar().clientes.stream().filter(c -> c.getIdCliente().equals(id)).findFirst().orElseThrow();
    }

    /** Formulario para registrar (c = null) o editar un cliente. */
    private void formulario(Cliente c) {
        var f = new Gui.Formulario().campo("Código", c == null ? "" : c.getIdCliente()).campo("Nombre", c == null ? "" : c.getNombre())
                .campo("Apellido", c == null ? "" : c.getApellido()).campo("DNI", c == null ? "" : c.getDni())
                .campo("Dirección", c == null ? "" : c.getDireccion()).campo("Teléfono", c == null ? "" : c.getTelefono())
                .campo("Correo", c == null ? "" : c.getCorreoElectronico())
                .opciones("Categoría", Cliente.CATEGORIAS, c == null ? Cliente.CATEGORIAS[0] : c.getCategoria());
        f.mostrar(this, c == null ? "Registrar cliente" : "Editar cliente", () -> {
            if (c == null) {
                s.clientes.registrar(f.valor("Código"), f.valor("Nombre"), f.valor("Apellido"), f.valor("DNI"), f.valor("Dirección"),
                        f.valor("Teléfono"), f.valor("Correo"), f.valor("Categoría"));
            } else {
                if (!f.valor("Código").trim().equalsIgnoreCase(c.getIdCliente())) {
                    throw new IllegalArgumentException("El código no se puede cambiar. Conserva " + c.getIdCliente());
                }
                s.clientes.editar(c.getIdCliente(), f.valor("Nombre"), f.valor("Apellido"), f.valor("DNI"), f.valor("Dirección"),
                        f.valor("Teléfono"), f.valor("Correo"), f.valor("Categoría"));
            }
            actualizar();
        });
    }

    /** Pide confirmación y elimina el cliente seleccionado. */
    private void eliminar() throws Exception {
        String id = Gui.elegido(tabla);
        if (Gui.confirmar(this, "¿Eliminar al cliente " + id + "? Solo es posible si no tiene compras ni pedidos.")) {
            s.clientes.eliminar(id);
            actualizar();
        }
    }

    /** Muestra las compras y pedidos del cliente seleccionado. */
    private void historial() {
        String id = Gui.elegido(tabla);
        StringBuilder b = new StringBuilder();
        var e = s.consultar();
        var ventas = e.ventas.stream().filter(v -> v.getCliente().getIdCliente().equals(id)).toList();
        for (var v : ventas) {
            b.append(v.getResumenVenta()).append("\n\n");
        }
        b.append("Total comprado: ").append(Gui.soles(ventas.stream().mapToDouble(v -> v.getMontoTotal()).sum())).append("\n\n");
        var pedidos = e.pedidos.stream().filter(p -> p.getIdCliente().equals(id)).toList();
        if (!pedidos.isEmpty()) {
            b.append("PEDIDOS\n");
            for (var p : pedidos) {
                b.append(p.getResumen()).append("\n\n");
            }
        }
        var texto = Gui.texto();
        texto.setText(b.toString());
        texto.setCaretPosition(0);
        var scroll = new JScrollPane(texto);
        scroll.setPreferredSize(new Dimension(820, 420));
        JOptionPane.showMessageDialog(this, scroll, "Historial · " + id, JOptionPane.PLAIN_MESSAGE);
    }
}
