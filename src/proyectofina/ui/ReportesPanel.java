package proyectofina.ui;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import proyectofina.servicios.*;
public final class ReportesPanel extends JPanel {
    private final ReportesServicio servicio;
    private final JComboBox<String> tipo=new JComboBox<>(ReportesServicio.TIPOS);
    private final JTextField desde=new JTextField(LocalDate.now().withDayOfMonth(1).toString(),10),hasta=new JTextField(LocalDate.now().toString(),10);
    private final JTextArea contenido=Gui.texto();
    private final JLabel estado=new JLabel("Las fechas filtran el reporte de movimientos.");
    public ReportesPanel(Sistema s) {
        super(new BorderLayout());
        servicio=new ReportesServicio(s);
        var p=Gui.pagina("Reportes","Inventario, ventas y movimientos. Exporta y abre los resultados en PDF.");
        var c=new JPanel(new BorderLayout(8,12));
        var barras=new JPanel(new GridLayout(0,1,0,4));
        barras.add(Gui.fila(new JLabel("Reporte"),tipo,Gui.boton("Consultar",this::generar),Gui.boton("Exportar PDF",this::exportar)));
        barras.add(Gui.fila(new JLabel("Movimientos desde (AAAA-MM-DD)"),desde,new JLabel("hasta"),hasta));
        c.add(barras,BorderLayout.NORTH);
        c.add(new JScrollPane(contenido));
        c.add(estado,BorderLayout.SOUTH);
        p.add(c);
        add(p);
        tipo.addActionListener(e->Gui.intentar(this,this::generar));
        generar();
    }
    private void generar() {
        try {
            contenido.setText(servicio.contenido((String)tipo.getSelectedItem(),LocalDate.parse(desde.getText()),LocalDate.parse(hasta.getText())));
            contenido.setCaretPosition(0);
        }
        catch(java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Usa fechas válidas con formato AAAA-MM-DD.");
        }
    }
    private void exportar() throws Exception {
        generar();
        var f=servicio.exportar((String)tipo.getSelectedItem(),contenido.getText(),"PDF");
        estado.setText("Guardado: "+f.getAbsolutePath());
        Gui.abrirPdfGuardado(this,f);
    }
}
