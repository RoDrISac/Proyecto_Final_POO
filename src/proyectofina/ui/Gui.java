package proyectofina.ui;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
/** Componentes reutilizables; solo presentación y conversión de formularios. */
public final class Gui {
    public static final Color TINTA=new Color(27,46,56), ACENTO=new Color(22,105,94), FONDO=new Color(245,247,248);
    private Gui() {
    }
    public static void tema() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        }
        catch(Exception ignored) {
        }
        UIManager.put("defaultFont",new Font("Segoe UI",Font.PLAIN,14));
        UIManager.put("nimbusBase",ACENTO);
        UIManager.put("nimbusSelectionBackground",ACENTO);
        UIManager.put("control",FONDO);
        UIManager.put("text",TINTA);
        var fuente=new javax.swing.plaf.FontUIResource("Segoe UI",Font.PLAIN,14);
        var claves=java.util.Collections.list(UIManager.getDefaults().keys());
        for(var clave:claves)if(clave.toString().toLowerCase(java.util.Locale.ROOT).endsWith("font"))UIManager.put(clave,fuente);
    }
    @FunctionalInterface public interface Accion {
        void ejecutar() throws Exception;
    }
    public static JButton boton(String texto,Accion accion) {
        JButton b=new JButton(texto);
        b.setMargin(new Insets(8,14,8,14));
        b.addActionListener(e->intentar(b,accion));
        return b;
    }
    public static void intentar(Component padre,Accion accion) {
        try {
            accion.ejecutar();
        }
        catch(Exception e) {
            error(padre,e);
        }
    }
    public static void error(Component padre,Exception e) {
        JOptionPane.showMessageDialog(padre,e instanceof IOException ? "No se pudo completar la operación en disco. "+e.getMessage() : e.getMessage(),"Revisa los datos",JOptionPane.WARNING_MESSAGE);
    }
    public static JPanel fila(Component... componentes) {
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        for(var c:componentes) {
            if(c instanceof JLabel) {
                ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(1,0,1,14));
            }
            p.add(c);
        }
        return p;
    }
    public static JPanel pagina(String titulo,String subtitulo) {
        JPanel p=new JPanel(new BorderLayout(12,16));
        p.setBorder(BorderFactory.createEmptyBorder(24,26,20,26));
        JPanel h=new JPanel(new GridLayout(0,1,0,6));
        JLabel t=new JLabel(titulo);
        t.setFont(new Font("Segoe UI",Font.BOLD,27));
        t.setForeground(TINTA);
        h.add(t);
        JLabel sub=new JLabel(subtitulo);
        sub.setForeground(new Color(95,111,121));
        h.add(sub);
        p.add(h,BorderLayout.NORTH);
        return p;
    }
    public static JTable tabla(String... columnas) {
        JTable t=new JTable(new DefaultTableModel(columnas,0) {
            public boolean isCellEditable(int r,int c) {
                return false;
            }
        }
        );
        t.setRowHeight(34);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0,0));
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }
    public static void datos(JTable t,List<Object[]> filas) {
        DefaultTableModel m=(DefaultTableModel)t.getModel();
        m.setRowCount(0);
        for(var f:filas)m.addRow(f);
    }
    public static String elegido(JTable t) {
        int fila=t.getSelectedRow();
        if(fila<0)throw new IllegalArgumentException("Selecciona una fila de la tabla.");
        return t.getValueAt(fila,0).toString();
    }
    public static String soles(double n) {
        return String.format(java.util.Locale.US,"S/ %,.2f",n);
    }
    public static int entero(String v) {
        try {
            return Integer.parseInt(v.trim());
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Ingresa una cantidad entera válida.");
        }
    }
    public static double decimal(String v) {
        try {
            return Double.parseDouble(v.trim().replace(',','.'));
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Ingresa un importe válido, por ejemplo 25.50.");
        }
    }
    public static boolean confirmar(Component c,String mensaje) {
        return JOptionPane.showConfirmDialog(c,mensaje,"Confirmar operación",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;
    }
    public static void abrir(File f) throws IOException {
        if(Desktop.isDesktopSupported()&&Desktop.getDesktop().isSupported(Desktop.Action.OPEN))Desktop.getDesktop().open(f);
        else throw new IOException("Abre manualmente: "+f.getAbsolutePath());
    }
    public static void abrirPdfGuardado(Component padre,File archivo) {
        try {
            abrir(archivo);
        }
        catch(IOException | UnsupportedOperationException | SecurityException e) {
            JOptionPane.showMessageDialog(padre,
                "El PDF se guardó, pero no se pudo abrir automáticamente.\n"
                +"Puedes abrirlo manualmente en:\n"+archivo.getAbsolutePath(),
                "PDF guardado",JOptionPane.WARNING_MESSAGE);
        }
    }
    public static JTextArea texto() {
        var t=new JTextArea();
        t.setEditable(false);
        t.setFont(new Font(Font.MONOSPACED,Font.PLAIN,13));
        t.setMargin(new Insets(12,12,12,12));
        return t;
    }
    /** El diálogo conserva los campos cuando la validación falla. */
    public static final class Formulario {
        private final LinkedHashMap<String,JComponent> campos=new LinkedHashMap<>();
        public Formulario campo(String nombre,String valor) {
            campos.put(nombre,new JTextField(valor,24));
            return this;
        }
        public Formulario clave(String nombre) {
            campos.put(nombre,new JPasswordField(24));
            return this;
        }
        public Formulario opciones(String nombre,String[] valores,String actual) {
            var c=new JComboBox<>(valores);
            c.setSelectedItem(actual);
            campos.put(nombre,c);
            return this;
        }
        public String valor(String nombre) {
            var c=campos.get(nombre);
            return c instanceof JComboBox<?> b ? Objects.toString(b.getSelectedItem(),""): c instanceof JPasswordField p ? new String(p.getPassword()):((JTextField)c).getText();
        }
        public void mostrar(Component padre,String titulo,Accion guardar) {
            JDialog d=new JDialog(SwingUtilities.getWindowAncestor(padre),titulo,Dialog.ModalityType.APPLICATION_MODAL);
            JPanel form=new JPanel(new GridLayout(0,2,14,8));
            form.setBorder(BorderFactory.createEmptyBorder(18,22,18,22));
            campos.forEach((n,c)-> {
                JLabel l=new JLabel(n); l.setLabelFor(c); form.add(l); form.add(c);
            }
            );
            JButton ok=boton("Guardar",()-> {
                guardar.ejecutar(); d.dispose();
            }
            );
            d.add(form);
            d.add(fila(boton("Cancelar",d::dispose),ok),BorderLayout.SOUTH);
            d.getRootPane().setDefaultButton(ok);
            d.getRootPane().registerKeyboardAction(e->d.dispose(),KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);
            d.pack();
            d.setLocationRelativeTo(padre);
            d.setVisible(true);
        }
    }
}
