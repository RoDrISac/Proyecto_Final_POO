package proyectofina.ui;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.awt.print.*;
import proyectofina.*;
import proyectofina.servicios.Sistema;
public final class HistorialPanel extends JPanel {
    private final Sistema s;
    private final JTable tabla=Gui.tabla("Venta","Fecha","Cliente","Pago","Tipo","Total");
    public HistorialPanel(Sistema s) {
        super(new BorderLayout());
        this.s=s;
        var p=Gui.pagina("Historial de ventas","Consulta el detalle, vuelve a generar el PDF o imprime un comprobante.");
        var c=new JPanel(new BorderLayout(8,12));
        c.add(Gui.fila(Gui.boton("Ver detalle",this::detalle),Gui.boton("Generar y abrir PDF",()->Gui.abrir(s.ventas.comprobante(venta()))),Gui.boton("Imprimir",this::imprimir)),BorderLayout.NORTH);
        c.add(new JScrollPane(tabla));
        p.add(c);
        add(p);
        actualizar();
    }
    public void actualizar() {
        Gui.datos(tabla,s.consultar().ventas.stream().sorted(java.util.Comparator.comparing(Venta::getFecha).reversed()).map(v->new Object[] {
            v.getIdVenta(),new SimpleDateFormat("dd/MM/yyyy HH:mm").format(v.getFecha()),v.getCliente().getNombreCompleto(),v.getMetodoPago(),v.getTipoComprobante(),Gui.soles(v.getMontoTotal())
        }
        ).toList());
    }
    private Venta venta() {
        String id=Gui.elegido(tabla);
        return s.consultar().ventas.stream().filter(v->v.getIdVenta().equals(id)).findFirst().orElseThrow();
    }
    private void detalle() {
        var t=Gui.texto();
        t.setText(venta().getResumenVenta());
        var scroll=new JScrollPane(t);
        scroll.setPreferredSize(new Dimension(780,350));
        JOptionPane.showMessageDialog(this,scroll,"Detalle de venta",JOptionPane.PLAIN_MESSAGE);
    }
    private void imprimir() throws PrinterException {
        s.exigir("VENTAS");
        var lineas=new Comprobante(venta()).generarLineas();
        PrinterJob job=PrinterJob.getPrinterJob();
        job.setJobName("Comprobante Colores y Creaciones");
        job.setPrintable((g,f,pagina)-> {
            int porPagina=Math.max(1,(int)(f.getImageableHeight()/13)); int inicio=pagina*porPagina; if(inicio>=lineas.size())return Printable.NO_SUCH_PAGE; g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,9)); for(int i=inicio; i<Math.min(inicio+porPagina,lineas.size()); i++)g.drawString(lineas.get(i),(int)f.getImageableX(),(int)f.getImageableY()+12+(i-inicio)*13); return Printable.PAGE_EXISTS;
        }
        );
        if(job.printDialog())job.print();
    }
}
