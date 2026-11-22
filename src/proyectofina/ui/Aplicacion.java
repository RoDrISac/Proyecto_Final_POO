package proyectofina.ui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.function.Supplier;
import proyectofina.servicios.*;

/** Ventana principal y navegación; cada pantalla administra su propia presentación. */
public final class Aplicacion extends JFrame {
    private static final String GUARDADO = "Los cambios confirmados se guardan automáticamente en la base de datos.";
    private final Sistema sistema;
    private JPanel principal;
    private final JLabel estado = new JLabel(" ");
    private VentasPanel ventas;
    private final LinkedHashMap<String, Gui.Boton> navegacion = new LinkedHashMap<>();

    /** Crea la ventana y muestra la pantalla de acceso. */
    public Aplicacion(Sistema sistema) {
        super("Colores y Creaciones - Sistema de ventas, producción e inventario");
        this.sistema = sistema;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1280, 790);
        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cerrar();
            }
        });
        login();
    }

    /** Confirma el cierre, actualiza los reportes automáticos y cierra la ventana. */
    private void cerrar() {
        if (Gui.confirmar(this, "¿Cerrar la aplicación? Las operaciones confirmadas ya están guardadas; "
                + "el carrito pendiente se descartará.")) {
            try {
                new ReportesServicio(sistema).automaticos();
            } catch (Exception ignored) {
                // Si el reporte automático falla, igual se cierra: las operaciones ya están guardadas
            }
            dispose();
        }
    }

    /** Pantalla de acceso: marca a la izquierda y formulario a la derecha. */
    public void login() {
        ventas = null;
        var raiz = new JPanel(new GridLayout(1, 2));
        raiz.setBackground(Gui.FONDO);

        // Marca: el plato vidriado en cobalto es el único elemento decorativo
        var marca = new JPanel(new GridBagLayout());
        marca.setBackground(Gui.FONDO);
        var gm = new GridBagConstraints();
        gm.gridx = 0;
        gm.gridy = 0;
        gm.insets = new Insets(0, 0, 28, 0);
        marca.add(new Gui.Plato(300, false), gm);
        var nombre = new JLabel("Colores y Creaciones");
        nombre.setFont(Gui.FUENTE.deriveFont(Font.BOLD, 26f));
        nombre.setForeground(Gui.TINTA);
        gm.gridy = 1;
        gm.insets = new Insets(0, 0, 6, 0);
        marca.add(nombre, gm);
        var lema = new JLabel("Cerámica artesanal de Cajamarca");
        lema.setForeground(Gui.TEXTO_SUAVE);
        gm.gridy = 2;
        marca.add(lema, gm);
        raiz.add(marca);

        var lado = new JPanel(new GridBagLayout());
        lado.setBackground(Gui.FONDO);
        var form = Gui.tarjeta(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(36, 40, 32, 40));
        var gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        var titulo = new JLabel("Iniciar sesión");
        titulo.setFont(Gui.FUENTE.deriveFont(Font.BOLD, 24f));
        titulo.setForeground(Gui.TINTA);
        var sub = new JLabel("Usa tu código de usuario y tu contraseña.");
        sub.setForeground(Gui.TEXTO_SUAVE);
        var usuario = new JTextField();
        usuario.setName("usuario");
        var clave = new JPasswordField();
        clave.setName("clave");
        usuario.setPreferredSize(new Dimension(320, 40));
        clave.setPreferredSize(new Dimension(320, 40));
        var lu = new JLabel("Código de usuario");
        lu.setLabelFor(usuario);
        var lc = new JLabel("Contraseña");
        lc.setLabelFor(clave);
        for (var l : new JLabel[]{lu, lc}) {
            l.setFont(Gui.FUENTE.deriveFont(Font.BOLD, 13f));
            l.setForeground(Gui.TINTA);
        }
        var mensaje = new JLabel(" ");
        mensaje.setForeground(Gui.PELIGRO);
        var entrar = Gui.boton("Ingresar", Gui.Estilo.PRIMARIO, () -> {
            try {
                sistema.ingresar(usuario.getText(), new String(clave.getPassword()));
                inicio();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                mensaje.setText("<html><div style='width:300px'>" + ex.getMessage() + "</div></html>");
                clave.setText("");
            }
        });
        entrar.setPreferredSize(new Dimension(320, 42));
        int y = 0;
        gc.gridy = y++;
        gc.insets = new Insets(0, 0, 4, 0);
        form.add(titulo, gc);
        gc.gridy = y++;
        gc.insets = new Insets(0, 0, 26, 0);
        form.add(sub, gc);
        gc.gridy = y++;
        gc.insets = new Insets(0, 0, 6, 0);
        form.add(lu, gc);
        gc.gridy = y++;
        gc.insets = new Insets(0, 0, 16, 0);
        form.add(usuario, gc);
        gc.gridy = y++;
        gc.insets = new Insets(0, 0, 6, 0);
        form.add(lc, gc);
        gc.gridy = y++;
        gc.insets = new Insets(0, 0, 24, 0);
        form.add(clave, gc);
        gc.gridy = y++;
        gc.insets = new Insets(0, 0, 8, 0);
        form.add(entrar, gc);
        gc.gridy = y;
        form.add(mensaje, gc);
        lado.add(form);
        raiz.add(lado);
        setContentPane(raiz);
        getRootPane().setDefaultButton(entrar);
        revalidate();
        repaint();
        SwingUtilities.invokeLater(usuario::requestFocusInWindow);
    }

    /** Ventana principal: menú lateral según el rol, área de trabajo y barra de estado. */
    public void inicio() {
        var raiz = new JPanel(new BorderLayout());
        var lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(Gui.MENU);
        lateral.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Gui.BORDE),
                BorderFactory.createEmptyBorder(18, 12, 12, 12)));
        lateral.setPreferredSize(new Dimension(232, 0));

        var marca = new JPanel(new BorderLayout(10, 0));
        marca.setOpaque(false);
        marca.add(new Gui.Plato(38, true), BorderLayout.WEST);
        var textos = new JPanel(new GridLayout(2, 1, 0, 0));
        textos.setOpaque(false);
        var nombre = new JLabel("Colores y Creaciones");
        nombre.setFont(Gui.FUENTE.deriveFont(Font.BOLD, 13.5f));
        nombre.setForeground(Gui.TINTA);
        var taller = new JLabel("Taller de cerámica");
        taller.setFont(Gui.FUENTE.deriveFont(12.5f));
        taller.setForeground(Gui.TEXTO_SUAVE);
        textos.add(nombre);
        textos.add(taller);
        marca.add(textos);
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        marca.setMaximumSize(new Dimension(208, 42));
        marca.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        lateral.add(marca);
        lateral.add(Box.createVerticalStrut(12));

        principal = new JPanel(new BorderLayout());
        principal.setBackground(Gui.FONDO);
        navegacion.clear();
        seccion(lateral, "General");
        agregar(lateral, "Resumen", this::resumen);
        agregar(lateral, "Inventario", () -> new InventarioPanel(sistema));
        if (sistema.permite("INVENTARIO")) {
            seccion(lateral, "Taller");
            agregar(lateral, "Materias primas", () -> new MateriasPrimasPanel(sistema));
            agregar(lateral, "Producción", () -> new ProduccionPanel(sistema));
        }
        if (sistema.permite("VENTAS") || sistema.permite("INVENTARIO")) {
            seccion(lateral, "Ventas");
            agregar(lateral, "Pedidos", () -> new PedidosPanel(sistema));
        }
        if (sistema.permite("VENTAS")) {
            agregar(lateral, "Clientes", () -> new ClientesPanel(sistema));
            agregar(lateral, "Nueva venta", () -> {
                if (ventas == null) {
                    ventas = new VentasPanel(sistema);
                } else {
                    ventas.actualizarCatalogos();
                }
                return ventas;
            });
            agregar(lateral, "Historial", () -> new HistorialPanel(sistema));
        }
        if (sistema.permite("REPORTES") || sistema.permite("USUARIOS")) {
            seccion(lateral, "Administración");
        }
        if (sistema.permite("REPORTES")) {
            agregar(lateral, "Reportes", () -> new ReportesPanel(sistema));
        }
        if (sistema.permite("USUARIOS")) {
            agregar(lateral, "Usuarios", () -> new UsuariosPanel(sistema));
            agregar(lateral, "Auditoría y cierre", () -> new AdministracionPanel(sistema));
        }
        lateral.add(Box.createVerticalGlue());

        var separador = new JSeparator();
        separador.setForeground(Gui.BORDE);
        separador.setMaximumSize(new Dimension(208, 1));
        separador.setAlignmentX(Component.LEFT_ALIGNMENT);
        lateral.add(separador);
        var u = sistema.usuario();
        var usuario = new JLabel("<html><b>" + u.getNombreCompleto() + "</b><br>"
                + "<span style='color:#656E85'>" + u.getRol().getNombreRol() + "</span></html>");
        usuario.setForeground(Gui.TINTA);
        usuario.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 0));
        usuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        lateral.add(usuario);
        var salir = Gui.boton("Cerrar sesión", Gui.Estilo.MENU, () -> {
            if (Gui.confirmar(this, "¿Cerrar sesión? Se descartará el carrito pendiente.")) {
                new ReportesServicio(sistema).automaticos();
                sistema.salir();
                login();
            }
        });
        salir.setForeground(Gui.TEXTO_SUAVE);
        salir.setAlignmentX(Component.LEFT_ALIGNMENT);
        salir.setMaximumSize(new Dimension(208, 34));
        salir.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        salir.setHorizontalAlignment(SwingConstants.LEFT);
        lateral.add(salir);

        raiz.add(lateral, BorderLayout.WEST);
        raiz.add(principal);
        estado.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Gui.BORDE),
                BorderFactory.createEmptyBorder(7, 18, 7, 18)));
        estado.setForeground(Gui.TEXTO_SUAVE);
        estado.setOpaque(true);
        estado.setBackground(Gui.SUPERFICIE);
        raiz.add(estado, BorderLayout.SOUTH);
        setContentPane(raiz);
        getRootPane().setDefaultButton(null);
        navegacion.get("Resumen").doClick();
        revalidate();
        repaint();
        try {
            new ReportesServicio(sistema).automaticos();
            estado.setText("Sesión iniciada. " + GUARDADO);
        } catch (Exception e) {
            estado.setText("Sesión iniciada. Reportes automáticos pendientes: " + e.getMessage());
        }
    }

    /** Título de sección del menú lateral. */
    private static void seccion(JPanel lateral, String nombre) {
        var l = new JLabel(nombre);
        l.setFont(Gui.FUENTE.deriveFont(Font.BOLD, 12f));
        l.setForeground(Gui.TEXTO_SUAVE);
        l.setBorder(BorderFactory.createEmptyBorder(10, 10, 3, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        lateral.add(l);
    }

    /** Agrega una opción al menú; al pulsarla reemplaza el contenido del área de trabajo. */
    private void agregar(JPanel lateral, String titulo, Supplier<JPanel> fabrica) {
        var b = (Gui.Boton) Gui.boton(titulo, Gui.Estilo.MENU, () -> {
            principal.removeAll();
            principal.add(fabrica.get());
            navegacion.forEach((n, boton) -> boton.setActivo(n.equals(titulo)));
            principal.revalidate();
            principal.repaint();
            estado.setText(GUARDADO);
        });
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(208, 34));
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        lateral.add(b);
        navegacion.put(titulo, b);
    }

    /** Pantalla de resumen con indicadores y tabla de alertas. */
    private JPanel resumen() {
        var p = Gui.pagina("Resumen", "Existencias, pedidos y alertas para decidir la siguiente operación.");
        var e = sistema.consultar();
        var centro = new JPanel(new BorderLayout(16, 18));
        centro.setOpaque(false);

        // Una sola franja con los indicadores, separados por líneas finas
        var franja = Gui.tarjeta(new GridLayout(1, 5));
        franja.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 0));
        long stockBajo = e.productos.stream().filter(x -> x.tieneStockBajo()).count();
        long mpBaja = e.materias.stream().filter(x -> x.tieneStockBajo()).count();
        boolean verVentas = sistema.permite("VENTAS") || sistema.permite("REPORTES");
        franja.add(indicador("" + e.productos.size(), "productos en inventario", Gui.TINTA, false));
        franja.add(indicador("" + stockBajo, "productos por reponer", stockBajo > 0 ? Gui.PELIGRO : Gui.TINTA, true));
        franja.add(indicador("" + mpBaja, "insumos por comprar", mpBaja > 0 ? Gui.PELIGRO : Gui.TINTA, true));
        franja.add(indicador("" + e.pedidos.stream().filter(x -> x.estaActivo()).count(), "pedidos activos", Gui.ACENTO, true));
        franja.add(indicador("" + (verVentas ? e.ventas.size() : e.productos.stream().filter(x -> x.estaDisponible()).count()),
                verVentas ? "ventas registradas" : "productos con stock", Gui.TINTA, true));
        centro.add(franja, BorderLayout.NORTH);

        var alertas = Gui.tabla("Código", "Producto o materia prima", "Tipo de alerta", "Stock", "Mínimo");
        var filas = new java.util.ArrayList<Object[]>();
        e.productos.stream().filter(x -> x.tieneStockBajo()).forEach(x -> filas.add(new Object[]{
            x.getIdProducto(), x.getNombre(), "Producto por reponer", x.getStock(), x.getStockMinimo()}));
        e.materias.stream().filter(x -> x.tieneStockBajo()).forEach(x -> filas.add(new Object[]{
            x.getIdMateria(), x.getNombre(), "Materia prima por comprar",
                    proyectofina.MateriaPrima.formato(x.getStock()) + " " + x.getUnidad(),
            proyectofina.MateriaPrima.formato(x.getStockMinimo())}));
        e.productos.stream().filter(x -> e.stockComprometido(x.getIdProducto()) > x.getStock()).forEach(x -> filas.add(new Object[]{
            x.getIdProducto(), x.getNombre(), "Falta producir para pedidos", x.getStock(),
                    "Pedido: " + e.stockComprometido(x.getIdProducto())}));
        Gui.datos(alertas, filas);
        var cuerpo = Gui.tarjeta(new BorderLayout(10, 12));
        var t = new JLabel(filas.isEmpty() ? "Sin alertas: el stock cubre los mínimos y los pedidos." : "Requieren atención");
        t.setFont(Gui.FUENTE.deriveFont(Font.BOLD, 16f));
        t.setForeground(Gui.TINTA);
        cuerpo.add(t, BorderLayout.NORTH);
        cuerpo.add(new JScrollPane(alertas));
        centro.add(cuerpo);
        p.add(centro, BorderLayout.CENTER);
        return p;
    }

    /** Un indicador de la franja del resumen: número y descripción. */
    private static JPanel indicador(String valor, String texto, Color color, boolean separador) {
        var p = new JPanel(new GridLayout(0, 1, 0, 2));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                separador ? BorderFactory.createMatteBorder(0, 1, 0, 0, Gui.BORDE) : BorderFactory.createEmptyBorder(),
                BorderFactory.createEmptyBorder(4, 22, 4, 16)));
        var v = new JLabel(valor);
        v.setFont(Gui.FUENTE.deriveFont(Font.BOLD, 30f));
        v.setForeground(color);
        p.add(v);
        var l = new JLabel(texto);
        l.setForeground(Gui.TEXTO_SUAVE);
        p.add(l);
        return p;
    }
}
