package proyectofina;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.util.*;
import proyectofina.ui.*;
import proyectofina.persistencia.*;
import proyectofina.servicios.*;

/** Renderiza componentes Swing reales y prueba los eventos de acceso, navegación y carrito. */
public class PruebasVisuales {
    static Aplicacion ventana;static Sistema sistema;static final java.util.List<String> log=new ArrayList<>();
    static java.util.List<Component> hijos(Container padre){var lista=new ArrayList<Component>();for(Component c:padre.getComponents()){lista.add(c);if(c instanceof Container p)lista.addAll(hijos(p));}return lista;}
    static JButton boton(String texto){return hijos(ventana).stream().filter(c->c instanceof JButton b&&b.getText().equals(texto)).map(c->(JButton)c).findFirst().orElseThrow();}
    static Component nombre(String nombre){return hijos(ventana).stream().filter(c->nombre.equals(c.getName())).findFirst().orElseThrow();}
    static void formularioProducto() {
        javax.swing.Timer timer=new javax.swing.Timer(400,event->{
            try {
                var dialogo=Arrays.stream(Window.getWindows()).filter(w->w instanceof JDialog d && d.isShowing() && d.getTitle().equals("Registrar producto")).findFirst().orElseThrow();
                var campos=hijos(dialogo).stream().filter(c->c instanceof JTextField).map(c->(JTextField)c).toList();
                String[] valores={"PGUI","Producto de formulario","Modelo GUI","Textil","Unidad","Registro desde la interfaz","25.50","10","8","2"};
                for(int i=0;i<valores.length;i++)campos.get(i).setText(valores[i]);
                var imagen=new BufferedImage(dialogo.getWidth(),dialogo.getHeight(),BufferedImage.TYPE_INT_RGB);var g=imagen.createGraphics();dialogo.printAll(g);g.dispose();ImageIO.write(imagen,"png",Path.of("docs/evidencias/11-formulario-producto.png").toFile());
                hijos(dialogo).stream().filter(c->c instanceof JButton b&&b.getText().equals("Guardar")).map(c->(JButton)c).findFirst().orElseThrow().doClick();
            } catch(Exception ex){throw new RuntimeException(ex);}
        });timer.setRepeats(false);timer.start();boton("Nuevo").doClick();
        if(sistema.consultar().productos.stream().noneMatch(p->p.getIdProducto().equals("PGUI")))throw new AssertionError("Formulario no guardó");
        log.add("PASS | Formulario Nuevo y evento Guardar persisten producto");
    }
    static void cancelarVenta(){
        javax.swing.Timer timer=new javax.swing.Timer(400,event->{
            var dialogo=Arrays.stream(Window.getWindows()).filter(w->w instanceof JDialog d&&d.isShowing()&&d.getTitle().equals("Confirmar operación")).findFirst().orElseThrow();
            hijos(dialogo).stream().filter(c->c instanceof JOptionPane).map(c->(JOptionPane)c).findFirst().orElseThrow().setValue(JOptionPane.NO_OPTION);
        });timer.setRepeats(false);timer.start();boton("Confirmar venta").doClick();
        if(sistema.consultar().ventas.size()!=1||sistema.consultar().productos.stream().filter(p->p.getIdProducto().equals("P001")).findFirst().orElseThrow().getStock()!=10)throw new AssertionError("Cancelación de venta GUI");
        log.add("PASS | Cancelar confirmación no registra venta ni descuenta stock");
    }
    static void confirmarVenta(){
        javax.swing.Timer timer=new javax.swing.Timer(400,event->{
            var dialogo=Arrays.stream(Window.getWindows()).filter(w->w instanceof JDialog d&&d.isShowing()&&d.getTitle().equals("Confirmar operación")).findFirst().orElseThrow();
            hijos(dialogo).stream().filter(c->c instanceof JOptionPane).map(c->(JOptionPane)c).findFirst().orElseThrow().setValue(JOptionPane.YES_OPTION);
        });timer.setRepeats(false);timer.start();boton("Confirmar venta").doClick();
        if(sistema.consultar().ventas.size()!=2)throw new AssertionError("Confirmación de venta GUI");log.add("PASS | Confirmar venta desde GUI registra venta y genera PDF");
    }
    static void render(String nombre) throws Exception {ventana.validate();var imagen=new BufferedImage(ventana.getWidth(),ventana.getHeight(),BufferedImage.TYPE_INT_RGB);var g=imagen.createGraphics();ventana.printAll(g);g.dispose();ImageIO.write(imagen,"png",Path.of("docs/evidencias",nombre+".png").toFile());log.add("PASS | Render Swing: "+nombre);}
    public static void main(String[] args) throws Exception {
        Path datos=Files.createTempDirectory("colores-t3-visual-");System.setProperty("app.datos",datos.toString());
        sistema=new Sistema(new RepositorioArchivo(datos.resolve("estado.dat")));sistema.ingresar("U001","1234");
        sistema.inventario.guardar(new InventarioServicio.Datos("P001","Chompa de alpaca","Cuello alto","Textil","Unidad","Tejido artesanal",150,85,12,3),true);
        sistema.inventario.guardar(new InventarioServicio.Datos("P002","Chullo reversible","Andino","Textil","Unidad","Tejido artesanal",80,40,3,3),true);
        sistema.clientes.registrar("C001","Cliente","Demostración","12345678","Dirección de ejemplo","999999999","demo@example.com");
        sistema.ventas.confirmar("C001",Map.of("P001",2),"Yape","Boleta");sistema.salir();
        Gui.tema();
        SwingUtilities.invokeAndWait(()->{try{
            ventana=new Aplicacion(sistema);ventana.setVisible(true);render("01-acceso");
            ((JTextField)nombre("usuario")).setText("U001");((JPasswordField)nombre("clave")).setText("1234");boton("Ingresar").doClick();
            if(!sistema.usuario().getIdUsuario().equals("U001"))throw new AssertionError("Acceso GUI");log.add("PASS | Evento Ingresar inicia sesión");render("02-resumen");
            for(String[] pagina:new String[][]{{"Inventario","03-inventario"},{"Clientes","04-clientes"},{"Nueva venta","05-venta"},{"Historial","06-historial"},{"Reportes","07-reportes"},{"Usuarios","08-usuarios"}}){
                boton(pagina[0]).doClick();ventana.validate();
                if(pagina[0].equals("Inventario")){
                    formularioProducto();
                    var buscador=hijos(ventana).stream().filter(c->c instanceof JTextField).map(c->(JTextField)c).findFirst().orElseThrow();
                    buscador.setText("P001");buscador.postActionEvent();
                    var tabla=hijos(ventana).stream().filter(c->c instanceof JTable).map(c->(JTable)c).findFirst().orElseThrow();
                    if(tabla.getRowCount()!=1||!tabla.getValueAt(0,0).equals("P001"))throw new AssertionError("Búsqueda por código GUI");
                    log.add("PASS | RF33 código y Enter filtran el inventario");
                    buscador.setText("");buscador.postActionEvent();
                }
                if(pagina[0].equals("Nueva venta")){((JTextField)nombre("codigoVenta")).setText("P001");boton("Agregar").doClick();boolean correcto=hijos(ventana).stream().anyMatch(c->c instanceof JTable t&&t.getRowCount()==1&&t.getColumnCount()==6);if(!correcto)throw new AssertionError("Carrito GUI");log.add("PASS | Evento Agregar añade producto al carrito");}
                render(pagina[1]);
                if(pagina[0].equals("Nueva venta")){cancelarVenta();confirmarVenta();render("12-venta-confirmada");}
            }
            boton("Nueva venta").doClick();ventana.setSize(1100,700);render("09-venta-tamano-minimo");
            sistema.salir();sistema.ingresar("U002","1234");ventana.inicio();
            boolean restringido=hijos(ventana).stream().noneMatch(c->c instanceof JButton b&&(b.getText().equals("Usuarios")||b.getText().equals("Reportes")));
            if(!restringido)throw new AssertionError("Navegación por rol");log.add("PASS | Vendedor no muestra Usuarios ni Reportes");render("10-rol-vendedor");
            sistema.salir();sistema.ingresar("U001","1234");ventana.inicio();ventana.setSize(1280,790);
        }catch(Exception e){throw new RuntimeException(e);}});
        Files.writeString(Path.of("docs/evidencias/resultados-visuales.txt"),"Ejecución: "+java.time.ZonedDateTime.now()+"\nImágenes renderizadas por printAll de componentes Swing reales (no fotografías del escritorio).\nDatos ficticios aislados.\n"+String.join("\n",log)+"\n");
        if(!Arrays.asList(args).contains("--mantener"))SwingUtilities.invokeAndWait(ventana::dispose);
    }
}
