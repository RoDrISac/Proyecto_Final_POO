package proyectofina.ui;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import proyectofina.*;
import proyectofina.servicios.*;
public final class VentasPanel extends JPanel {
    private final Sistema s;
    private final LinkedHashMap<String,Integer> carrito=new LinkedHashMap<>();
    private final JTable tabla=Gui.tabla("Código","Producto","Modelo","Cantidad","Precio","Subtotal");
    private final JComboBox<String> clientes=new JComboBox<>(),producto=new JComboBox<>(),pago=new JComboBox<>(VentasServicio.PAGOS);
    private final JLabel tipo=new JLabel(
        "Boleta"
    );
    private final JTextField codigo=new JTextField(12);
    private final JSpinner cantidad=new JSpinner(new SpinnerNumberModel(1,1,1000000,1));
    private final JLabel total=new JLabel("Total: S/ 0.00"),estado=new JLabel("Agrega productos. El stock se descuenta solo al confirmar.");
    public VentasPanel(Sistema s) {
        super(new BorderLayout());
        this.s=s;
        var p=Gui.pagina("Nueva venta","Cliente, productos y pago. Revisa el detalle antes de confirmar.");
        var c=new JPanel(new BorderLayout(8,14));
        var superior=new JPanel(new GridLayout(0,1,0,8));
        clientes.setPreferredSize(new Dimension(300,34));
        producto.setPreferredSize(new Dimension(260,34));
        pago.setPreferredSize(new Dimension(155,34));
        tipo.setText("BOLETA");
        tipo.setHorizontalAlignment(SwingConstants.CENTER);
        tipo.setFont(new Font("Segoe UI",Font.BOLD,13));
        tipo.setForeground(Gui.ACENTO);
        tipo.setOpaque(true);
        tipo.setBackground(new Color(225,241,236));
        tipo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(167,205,192),1,true),
            BorderFactory.createEmptyBorder(8,18,8,18)));
        tipo.setPreferredSize(new Dimension(130,36));
        superior.add(Gui.fila(new JLabel("Cliente"),clientes));
        superior.add(Gui.fila(new JLabel("Pago"),pago,new JLabel("Comprobante"),tipo));
        superior.add(Gui.fila(new JLabel("Producto"),producto,Gui.boton("Elegir",()-> {
            if(producto.getSelectedItem()!=null)codigo.setText(id(producto));
        }
        )));
        superior.add(Gui.fila(new JLabel("Código / lector"),codigo,new JLabel("Cantidad"),cantidad,Gui.boton("Agregar",this::agregar)));
        codigo.setName("codigoVenta");
        cantidad.setName("cantidadVenta");
        codigo.addActionListener(e->Gui.intentar(this,this::agregar));
        c.add(superior,BorderLayout.NORTH);
        c.add(new JScrollPane(tabla));
        var inferior=new JPanel(new GridLayout(0,1,0,8));
        total.setFont(new Font("Segoe UI",Font.BOLD,22));
        inferior.add(Gui.fila(Gui.boton("Quitar seleccionado",()-> {
            carrito.remove(Gui.elegido(tabla)); actualizarCarrito();
        }
        ),Gui.boton("Vaciar",()-> {
            if(Gui.confirmar(this,"¿Vaciar el carrito?")) {
                carrito.clear(); actualizarCarrito();
            }
        }
        ),total,Gui.boton("Confirmar venta",this::confirmar)));
        inferior.add(estado);
        c.add(inferior,BorderLayout.SOUTH);
        p.add(c);
        add(p);
        actualizarCatalogos();
    }
    private static String id(JComboBox<String> c) {
        Object o=c.getSelectedItem();
        if(o==null)throw new IllegalArgumentException("Selecciona un registro.");
        return o.toString().split(" · ")[0];
    }
    public void actualizarCatalogos() {
        var cliente=clientes.getSelectedItem();
        var prod=producto.getSelectedItem();
        clientes.removeAllItems();
        producto.removeAllItems();
        var e=s.consultar();
        for(var c:e.clientes)clientes.addItem(c.getIdCliente()+" · "+c.getNombreCompleto());
        for(var p:e.productos)producto.addItem(p.getIdProducto()+" · "+p.getNombre());
        if(cliente!=null)clientes.setSelectedItem(cliente);
        if(prod!=null)producto.setSelectedItem(prod);
        actualizarCarrito();
    }
    private void agregar() throws java.text.ParseException {
        cantidad.commitEdit();
        String id=codigo.getText().trim();
        var p=s.consultar().productos.stream().filter(x->x.getIdProducto().equalsIgnoreCase(id)).findFirst().orElseThrow(()->new IllegalArgumentException("No existe el código ingresado."));
        int n=((Number)cantidad.getValue()).intValue()+carrito.getOrDefault(p.getIdProducto(),0);
        if(n>p.getStock())throw new IllegalArgumentException("Stock insuficiente. Disponible: "+p.getStock()+"; ya agregado: "+carrito.getOrDefault(p.getIdProducto(),0));
        carrito.put(p.getIdProducto(),n);
        actualizarCarrito();
        estado.setText("Agregado: "+p.getNombre()+" · "+p.getModelo());
        codigo.setText("");
        codigo.requestFocusInWindow();
    }
    private void actualizarCarrito() {
        var filas=new ArrayList<Object[]>();
        double suma=0;
        for(var item:carrito.entrySet()) {
            var p=s.consultar().productos.stream().filter(x->x.getIdProducto().equals(item.getKey())).findFirst().orElse(null);
            if(p==null)continue;
            double sub=p.getPrecio()*item.getValue();
            suma+=sub;
            filas.add(new Object[] {
                p.getIdProducto(),p.getNombre(),p.getModelo(),item.getValue(),Gui.soles(p.getPrecio()),Gui.soles(sub)
            }
            );
        }
        Gui.datos(tabla,filas);
        total.setText("Total: "+Gui.soles(suma));
    }
    private void confirmar() throws Exception {
        if(carrito.isEmpty())throw new IllegalArgumentException("Agrega al menos un producto.");
        String cliente=id(clientes);
        if(!Gui.confirmar(this,"¿Registrar la venta? "+total.getText()+"\nSe descontará el stock de los productos."))return;
        Venta v=s.ventas.confirmar(cliente,carrito,(String)pago.getSelectedItem(),VentasServicio.COMPROBANTE);
        carrito.clear();
        actualizarCarrito();
        estado.setText("Venta "+v.getIdVenta()+" registrada · "+Gui.soles(v.getMontoTotal()));
        try {
            var pdf=s.ventas.comprobante(v);
            estado.setText("Venta "+v.getIdVenta()+" registrada · PDF: "+pdf.getName());
            Gui.abrirPdfGuardado(this,pdf);
        }
        catch(java.io.IOException e) {
            JOptionPane.showMessageDialog(this,"La venta SÍ se guardó. No repitas la venta.\nEl PDF no pudo generarse; reintenta desde Historial.\n"+e.getMessage());
        }
    }
}
