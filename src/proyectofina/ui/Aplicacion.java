package proyectofina.ui;
import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.function.Supplier;
import proyectofina.servicios.*;
/** Ventana y navegación; cada pantalla administra su propia presentación. */
public final class Aplicacion extends JFrame {
    private final Sistema sistema;
    private JPanel principal;
    private final JLabel estado=new JLabel(" ");
    private VentasPanel ventas;
    private final LinkedHashMap<String,JButton> navegacion=new LinkedHashMap<>();
    public Aplicacion(Sistema sistema) {
        super("Colores y Creaciones · T3");
        this.sistema=sistema;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1100,700));
        setSize(1280,790);
        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                cerrar();
            }
        }
        );
        login();
    }
    private void cerrar() {
        if(Gui.confirmar(this,"¿Cerrar la aplicación? Las operaciones confirmadas ya están guardadas; el carrito pendiente se descartará.")) {
            try {
                new ReportesServicio(sistema).automaticos();
            }
            catch(Exception ignored) {
            }
            dispose();
        }
    }
    public void login() {
        ventas=null;
        var raiz=new JPanel(new GridLayout(1,2));
        var marca=new JPanel(new GridBagLayout());
        marca.setBackground(Gui.TINTA);
        marca.add(new JLabel("<html><div style='width:330px;color:#ffffff'><p style='font-size:12px;color:#82cabc'>COLORES Y CREACIONES</p><br><p style='font-size:34px'>El trabajo artesanal,<br>bien organizado.</p><br><p style='font-size:16px;color:#d3e3e5'>Inventario, ventas y clientes<br>en un mismo lugar.</p></div></html>"));
        raiz.add(marca);
        var lado=new JPanel(new GridBagLayout());
        var form=new JPanel(new GridLayout(0,1,0,12));
        form.setPreferredSize(new Dimension(350,360));
        var titulo=new JLabel("Bienvenido de nuevo");
        titulo.setFont(new Font("Segoe UI",Font.BOLD,26));
        form.add(titulo);
        form.add(new JLabel("Ingresa con tu código de usuario."));
        var usuario=new JTextField();
        usuario.setName("usuario");
        var clave=new JPasswordField();
        clave.setName("clave");
        var lu=new JLabel("Código de usuario");
        lu.setLabelFor(usuario);
        var lc=new JLabel("Contraseña");
        lc.setLabelFor(clave);
        form.add(lu);
        form.add(usuario);
        form.add(lc);
        form.add(clave);
        var mensaje=new JLabel(" ");
        mensaje.setForeground(new Color(170,60,40));
        var entrar=new JButton("Ingresar");
        entrar.addActionListener(e-> {
            try {
                sistema.ingresar(usuario.getText(),new String(clave.getPassword())); inicio();
            }
            catch(Exception ex) {
                mensaje.setText(ex.getMessage()); clave.setText("");
            }
        }
        );
        form.add(entrar);
        form.add(mensaje);
        lado.add(form);
        raiz.add(lado);
        setContentPane(raiz);
        getRootPane().setDefaultButton(entrar);
        revalidate();
        repaint();
        SwingUtilities.invokeLater(usuario::requestFocusInWindow);
    }
    public void inicio() {
        var raiz=new JPanel(new BorderLayout());
        var lateral=new JPanel();
        lateral.setLayout(new BoxLayout(lateral,BoxLayout.Y_AXIS));
        lateral.setBackground(Gui.TINTA);
        lateral.setBorder(BorderFactory.createEmptyBorder(28,18,20,18));
        lateral.setPreferredSize(new Dimension(222,0));
        lateral.add(new JLabel("<html><span style='color:#88d0be;font-size:12px'>COLORES Y</span><br><span style='color:white;font-size:21px'>Creaciones</span></html>"));
        lateral.add(Box.createVerticalStrut(32));
        principal=new JPanel(new BorderLayout());
        navegacion.clear();
        agregar(lateral,"Resumen",this::resumen);
        agregar(lateral,"Inventario",()->new InventarioPanel(sistema));
        if(sistema.permite("VENTAS")) {
            agregar(lateral,"Clientes",()->new ClientesPanel(sistema));
            agregar(lateral,"Nueva venta",()-> {
                if(ventas==null)ventas=new VentasPanel(sistema); else ventas.actualizarCatalogos(); return ventas;
            }
            );
            agregar(lateral,"Historial",()->new HistorialPanel(sistema));
        }
        if(sistema.permite("REPORTES"))agregar(lateral,"Reportes",()->new ReportesPanel(sistema));
        if(sistema.permite("USUARIOS"))agregar(lateral,"Usuarios",()->new UsuariosPanel(sistema));
        lateral.add(Box.createVerticalGlue());
        var u=sistema.usuario();
        lateral.add(new JLabel("<html><span style='color:white'>"+u.getNombreCompleto()+"</span><br><span style='color:#9ab4bd'>"+u.getRol().getNombreRol()+"</span></html>"));
        lateral.add(Box.createVerticalStrut(16));
        lateral.add(Gui.boton("Cerrar sesión",()-> {
            if(Gui.confirmar(this,"¿Cerrar sesión? Se descartará el carrito pendiente.")) {
                new ReportesServicio(sistema).automaticos(); sistema.salir(); login();
            }
        }
        ));
        raiz.add(lateral,BorderLayout.WEST);
        raiz.add(principal);
        estado.setBorder(BorderFactory.createEmptyBorder(8,18,8,18));
        raiz.add(estado,BorderLayout.SOUTH);
        setContentPane(raiz);
        getRootPane().setDefaultButton(null);
        navegacion.get("Resumen").doClick();
        revalidate();
        repaint();
        try {
            new ReportesServicio(sistema).automaticos();
            estado.setText("Sesión iniciada · Operaciones confirmadas guardadas automáticamente");
        }
        catch(Exception e) {
            estado.setText("Sesión iniciada · Reportes automáticos pendientes: "+e.getMessage());
        }
    }
    private void agregar(JPanel lateral,String titulo,Supplier<JPanel> fabrica) {
        JButton b=Gui.boton(titulo,()-> {
            principal.removeAll(); principal.add(fabrica.get()); navegacion.forEach((n,boton)->boton.setFont(new Font("Segoe UI",n.equals(titulo)?Font.BOLD:Font.PLAIN,14))); principal.revalidate(); principal.repaint(); estado.setText(titulo+" · Los cambios confirmados se guardan automáticamente");
        }
        );
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(188,42));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        lateral.add(b);
        lateral.add(Box.createVerticalStrut(8));
        navegacion.put(titulo,b);
    }
    private JPanel resumen() {
        var p=Gui.pagina("Tu negocio, de un vistazo","Controla las existencias y continúa con la siguiente operación.");
        var e=sistema.consultar();
        var centro=new JPanel(new BorderLayout(16,24));
        var tarjetas=new JPanel(new GridLayout(1,3,16,0));
        tarjetas.add(tarjeta("Productos",""+e.productos.size(),"Modelos en inventario"));
        tarjetas.add(tarjeta("Stock bajo",""+e.productos.stream().filter(x->x.tieneStockBajo()).count(),"Revisa las alertas de reposición"));
        boolean verVentas=sistema.permite("VENTAS")||sistema.permite("REPORTES");
        tarjetas.add(tarjeta(verVentas?"Ventas registradas":"Productos disponibles",""+(verVentas?e.ventas.size():e.productos.stream().filter(x->x.estaDisponible()).count()),verVentas?"Consulta el historial":"Con unidades para vender"));
        centro.add(tarjetas,BorderLayout.NORTH);
        var alertas=Gui.tabla("Código","Producto","Stock","Mínimo");
        Gui.datos(alertas,e.productos.stream().filter(x->x.tieneStockBajo()).map(x->new Object[] {
            x.getIdProducto(),x.getNombre(),x.getStock(),x.getStockMinimo()
        }
        ).toList());
        var cuerpo=new JPanel(new BorderLayout(10,12));
        var t=new JLabel("Productos que necesitan atención");
        t.setFont(new Font("Segoe UI",Font.BOLD,18));
        cuerpo.add(t,BorderLayout.NORTH);
        cuerpo.add(new JScrollPane(alertas));
        centro.add(cuerpo);
        p.add(centro);
        return p;
    }
    private JPanel tarjeta(String nombre,String valor,String nota) {
        var p=new JPanel(new GridLayout(0,1,0,8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(221,229,232)),BorderFactory.createEmptyBorder(18,22,18,22)));
        p.add(new JLabel(nombre));
        var v=new JLabel(valor);
        v.setFont(new Font("Segoe UI",Font.BOLD,34));
        v.setForeground(Gui.ACENTO);
        p.add(v);
        var sub=new JLabel(nota);
        sub.setForeground(Color.GRAY);
        p.add(sub);
        return p;
    }
}
