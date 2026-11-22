package proyectofina.ui;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import proyectofina.MateriaPrima;
import proyectofina.servicios.*;

/** Materias primas del taller (S08 N.° 2): registro, edicion, compras, baja y kardex. */
public final class MateriasPrimasPanel extends JPanel {
    private final Sistema s;
    private final JTable tabla = Gui.tabla("Código", "Materia prima", "Unidad", "Stock", "Mínimo", "Costo unit.", "Valor", "Estado");
    private final JLabel resumen = new JLabel();

    /** Arma la barra de acciones, la tabla y el resumen. */
    public MateriasPrimasPanel(Sistema s) {
        super(new BorderLayout());
        this.s = s;
        var p = Gui.pagina("Materias primas",
                "Arcilla, esmaltes, pigmentos y demás insumos del taller. El costo se promedia en cada compra.");
        var c = new JPanel(new BorderLayout(8, 12));
        c.add(Gui.fila(Gui.boton("Nueva", () -> formulario(null)), Gui.boton("Editar", () -> formulario(seleccion())),
                Gui.boton("Registrar compra", this::comprar), Gui.boton("Eliminar", this::eliminar), Gui.boton("Kardex", this::kardex)),
                BorderLayout.NORTH);
        c.add(new JScrollPane(tabla));
        c.add(resumen, BorderLayout.SOUTH);
        p.add(c);
        add(p);
        actualizar();
    }

    /** Recarga la tabla y el valor total del almacén. */
    public void actualizar() {
        var materias = s.consultar().materias;
        Gui.datos(tabla, materias.stream().map(m -> new Object[]{m.getIdMateria(), m.getNombre(), m.getUnidad(),
            MateriaPrima.formato(m.getStock()), MateriaPrima.formato(m.getStockMinimo()), Gui.soles(m.getCostoUnitario()),
            Gui.soles(m.getStock() * m.getCostoUnitario()), m.tieneStockBajo() ? "STOCK BAJO" : "Disponible"}).toList());
        resumen.setText(materias.size() + " materias primas  ·  " + materias.stream().filter(MateriaPrima::tieneStockBajo).count()
                + " con stock bajo  ·  Valor del almacén: "
                + Gui.soles(materias.stream().mapToDouble(m -> m.getStock() * m.getCostoUnitario()).sum()));
    }

    /** Materia prima de la fila seleccionada. */
    private MateriaPrima seleccion() {
        String id = Gui.elegido(tabla);
        return s.consultar().materias.stream().filter(m -> m.getIdMateria().equals(id)).findFirst().orElseThrow();
    }

    /** Formulario para registrar (m = null) o editar una materia prima. */
    private void formulario(MateriaPrima m) {
        var f = new Gui.Formulario().campo("Código", m == null ? "" : m.getIdMateria()).campo("Nombre", m == null ? "" : m.getNombre())
                .opciones("Unidad", MateriaPrima.UNIDADES, m == null ? "kg" : m.getUnidad())
                .campo("Stock inicial", m == null ? "0" : MateriaPrima.formato(m.getStock()))
                .campo("Stock mínimo", m == null ? "5" : MateriaPrima.formato(m.getStockMinimo()))
                .campo("Costo unitario (S/)", m == null ? "0.00" : String.valueOf(m.getCostoUnitario()));
        f.mostrar(this, m == null ? "Registrar materia prima" : "Editar materia prima (el stock cambia con compras y consumos)", () -> {
            if (m != null && !f.valor("Código").trim().equalsIgnoreCase(m.getIdMateria())) {
                throw new IllegalArgumentException("El código no se puede cambiar. Conserva " + m.getIdMateria());
            }
            if (m != null && Gui.decimal(f.valor("Stock inicial")) != m.getStock()) {
                throw new IllegalArgumentException("Usa Registrar compra para cambiar el stock.");
            }
            s.produccion.guardarMateria(new ProduccionServicio.DatosMateria(f.valor("Código"), f.valor("Nombre"), f.valor("Unidad"),
                    Gui.decimal(f.valor("Stock inicial")), Gui.decimal(f.valor("Stock mínimo")),
                    Gui.decimal(f.valor("Costo unitario (S/)"))), m == null);
            actualizar();
        });
    }

    /** Formulario de compra de la materia prima seleccionada. */
    private void comprar() {
        var m = seleccion();
        var f = new Gui.Formulario().campo("Cantidad (" + m.getUnidad() + ")", "1")
                .campo("Costo unitario (S/)", String.valueOf(m.getCostoUnitario()));
        f.mostrar(this, "Compra de " + m.getNombre(), () -> {
            s.produccion.ingresarMateria(m.getIdMateria(), Gui.decimal(f.valor("Cantidad (" + m.getUnidad() + ")")),
                    Gui.decimal(f.valor("Costo unitario (S/)")));
            actualizar();
        });
    }

    /** Pide confirmación y elimina la materia prima seleccionada. */
    private void eliminar() throws Exception {
        String id = Gui.elegido(tabla);
        if (Gui.confirmar(this, "¿Eliminar la materia prima " + id + "?")) {
            s.produccion.eliminarMateria(id);
            actualizar();
        }
    }

    /** Muestra los movimientos de la materia prima seleccionada. */
    private void kardex() {
        String id = Gui.elegido(tabla);
        var f = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        var b = new StringBuilder("KARDEX DE " + id + "\n\nFecha            | Usuario | Tipo     | Cantidad   | Saldo      | Referencia\n");
        for (var m : s.consultar().movimientosMateria) {
            if (m.materia().equals(id)) {
                b.append(String.format("%-16s | %-7s | %-8s | %10s | %10s | %s%n", f.format(m.fecha()), m.usuario(), m.tipo(),
                        MateriaPrima.formato(m.cantidad()), MateriaPrima.formato(m.saldo()), m.referencia()));
            }
        }
        var t = Gui.texto();
        t.setText(b.toString());
        var scroll = new JScrollPane(t);
        scroll.setPreferredSize(new Dimension(820, 380));
        JOptionPane.showMessageDialog(this, scroll, "Kardex · " + id, JOptionPane.PLAIN_MESSAGE);
    }
}
