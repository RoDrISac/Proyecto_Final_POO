package proyectofina.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import proyectofina.servicios.*;

/** Consulta y exportación de reportes. */
public final class ReportesPanel extends JPanel {
    private final ReportesServicio servicio;
    private final JComboBox<String> tipo = new JComboBox<>(ReportesServicio.TIPOS);
    private final JTextField desde = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10);
    private final JTextField hasta = new JTextField(LocalDate.now().toString(), 10);
    private final JTextArea contenido = Gui.texto();
    private final JLabel estado = new JLabel("Las fechas filtran los reportes de movimientos y de producción.");

    /** Arma el selector de reporte, las fechas y el área de texto. */
    public ReportesPanel(Sistema s) {
        super(new BorderLayout());
        servicio = new ReportesServicio(s);
        var p = Gui.pagina("Reportes", "Inventario, ventas, producción y movimientos. Exporta y abre los resultados en PDF.");
        var c = new JPanel(new BorderLayout(8, 12));
        var barras = new JPanel(new GridLayout(0, 1, 0, 4));
        barras.add(Gui.fila(new JLabel("Reporte"), tipo, Gui.boton("Consultar", this::generar), Gui.boton("Exportar PDF", this::exportar)));
        barras.add(Gui.fila(new JLabel("Desde (AAAA-MM-DD)"), desde, new JLabel("hasta"), hasta));
        c.add(barras, BorderLayout.NORTH);
        c.add(new JScrollPane(contenido));
        c.add(estado, BorderLayout.SOUTH);
        p.add(c);
        add(p);
        tipo.addActionListener(e -> Gui.intentar(this, this::generar));
        generar();
    }

    /** Muestra el reporte elegido; valida el formato de las fechas. */
    private void generar() {
        try {
            contenido.setText(servicio.contenido((String) tipo.getSelectedItem(),
                    LocalDate.parse(desde.getText().trim()), LocalDate.parse(hasta.getText().trim())));
            contenido.setCaretPosition(0);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Usa fechas válidas con formato AAAA-MM-DD.");
        }
    }

    /** Guarda el reporte en PDF y lo abre. */
    private void exportar() throws Exception {
        generar();
        var f = servicio.exportar((String) tipo.getSelectedItem(), contenido.getText(), "PDF");
        estado.setText("Guardado en " + f.getAbsolutePath());
        Gui.abrirPdfGuardado(this, f);
    }
}
