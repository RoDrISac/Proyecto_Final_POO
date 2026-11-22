package proyectofina.ui;

import javax.swing.*;
import java.awt.*;
import proyectofina.servicios.Sistema;

/** Gestión de usuarios y roles; asignar un rol exige la contraseña de ese rol. */
public final class UsuariosPanel extends JPanel {
    private final Sistema s;
    private final JTable tabla = Gui.tabla("Código", "Nombre", "DNI", "Rol");

    /** Arma la barra de acciones y la tabla de usuarios. */
    public UsuariosPanel(Sistema s) {
        super(new BorderLayout());
        this.s = s;
        var p = Gui.pagina("Usuarios y roles", "Cada persona accede a las funciones que necesita para su trabajo.");
        var c = new JPanel(new BorderLayout(8, 12));
        c.add(Gui.fila(Gui.boton("Nuevo usuario", this::nuevo), Gui.boton("Cambiar rol", this::rol)), BorderLayout.NORTH);
        c.add(new JScrollPane(tabla));
        p.add(c);
        add(p);
        actualizar();
    }

    /** Nombres de los roles disponibles. */
    private String[] roles() {
        return s.consultar().roles.stream().map(r -> r.getNombreRol()).toArray(String[]::new);
    }

    /** Recarga la tabla de usuarios. */
    public void actualizar() {
        Gui.datos(tabla, s.consultar().usuarios.stream()
                .map(u -> new Object[]{u.getIdUsuario(), u.getNombreCompleto(), u.getDni(), u.getRol().getNombreRol()})
                .toList());
    }

    /** Formulario para registrar un usuario. */
    private void nuevo() {
        var f = new Gui.Formulario()
                .campo("Código", "")
                .campo("Nombre", "")
                .campo("Apellido", "")
                .campo("DNI", "")
                .clave("Contraseña")
                .opciones("Rol", roles(), "Vendedor")
                .clave("Contraseña del rol");
        f.mostrar(this, "Registrar usuario", () -> {
            s.usuarios.registrar(f.valor("Código"), f.valor("Nombre"), f.valor("Apellido"), f.valor("DNI"),
                    f.valor("Contraseña"), f.valor("Rol"), f.valor("Contraseña del rol"));
            actualizar();
        });
    }

    /** Formulario para cambiar el rol del usuario seleccionado. */
    private void rol() {
        String id = Gui.elegido(tabla);
        String actual = tabla.getValueAt(tabla.getSelectedRow(), 3).toString();
        var f = new Gui.Formulario().opciones("Rol", roles(), actual).clave("Contraseña del rol");
        f.mostrar(this, "Cambiar rol de " + id, () -> {
            s.usuarios.cambiarRol(id, f.valor("Rol"), f.valor("Contraseña del rol"));
            actualizar();
        });
    }
}
