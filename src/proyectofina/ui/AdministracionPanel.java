package proyectofina.ui;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import proyectofina.servicios.Sistema;

/** Auditoria (S08 N.° 31) y cierre contable mensual (S08 N.° 22 y 24). */
public final class AdministracionPanel extends JPanel {
    private final Sistema s;
    private final JTable tabla = Gui.tabla("Fecha y hora", "Usuario", "Acción", "Detalle");
    private final JTextField desde = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10),
            hasta = new JTextField(LocalDate.now().toString(), 10), usuario = new JTextField(8),
            mes = new JTextField(YearMonth.now().minusMonths(1).toString(), 8);
    private final JLabel cerrados = new JLabel();

    /** Arma los filtros de auditoría y el cierre de mes. */
    public AdministracionPanel(Sistema s) {
        super(new BorderLayout());
        this.s = s;
        var p = Gui.pagina("Auditoría y cierre",
                "Quién hizo qué cambio y cuándo. Un mes cerrado ya no acepta ventas ni producciones con esa fecha.");
        var c = new JPanel(new BorderLayout(8, 12));
        var barras = new JPanel(new GridLayout(0, 1, 0, 4));
        barras.add(Gui.fila(new JLabel("Desde"), desde, new JLabel("Hasta"), hasta, new JLabel("Usuario"),
                usuario, Gui.boton("Consultar", this::actualizar)));
        barras.add(Gui.fila(new JLabel("Cierre contable del mes (AAAA-MM)"), mes, Gui.boton("Cerrar mes", this::cerrar), cerrados));
        c.add(barras, BorderLayout.NORTH);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(480);
        c.add(new JScrollPane(tabla));
        p.add(c);
        add(p);
        actualizar();
    }

    /** Consulta la auditoría con los filtros y muestra los meses cerrados. */
    private void actualizar() {
        LocalDate d, h;
        try {
            d = LocalDate.parse(desde.getText().trim());
            h = LocalDate.parse(hasta.getText().trim());
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Usa fechas válidas con formato AAAA-MM-DD.");
        }
        var f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Gui.datos(tabla, s.administracion.auditoria(d, h, usuario.getText()).stream()
                .map(r -> new Object[]{f.format(r.fecha()), r.usuario(), r.accion(), r.detalle()}).toList());
        var meses = s.administracion.mesesCerrados();
        cerrados.setText(meses.isEmpty() ? "Ningún mes cerrado" : "Cerrados: " + String.join(", ", meses));
    }

    /** Pide confirmación y cierra el mes indicado. */
    private void cerrar() throws Exception {
        if (Gui.confirmar(this, "¿Cerrar la contabilidad de " + mes.getText().trim()
                + "?\nNo se podrán registrar más operaciones con fecha de ese mes.")) {
            s.administracion.cerrarMes(mes.getText());
            actualizar();
        }
    }
}
