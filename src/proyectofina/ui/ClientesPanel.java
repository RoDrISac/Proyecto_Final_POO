package proyectofina.ui;
import javax.swing.*;
import java.awt.*;
import proyectofina.servicios.Sistema;
public final class ClientesPanel extends JPanel {
    private final Sistema s;
    private final JTable tabla=Gui.tabla("Código","Nombre","DNI","Teléfono","Correo");
    public ClientesPanel(Sistema s) {
        super(new BorderLayout());
        this.s=s;
        var p=Gui.pagina("Clientes","Datos de contacto e historial de compras, reunidos en un solo lugar.");
        var c=new JPanel(new BorderLayout(8,12));
        c.add(Gui.fila(Gui.boton("Nuevo cliente",this::nuevo),Gui.boton("Historial de compras",this::historial)),BorderLayout.NORTH);
        c.add(new JScrollPane(tabla));
        p.add(c);
        add(p);
        actualizar();
    }
    public void actualizar() {
        Gui.datos(tabla,s.consultar().clientes.stream().map(c->new Object[] {
            c.getIdCliente(),c.getNombreCompleto(),c.getDni(),c.getTelefono(),c.getCorreoElectronico()
        }
        ).toList());
    }
    private void nuevo() {
        var f=new Gui.Formulario().campo("Código","").campo("Nombre","").campo("Apellido","").campo("DNI","").campo("Dirección","").campo("Teléfono","").campo("Correo","");
        f.mostrar(this,"Registrar cliente",()-> {
            s.clientes.registrar(f.valor("Código"),f.valor("Nombre"),f.valor("Apellido"),f.valor("DNI"),f.valor("Dirección"),f.valor("Teléfono"),f.valor("Correo")); actualizar();
        }
        );
    }
    private void historial() {
        String id=Gui.elegido(tabla);
        StringBuilder b=new StringBuilder();
        var ventas=s.consultar().ventas.stream().filter(v->v.getCliente().getIdCliente().equals(id)).toList();
        for(var v:ventas)b.append(v.getResumenVenta()).append("\n\n");
        b.append("Total: ").append(Gui.soles(ventas.stream().mapToDouble(v->v.getMontoTotal()).sum()));
        var texto=Gui.texto();
        texto.setText(b.toString());
        var scroll=new JScrollPane(texto);
        scroll.setPreferredSize(new Dimension(780,400));
        JOptionPane.showMessageDialog(this,scroll,"Historial · "+id,JOptionPane.PLAIN_MESSAGE);
    }
}
