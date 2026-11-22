package proyectofina.ui;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import proyectofina.*;
import proyectofina.servicios.*;

/**
 * Produccion del taller (S08 N.° 3, 4, 5 y 11): recetas, registro de produccion
 * con revision de calidad, fallas y costo por pieza y por lote.
 */
public final class ProduccionPanel extends JPanel {
    private final Sistema s;
    private final JComboBox<String> producto = new JComboBox<>();
    private final JTable tabla = Gui.tabla("Operación", "Fecha", "Tipo", "Detalle", "Cantidad", "Aprobadas",
            "Rechazadas", "Costo lote", "Costo pieza");
    private final JTextArea receta = Gui.texto();

    /** Arma el selector de producto, las acciones, la receta y el historial. */
    public ProduccionPanel(Sistema s) {
        super(new BorderLayout());
        this.s = s;
        var p = Gui.pagina("Producción", "Cada pieza descuenta su materia prima. "
                + "Solo las piezas que aprueban la revisión de calidad ingresan al inventario.");
        var c = new JPanel(new BorderLayout(8, 12));
        producto.setPreferredSize(new Dimension(320, 34));
        producto.addActionListener(e -> mostrarReceta());
        var barras = new JPanel(new GridLayout(0, 1, 0, 4));
        barras.add(Gui.fila(new JLabel("Producto"), producto, Gui.boton("Definir receta", this::definirReceta)));
        barras.add(Gui.fila(Gui.boton("Registrar producción", this::producir), Gui.boton("Registrar falla", this::falla),
                Gui.boton("Ver detalle", this::detalle)));
        c.add(barras, BorderLayout.NORTH);
        receta.setRows(5);
        var centro = new JPanel(new BorderLayout(8, 8));
        centro.add(new JScrollPane(receta), BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla));
        c.add(centro);
        p.add(c);
        add(p);
        var e = s.consultar();
        for (var pr : e.productos) {
            producto.addItem(pr.getIdProducto() + " · " + pr.getNombre());
        }
        actualizar();
    }

    /** Código del producto elegido en la lista. */
    private String idProducto() {
        Object o = producto.getSelectedItem();
        if (o == null) {
            throw new IllegalArgumentException("Registra primero un producto en Inventario.");
        }
        return o.toString().split(" · ")[0];
    }

    /** Recarga el historial de producciones y fallas. */
    public void actualizar() {
        var f = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        var ops = new java.util.ArrayList<>(s.consultar().operaciones);
        java.util.Collections.reverse(ops);
        Gui.datos(tabla, ops.stream().map(o -> {
            if (o instanceof Produccion pr) {
                return new Object[]{o.getIdOperacion(), f.format(o.getFecha()), o.getTipo(), pr.getNombreProducto(), pr.getCantidad(),
                    pr.getAprobadas(), pr.getRechazadas(), Gui.soles(pr.calcularCostoTotal()), Gui.soles(pr.calcularCostoUnitario())};
            }
            FallaProduccion fa = (FallaProduccion) o;
            return new Object[]{o.getIdOperacion(), f.format(o.getFecha()), o.getTipo(), fa.getMotivo(), "", "",
                    "", Gui.soles(fa.calcularCostoTotal()), ""};
        }).toList());
        mostrarReceta();
    }

    /** Muestra la receta del producto y su costo de materiales por pieza. */
    private void mostrarReceta() {
        if (producto.getSelectedItem() == null) {
            receta.setText("Registra productos en Inventario para definir sus recetas.");
            return;
        }
        var e = s.consultar();
        String id = idProducto();
        var r = e.recetas.stream().filter(x -> x.getIdProducto().equals(id)).findFirst().orElse(null);
        var b = new StringBuilder("RECETA (materia prima por cada pieza) · " + producto.getSelectedItem() + "\n");
        if (r == null || r.estaVacia()) {
            b.append("  Sin receta. Pulsa \"Definir receta\".");
        } else {
            double costo = 0;
            for (var i : r.getInsumos()) {
                var m = e.materias.stream().filter(x -> x.getIdMateria().equals(i.idMateria())).findFirst().orElse(null);
                if (m == null) {
                    continue;
                }
                costo += i.cantidadPorUnidad() * m.getCostoUnitario();
                b.append(String.format("  %-28s %8s %-6s  (stock %s)%n", m.getNombre(), MateriaPrima.formato(i.cantidadPorUnidad()),
                        m.getUnidad(), MateriaPrima.formato(m.getStock())));
            }
            b.append(String.format("  Costo de materiales por pieza: S/ %.2f", costo));
        }
        receta.setText(b.toString());
        receta.setCaretPosition(0);
    }

    /** Formulario con un campo por materia prima para definir la receta. */
    private void definirReceta() {
        String id = idProducto();
        var e = s.consultar();
        if (e.materias.isEmpty()) {
            throw new IllegalArgumentException("Registra primero las materias primas.");
        }
        var actual = e.recetas.stream().filter(x -> x.getIdProducto().equals(id)).findFirst().orElse(null);
        var f = new Gui.Formulario();
        var etiquetas = new LinkedHashMap<String, String>();
        for (var m : e.materias) {
            String etiqueta = m.getIdMateria() + " · " + m.getNombre() + " (" + m.getUnidad() + ")";
            etiquetas.put(etiqueta, m.getIdMateria());
            String valor = actual == null ? "" : actual.getInsumos().stream().filter(i -> i.idMateria().equals(m.getIdMateria()))
                    .map(i -> MateriaPrima.formato(i.cantidadPorUnidad())).findFirst().orElse("");
            f.campo(etiqueta, valor);
        }
        f.mostrar(this, "Receta por pieza · deja vacío lo que no se usa", () -> {
            var insumos = new LinkedHashMap<String, Double>();
            for (var et : etiquetas.entrySet()) {
                String v = f.valor(et.getKey()).trim();
                if (!v.isEmpty() && Gui.decimal(v) != 0) {
                    insumos.put(et.getValue(), Gui.decimal(v));
                }
            }
            s.produccion.guardarReceta(id, insumos);
            mostrarReceta();
        });
    }

    /** Formulario de producción; muestra la materia prima a descontar antes de confirmar. */
    private void producir() {
        String id = idProducto();
        var f = new Gui.Formulario().campo("Piezas producidas", "10").campo("Aprobadas en calidad", "10")
                .campo("Observación de calidad", "").campo("Costo de mano de obra del lote (S/)", "0.00");
        f.mostrar(this, "Registrar producción · " + producto.getSelectedItem(), () -> {
            int cantidad = Gui.entero(f.valor("Piezas producidas"));
            var necesarios = s.produccion.verificarMateriales(id, cantidad);
            var b = new StringBuilder("Materia prima que se descontará:\n");
            necesarios.forEach((k, v) -> b.append("  ").append(k).append(": ").append(v).append('\n'));
            if (!Gui.confirmar(this, b + "\n¿Registrar la producción?")) {
                return;
            }
            var pr = s.produccion.producir(id, cantidad, Gui.entero(f.valor("Aprobadas en calidad")),
                    f.valor("Observación de calidad"), Gui.decimal(f.valor("Costo de mano de obra del lote (S/)")));
            actualizar();
            JOptionPane.showMessageDialog(this, pr.describir(), "Producción registrada", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /** Formulario para registrar una falla de producción. */
    private void falla() {
        String id = idProducto();
        var f = new Gui.Formulario().campo("Piezas falladas", "1").campo("Motivo (rotura, quemado en horno...)", "");
        f.mostrar(this, "Registrar falla de producción · " + producto.getSelectedItem(), () -> {
            var fa = s.produccion.registrarFalla(id, Gui.entero(f.valor("Piezas falladas")),
                    f.valor("Motivo (rotura, quemado en horno...)"));
            actualizar();
            JOptionPane.showMessageDialog(this, fa.describir(), "Falla registrada", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /** Muestra la materia prima consumida y los costos de la operación. */
    private void detalle() {
        String id = Gui.elegido(tabla);
        var o = s.consultar().operaciones.stream().filter(x -> x.getIdOperacion().equals(id)).findFirst().orElseThrow();
        var b = new StringBuilder(o.describir()).append("\n\nMateria prima consumida:\n");
        for (var c : o.getConsumos()) {
            b.append(String.format("  %-26s %10s %-6s x S/ %8.4f = S/ %8.2f%n", c.nombre(), MateriaPrima.formato(c.cantidad()), c.unidad(),
                    c.costoUnitario(), c.subtotal()));
        }
        b.append(String.format("  Materiales: S/ %.2f", o.calcularCostoMateriales()));
        if (o instanceof Produccion pr) {
            b.append(String.format("%n  Mano de obra: S/ %.2f%n  Costo del lote: S/ %.2f%n  Costo por pieza aprobada: S/ %.2f",
                    pr.getCostoManoObra(), pr.calcularCostoTotal(), pr.calcularCostoUnitario()));
        }
        var t = Gui.texto();
        t.setText(b.toString());
        var scroll = new JScrollPane(t);
        scroll.setPreferredSize(new Dimension(820, 320));
        JOptionPane.showMessageDialog(this, scroll, "Detalle · " + id, JOptionPane.PLAIN_MESSAGE);
    }
}
