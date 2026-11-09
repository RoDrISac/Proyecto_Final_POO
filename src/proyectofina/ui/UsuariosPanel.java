package proyectofina.ui;
import javax.swing.*;
import java.awt.*;
import proyectofina.servicios.Sistema;
public final class UsuariosPanel extends JPanel {
    private final Sistema s;
    private final JTable tabla=Gui.tabla("Código","Nombre","DNI","Rol");
    public UsuariosPanel(Sistema s) {
        super(new BorderLayout());
        this.s=s;
        var p=Gui.pagina("Usuarios y roles","Cada persona accede a las funciones que necesita para su trabajo.");
        var c=new JPanel(new BorderLayout(8,12));
        c.add(Gui.fila(Gui.boton("Nuevo usuario",this::nuevo),Gui.boton("Cambiar rol",this::rol)),BorderLayout.NORTH);
        c.add(new JScrollPane(tabla));
        p.add(c);
        add(p);
        actualizar();
    }
    private String[] roles() {
        return s.consultar().roles.stream().map(r->r.getNombreRol()).toArray(String[]::new);
    }
    public void actualizar() {
        Gui.datos(tabla,s.consultar().usuarios.stream().map(u->new Object[] {
            u.getIdUsuario(),u.getNombreCompleto(),u.getDni(),u.getRol().getNombreRol()
        }
        ).toList());
    }
    private void nuevo() {
        var f=new Gui.Formulario().campo("Código","").campo("Nombre","").campo("Apellido","").campo("DNI","").clave("Contraseña").opciones("Rol",roles(),"Vendedor");
        f.mostrar(this,"Registrar usuario",()-> {
            s.usuarios.registrar(f.valor("Código"),f.valor("Nombre"),f.valor("Apellido"),f.valor("DNI"),f.valor("Contraseña"),f.valor("Rol")); actualizar();
        }
        );
    }
    private void rol() {
        String id=Gui.elegido(tabla);
        var f=new Gui.Formulario().opciones("Rol",roles(),tabla.getValueAt(tabla.getSelectedRow(),3).toString());
        f.mostrar(this,"Cambiar rol · "+id,()-> {
            s.usuarios.cambiarRol(id,f.valor("Rol")); actualizar();
        }
        );
    }
}
