package proyectofina.ui;
import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import proyectofina.*;
import proyectofina.servicios.*;
public final class InventarioPanel extends JPanel {
    private final Sistema s;
    private final JTable tabla=Gui.tabla("Código","Producto","Modelo","Unidad","Precio","Stock","Mínimo","Estado");
    private final JTextField buscar=new JTextField(18);
    private final JLabel resumen=new JLabel();
    public InventarioPanel(Sistema s) {
        super(new BorderLayout());
        this.s=s;
        var p=Gui.pagina("Inventario","Busca por código, modelo o nombre. El lector puede escribir el código y pulsar Enter.");
        var cuerpo=new JPanel(new BorderLayout(8,12));
        var barra=Gui.fila(new JLabel("Buscar"),buscar,Gui.boton("Buscar",this::actualizar),Gui.boton("Ver todo",()-> {
            buscar.setText(""); actualizar();
        }
        ));
        if(s.permite("INVENTARIO")) {
            barra.add(Gui.boton("Nuevo",()->editar(null)));
            barra.add(Gui.boton("Editar",()->editar(seleccion())));
            barra.add(Gui.boton("Ingresar stock",this::ingresar));
            barra.add(Gui.boton("Eliminar",()-> {
                String id=Gui.elegido(tabla); if(Gui.confirmar(this,"¿Eliminar "+id+" del inventario? Sus ventas se conservarán.")) {
                    s.inventario.eliminar(id); actualizar();
                }
            }
            ));
        }
        buscar.addActionListener(e->actualizar());
        var barras=new JPanel(new GridLayout(0,1,0,4));
        var acciones=Gui.fila();
        while(barra.getComponentCount()>4)acciones.add(barra.getComponent(4));
        barras.add(barra);
        if(acciones.getComponentCount()>0)barras.add(acciones);
        cuerpo.add(barras,BorderLayout.NORTH);
        cuerpo.add(new JScrollPane(tabla));
        cuerpo.add(resumen,BorderLayout.SOUTH);
        p.add(cuerpo);
        add(p);
        actualizar();
    }
    private Inventario seleccion() {
        String id=Gui.elegido(tabla);
        return s.consultar().productos.stream().filter(p->p.getIdProducto().equals(id)).findFirst().orElseThrow();
    }
    public void actualizar() {
        String filtro=buscar.getText().trim().toLowerCase(Locale.ROOT);
        var productos=s.consultar().productos.stream().filter(p->(p.getIdProducto()+" "+p.getNombre()+" "+p.getModelo()).toLowerCase(Locale.ROOT).contains(filtro)).toList();
        Gui.datos(tabla,productos.stream().map(p->new Object[] {
            p.getIdProducto(),p.getNombre(),p.getModelo(),p.getUnidad(),Gui.soles(p.getPrecio()),p.getStock(),p.getStockMinimo(),p.tieneStockBajo()?"STOCK BAJO":"Disponible"
        }
        ).toList());
        resumen.setText(productos.size()+" productos  ·  "+productos.stream().filter(Inventario::tieneStockBajo).count()+" con stock bajo");
        if(tabla.getRowCount()==1)tabla.setRowSelectionInterval(0,0);
    }
    private void ingresar() {
        var p=seleccion();
        var f=new Gui.Formulario().campo("Cantidad","1");
        f.mostrar(this,"Ingreso de stock · "+p.getNombre(),()-> {
            s.inventario.ingresar(p.getIdProducto(),Gui.entero(f.valor("Cantidad"))); actualizar();
        }
        );
    }
    private void editar(Inventario p) {
        var f=new Gui.Formulario().campo("Código",p==null?"":p.getIdProducto()).campo("Nombre",p==null?"":p.getNombre()).campo("Modelo",p==null?"":p.getModelo()).campo("Categoría",p==null?"Textil":p.getCategoria()).campo("Unidad",p==null?"Unidad":p.getUnidad()).campo("Descripción",p==null?"":p.getDescripcion()).campo("Precio",p==null?"0.00":""+p.getPrecio()).campo("Costo",p==null?"0.00":""+p.getCosto()).campo("Stock inicial",p==null?"0":""+p.getStock()).campo("Stock mínimo",p==null?"3":""+p.getStockMinimo());
        f.mostrar(this,p==null?"Registrar producto":"Editar producto (stock se cambia mediante ingresos)",()-> {
            if(p!=null&&!f.valor("Código").equals(p.getIdProducto()))throw new IllegalArgumentException("El código no se puede cambiar. Conserva "+p.getIdProducto());              if(p!=null&&Gui.entero(f.valor("Stock inicial"))!=p.getStock())throw new IllegalArgumentException("Usa Ingresar stock para registrar un movimiento.");              s.inventario.guardar(new InventarioServicio.Datos(f.valor("Código"),f.valor("Nombre"),f.valor("Modelo"),f.valor("Categoría"),f.valor("Unidad"),f.valor("Descripción"),Gui.decimal(f.valor("Precio")),Gui.decimal(f.valor("Costo")),Gui.entero(f.valor("Stock inicial")),Gui.entero(f.valor("Stock mínimo"))),p==null); actualizar();
        }
        );
    }
}
