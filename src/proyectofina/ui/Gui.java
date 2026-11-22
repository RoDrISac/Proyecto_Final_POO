package proyectofina.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Tema visual y componentes reutilizables de la interfaz. Estilo plano inspirado
 * en la cerámica: terracota como color principal sobre fondos claros.
 * Solo presentación y conversión de formularios; no contiene reglas de negocio.
 */
public final class Gui {
    // Paleta "porcelana y cobalto": cerámica vidriada en blanco y azul
    public static final Color TINTA = new Color(0x1E, 0x24, 0x33); // texto
    public static final Color ACENTO = new Color(0x23, 0x40, 0x9A); // cobalto
    public static final Color ACENTO_OSCURO = new Color(0x1A, 0x31, 0x78);
    public static final Color ACENTO_SUAVE = new Color(0xE9, 0xEE, 0xFA);
    public static final Color FONDO = new Color(0xF6, 0xF8, 0xFC); // porcelana
    public static final Color SUPERFICIE = Color.WHITE;
    public static final Color BORDE = new Color(0xDD, 0xE3, 0xEE);
    public static final Color TEXTO_SUAVE = new Color(0x65, 0x6E, 0x85);
    public static final Color MENU = Color.WHITE;
    public static final Color MENU_ACTIVO = ACENTO_SUAVE;
    public static final Color ARCILLA = new Color(0xB5, 0x65, 0x3A);
    public static final Color EXITO = new Color(0x1F, 0x7A, 0x4D);
    public static final Color PELIGRO = new Color(0xB3, 0x26, 0x1E);
    public static final Font FUENTE = new Font("Segoe UI", Font.PLAIN, 14);

    /** Clase utilitaria: no se instancia. */
    private Gui() {
    }

    /** Aplica el tema visual a toda la aplicación (colores, fuentes, bordes y barras de desplazamiento). */
    public static void tema() {
        try {
            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
            javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new javax.swing.plaf.metal.DefaultMetalTheme());
            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        } catch (Exception ignored) {
            // Metal viene incluido en Java; si fallara, se mantiene el aspecto por defecto
        }
        var fuente = new javax.swing.plaf.FontUIResource(FUENTE);
        for (var clave : Collections.list(UIManager.getDefaults().keys())) {
            if (clave.toString().toLowerCase(Locale.ROOT).endsWith("font")) {
                UIManager.put(clave, fuente);
            }
        }
        UIManager.put("Panel.background", FONDO);
        UIManager.put("OptionPane.background", SUPERFICIE);
        UIManager.put("OptionPane.messageForeground", TINTA);
        UIManager.put("Label.foreground", TINTA);
        UIManager.put("Viewport.background", SUPERFICIE);
        UIManager.put("ScrollPane.background", SUPERFICIE);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(BORDE));
        UIManager.put("ScrollBarUI", BarraDesplazamiento.class.getName());
        UIManager.put("ScrollBar.width", 12);
        Border campo = BorderFactory.createCompoundBorder(new BordeRedondeado(BORDE, 8), BorderFactory.createEmptyBorder(6, 10, 6, 10));
        for (String c : new String[]{"TextField", "PasswordField", "FormattedTextField"}) {
            UIManager.put(c + ".border", campo);
            UIManager.put(c + ".background", SUPERFICIE);
            UIManager.put(c + ".selectionBackground", ACENTO_SUAVE);
            UIManager.put(c + ".selectionForeground", TINTA);
            UIManager.put(c + ".caretForeground", ACENTO);
        }
        UIManager.put("TextArea.background", SUPERFICIE);
        UIManager.put("TextArea.selectionBackground", ACENTO_SUAVE);
        UIManager.put("ComboBox.background", SUPERFICIE);
        UIManager.put("ComboBox.buttonBackground", SUPERFICIE);
        UIManager.put("ComboBox.selectionBackground", ACENTO_SUAVE);
        UIManager.put("ComboBox.selectionForeground", TINTA);
        UIManager.put("ComboBox.border", new BordeRedondeado(BORDE, 8));
        UIManager.put("Spinner.border", new BordeRedondeado(BORDE, 8));
        UIManager.put("Spinner.background", SUPERFICIE);
        UIManager.put("Table.selectionBackground", ACENTO_SUAVE);
        UIManager.put("Table.selectionForeground", TINTA);
        UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder(0, 12, 0, 12));
        UIManager.put("ToolTip.background", TINTA);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
    }

    @FunctionalInterface
    public interface Accion {
        void ejecutar() throws Exception;
    }

    /** Estilos de botón. */
    public enum Estilo { PRIMARIO, SECUNDARIO, PELIGRO, MENU }

    /** Botón plano y redondeado, dibujado a mano para no depender del look and feel. */
    public static final class Boton extends JButton {
        private final Estilo estilo;
        private boolean activo;

        Boton(String texto, Estilo estilo) {
            super(texto);
            this.estilo = estilo;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(estilo == Estilo.MENU ? FUENTE : FUENTE.deriveFont(Font.BOLD, 13.5f));
            setBorder(estilo == Estilo.MENU ? BorderFactory.createEmptyBorder(9, 14, 9, 14)
                    : BorderFactory.createEmptyBorder(8, 16, 8, 16));
            setForeground(switch (estilo) {
                case PRIMARIO, PELIGRO -> Color.WHITE;
                case MENU -> TINTA;
                default -> TINTA;
            });
            setRolloverEnabled(true);
        }

        /** Resalta en el menú lateral el botón de la pantalla abierta. */
        public void setActivo(boolean activo) {
            this.activo = activo;
            setForeground(activo ? ACENTO : TINTA);
            setFont(FUENTE.deriveFont(activo ? Font.BOLD : Font.PLAIN));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            var g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            var m = getModel();
            boolean encima = m.isRollover() || m.isPressed();
            var forma = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            switch (estilo) {
                case PRIMARIO -> {
                    g2.setColor(m.isPressed() ? ACENTO_OSCURO : encima ? ACENTO.darker() : ACENTO);
                    g2.fill(forma);
                }
                case PELIGRO -> {
                    g2.setColor(encima ? PELIGRO.darker() : PELIGRO);
                    g2.fill(forma);
                }
                case MENU -> {
                    if (activo) {
                        g2.setColor(MENU_ACTIVO);
                        g2.fill(forma);
                        g2.setColor(ACENTO);
                        g2.fillRoundRect(0, 7, 3, getHeight() - 14, 3, 3);
                    } else if (encima) {
                        g2.setColor(new Color(0xF2, 0xF5, 0xFB));
                        g2.fill(forma);
                    }
                }
                default -> {
                    g2.setColor(encima ? new Color(0xF2, 0xF5, 0xFB) : SUPERFICIE);
                    g2.fill(forma);
                    g2.setColor(encima ? ACENTO : BORDE);
                    g2.draw(forma);
                }
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Las acciones principales (crear, guardar, confirmar) se destacan en terracota. */
    private static final List<String> PRIMARIAS = List.of("Nuevo", "Nueva", "Guardar", "Confirmar", "Registrar",
            "Ingresar", "Entregar", "Consultar", "Agregar");

    /** Crea un botón y elige su estilo según la acción (principal, peligro o secundario). */
    public static JButton boton(String texto, Accion accion) {
        Estilo e = PRIMARIAS.stream().anyMatch(texto::startsWith) ? Estilo.PRIMARIO
                : (texto.startsWith("Eliminar") || texto.startsWith("Cancelar pedido")) ? Estilo.PELIGRO : Estilo.SECUNDARIO;
        return boton(texto, e, accion);
    }

    /** Crea un botón con el estilo indicado; si la acción falla, muestra el error. */
    public static JButton boton(String texto, Estilo estilo, Accion accion) {
        var b = new Boton(texto, estilo);
        b.addActionListener(e -> intentar(b, accion));
        return b;
    }

    /** Ejecuta una acción y muestra cualquier error en un diálogo. */
    public static void intentar(Component padre, Accion accion) {
        try {
            accion.ejecutar();
        } catch (Exception e) {
            error(padre, e);
        }
    }

    /** Muestra el mensaje de error al usuario. */
    public static void error(Component padre, Exception e) {
        JOptionPane.showMessageDialog(padre,
                e instanceof IOException ? "No se pudo completar la operación. " + e.getMessage() : e.getMessage(),
                "Revisa los datos", JOptionPane.WARNING_MESSAGE);
    }

    /** Fila horizontal de componentes con el estilo de la interfaz. */
    public static JPanel fila(Component... componentes) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setOpaque(false);
        for (var c : componentes) {
            estilizar(c);
            if (c instanceof JLabel l) {
                l.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 4));
                if (!l.getFont().isBold()) {
                    l.setForeground(TEXTO_SUAVE);
                }
            }
            p.add(c);
        }
        return p;
    }

    /** Página con título, subtítulo y el contenido dentro de una tarjeta blanca. */
    public static JPanel pagina(String titulo, String subtitulo) {
        JPanel p = new JPanel(new BorderLayout(12, 16)) {
            @Override
            public Component add(Component comp) {
                if (comp instanceof JPanel contenido && getComponentCount() == 1) {
                    var tarjeta = tarjeta(new BorderLayout());
                    transparentar(contenido);
                    tarjeta.add(contenido);
                    return super.add(tarjeta);
                }
                return super.add(comp);
            }
        };
        p.setBackground(FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
        JPanel h = new JPanel(new GridLayout(0, 1, 0, 4));
        h.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setFont(FUENTE.deriveFont(Font.BOLD, 24f));
        t.setForeground(TINTA);
        h.add(t);
        JLabel sub = new JLabel(subtitulo);
        sub.setForeground(TEXTO_SUAVE);
        h.add(sub);
        p.add(h, BorderLayout.NORTH);
        return p;
    }

    /** Bordes redondeados para listas desplegables y campos numéricos (el look and feel los dibuja antiguos). */
    static void estilizar(Component c) {
        if (c instanceof JComboBox<?> cb) {
            cb.setBorder(new BordeRedondeado(BORDE, 8));
            cb.setBackground(SUPERFICIE);
            cb.setPreferredSize(new Dimension(Math.max(cb.getPreferredSize().width, 120), 36));
            cb.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                    var r = (JLabel) super.getListCellRendererComponent(l, v, i, sel, foco);
                    r.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                    if (sel) {
                        r.setBackground(ACENTO_SUAVE);
                        r.setForeground(TINTA);
                    }
                    return r;
                }
            });
            for (Component h : cb.getComponents()) {
                if (h instanceof JButton flecha) {
                    flecha.setBorder(BorderFactory.createEmptyBorder());
                    flecha.setBackground(SUPERFICIE);
                }
            }
        } else if (c instanceof JSpinner sp) {
            sp.setBorder(new BordeRedondeado(BORDE, 8));
            sp.setPreferredSize(new Dimension(Math.max(sp.getPreferredSize().width, 90), 36));
            if (sp.getEditor() instanceof JSpinner.DefaultEditor ed) {
                ed.getTextField().setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 6));
            }
            for (Component h : sp.getComponents()) {
                if (h instanceof JButton flecha) {
                    flecha.setBorder(BorderFactory.createEmptyBorder());
                    flecha.setBackground(new Color(0xF3, 0xEF, 0xEB));
                }
            }
        } else if (c instanceof JTextField tf) {
            tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 36));
        }
    }

    /** Quita el fondo de los paneles internos para que se vea la tarjeta blanca. */
    static void transparentar(Container c) {
        if (c instanceof JPanel p) {
            p.setOpaque(false);
        }
        for (Component h : c.getComponents()) {
            if (h instanceof JPanel hp) {
                transparentar(hp);
            }
        }
    }

    /** Panel blanco con borde redondeado. */
    public static JPanel tarjeta(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                var g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SUPERFICIE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        return p;
    }

    /** Tabla de solo lectura con filas alternadas, ordenamiento y estados resaltados. */
    public static JTable tabla(String... columnas) {
        JTable t = new JTable(new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        }) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int fila, int col) {
                Component c = super.prepareRenderer(r, fila, col);
                if (!isRowSelected(fila)) {
                    c.setBackground(fila % 2 == 0 ? SUPERFICIE : new Color(0xF8, 0xFA, 0xFD));
                    c.setForeground(TINTA);
                }
                Object v = getValueAt(fila, col);
                if (v != null && (v.toString().equals("STOCK BAJO") || v.toString().equals("FALTA PRODUCIR")
                        || v.toString().equals("CANCELADO"))) {
                    c.setForeground(PELIGRO);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (v != null && (v.toString().equals("APROBADO") || v.toString().equals("ENTREGADO")
                        || v.toString().equals("Urgente"))) {
                    c.setForeground(v.toString().equals("Urgente") ? ARCILLA : EXITO);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }
                if (c instanceof JComponent jc) {
                    jc.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                }
                return c;
            }
        };
        t.setRowHeight(36);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setBackground(SUPERFICIE);
        var cab = t.getTableHeader();
        cab.setReorderingAllowed(false);
        cab.setPreferredSize(new Dimension(0, 38));
        cab.setDefaultRenderer((tabla, valor, sel, foco, fila, col) -> {
            var l = new JLabel(valor == null ? "" : valor.toString());
            l.setOpaque(true);
            l.setBackground(new Color(0xF4, 0xF6, 0xFB));
            l.setForeground(TEXTO_SUAVE);
            l.setFont(FUENTE.deriveFont(Font.BOLD, 13f));
            l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return l;
        });
        return t;
    }

    /** Reemplaza las filas de una tabla y ajusta el ancho de sus columnas. */
    public static void datos(JTable t, List<Object[]> filas) {
        DefaultTableModel m = (DefaultTableModel) t.getModel();
        m.setRowCount(0);
        for (var f : filas) {
            m.addRow(f);
        }
        ajustarColumnas(t);
    }

    /** Reparte el ancho de las columnas según el texto más largo de cada una. */
    public static void ajustarColumnas(JTable t) {
        var fm = t.getFontMetrics(FUENTE);
        var fmCab = t.getFontMetrics(FUENTE.deriveFont(Font.BOLD, 13f));
        for (int c = 0; c < t.getColumnCount(); c++) {
            int ancho = fmCab.stringWidth(t.getColumnName(c)) + 28;
            for (int f = 0; f < Math.min(t.getRowCount(), 200); f++) {
                Object v = t.getValueAt(f, c);
                ancho = Math.max(ancho, fm.stringWidth(v == null ? "" : v.toString()) + 28);
            }
            t.getColumnModel().getColumn(c).setPreferredWidth(Math.min(ancho, 250));
        }
    }

    /** Código (primera columna) de la fila seleccionada. */
    public static String elegido(JTable t) {
        int fila = t.getSelectedRow();
        if (fila < 0) {
            throw new IllegalArgumentException("Selecciona una fila de la tabla.");
        }
        return t.getValueAt(fila, 0).toString();
    }

    /** Formato de moneda: S/ 1,234.50. */
    public static String soles(double n) {
        return String.format(Locale.US, "S/ %,.2f", n);
    }

    /** Convierte el texto a entero o informa el error. */
    public static int entero(String v) {
        try {
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Ingresa una cantidad entera válida.");
        }
    }

    /** Convierte el texto a número (acepta coma o punto) o informa el error. */
    public static double decimal(String v) {
        try {
            return Double.parseDouble(v.trim().replace(',', '.'));
        } catch (Exception e) {
            throw new IllegalArgumentException("Ingresa un importe válido, por ejemplo 25.50.");
        }
    }

    /** Pregunta Sí o No antes de una operación importante. */
    public static boolean confirmar(Component c, String mensaje) {
        return JOptionPane.showConfirmDialog(c, mensaje, "Confirmar operación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Abre un archivo con el programa predeterminado de Windows. */
    public static void abrir(File f) throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(f);
        } else {
            throw new IOException("Abre manualmente: " + f.getAbsolutePath());
        }
    }

    /** Abre el PDF; si no se puede, indica dónde quedó guardado. */
    public static void abrirPdfGuardado(Component padre, File archivo) {
        try {
            abrir(archivo);
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            JOptionPane.showMessageDialog(padre,
                    "El PDF se guardó, pero no se pudo abrir automáticamente.\nPuedes abrirlo manualmente en:\n"
                    + archivo.getAbsolutePath(),
                    "PDF guardado", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Área de texto de solo lectura con fuente monoespaciada (detalles y reportes). */
    public static JTextArea texto() {
        var t = new JTextArea();
        t.setEditable(false);
        t.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        t.setMargin(new Insets(12, 14, 12, 14));
        t.setBackground(SUPERFICIE);
        t.setForeground(TINTA);
        return t;
    }

    /**
     * Plato vidriado dibujado con Graphics2D: anillos y motivos en cobalto, como la
     * cerámica en blanco y azul. Es el único elemento decorativo de la interfaz.
     */
    public static final class Plato extends JComponent {
        private final boolean pequeno;

        public Plato(int tam, boolean pequeno) {
            this.pequeno = pequeno;
            setPreferredSize(new Dimension(tam, tam));
            setMinimumSize(new Dimension(tam, tam));
            setMaximumSize(new Dimension(tam, tam));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            var g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int t = Math.min(getWidth(), getHeight());
            double cx = getWidth() / 2.0, cy = getHeight() / 2.0, r = t / 2.0 - 2;
            g2.setColor(Color.WHITE);
            g2.fill(new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
            g2.setColor(ACENTO);
            g2.setStroke(new BasicStroke(pequeno ? 1.6f : Math.max(2f, t / 110f)));
            g2.draw(new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
            double[] anillos = pequeno ? new double[]{0.62} : new double[]{0.9, 0.62, 0.3};
            for (double a : anillos) {
                double rr = r * a;
                g2.draw(new java.awt.geom.Ellipse2D.Double(cx - rr, cy - rr, 2 * rr, 2 * rr));
            }
            int petalos = pequeno ? 8 : 16;
            double rm = r * (pequeno ? 0.81 : 0.76), lp = r * (pequeno ? 0.14 : 0.1);
            for (int i = 0; i < petalos; i++) {
                double ang = 2 * Math.PI * i / petalos;
                var at = g2.getTransform();
                g2.translate(cx + rm * Math.cos(ang), cy + rm * Math.sin(ang));
                g2.rotate(ang);
                g2.fill(new java.awt.geom.Ellipse2D.Double(-lp, -lp / 2.6, 2 * lp, 2 * lp / 2.6));
                g2.setTransform(at);
            }
            if (!pequeno) {
                for (int i = 0; i < 24; i++) {
                    double ang = 2 * Math.PI * i / 24 + Math.PI / 24;
                    double rd = r * 0.46, pd = r * 0.025;
                    g2.fill(new java.awt.geom.Ellipse2D.Double(cx + rd * Math.cos(ang) - pd, cy + rd * Math.sin(ang) - pd, 2 * pd, 2 * pd));
                }
                for (int i = 0; i < 6; i++) {
                    double ang = 2 * Math.PI * i / 6;
                    var at = g2.getTransform();
                    g2.translate(cx + r * 0.14 * Math.cos(ang), cy + r * 0.14 * Math.sin(ang));
                    g2.rotate(ang);
                    g2.fill(new java.awt.geom.Ellipse2D.Double(-r * 0.11, -r * 0.045, r * 0.22, r * 0.09));
                    g2.setTransform(at);
                }
            } else {
                double rc = r * 0.2;
                g2.fill(new java.awt.geom.Ellipse2D.Double(cx - rc, cy - rc, 2 * rc, 2 * rc));
            }
            g2.dispose();
        }
    }

    /** Borde redondeado de una línea. */
    public static final class BordeRedondeado extends AbstractBorder {
        private final Color color;
        private final int radio;

        public BordeRedondeado(Color color, int radio) {
            this.color = color;
            this.radio = radio;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            var g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.hasFocus() ? ACENTO : color);
            g2.drawRoundRect(x, y, w - 1, h - 1, radio, radio);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }

    /** Barra de desplazamiento delgada y sin flechas. */
    public static final class BarraDesplazamiento extends BasicScrollBarUI {
        public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
            return new BarraDesplazamiento();
        }

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(0xC9, 0xD2, 0xE3);
            trackColor = SUPERFICIE;
        }

        private static JButton sinBoton() {
            var b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected JButton createDecreaseButton(int o) {
            return sinBoton();
        }

        @Override
        protected JButton createIncreaseButton(int o) {
            return sinBoton();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) {
                return;
            }
            var g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 3, r.y + 3, r.width - 6, r.height - 6, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    /** El diálogo conserva los campos cuando la validación falla. */
    public static final class Formulario {
        private final LinkedHashMap<String, JComponent> campos = new LinkedHashMap<>();

        /** Agrega un campo de texto con su valor inicial. */
        public Formulario campo(String nombre, String valor) {
            campos.put(nombre, new JTextField(valor, 24));
            return this;
        }

        /** Agrega un campo de contraseña (no muestra lo escrito). */
        public Formulario clave(String nombre) {
            campos.put(nombre, new JPasswordField(24));
            return this;
        }

        /** Agrega una lista desplegable con el valor actual seleccionado. */
        public Formulario opciones(String nombre, String[] valores, String actual) {
            var c = new JComboBox<>(valores);
            c.setSelectedItem(actual);
            campos.put(nombre, c);
            return this;
        }

        /** Devuelve lo escrito o elegido en el campo indicado. */
        public String valor(String nombre) {
            var c = campos.get(nombre);
            return c instanceof JComboBox<?> b ? Objects.toString(b.getSelectedItem(), "")
                    : c instanceof JPasswordField p ? new String(p.getPassword()) : ((JTextField) c).getText();
        }

        /** Abre el diálogo modal; Guardar ejecuta la acción y, si la validación falla, el diálogo sigue abierto. */
        public void mostrar(Component padre, String titulo, Accion guardar) {
            JDialog d = new JDialog(SwingUtilities.getWindowAncestor(padre), titulo, Dialog.ModalityType.APPLICATION_MODAL);
            JPanel raiz = new JPanel(new BorderLayout(0, 12));
            raiz.setBackground(SUPERFICIE);
            raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
            JLabel t = new JLabel(titulo);
            t.setFont(FUENTE.deriveFont(Font.BOLD, 18f));
            raiz.add(t, BorderLayout.NORTH);
            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            var gc = new GridBagConstraints();
            gc.insets = new Insets(5, 0, 5, 14);
            gc.anchor = GridBagConstraints.WEST;
            int fila = 0;
            for (var e : campos.entrySet()) {
                JLabel l = new JLabel(e.getKey());
                l.setForeground(TEXTO_SUAVE);
                l.setLabelFor(e.getValue());
                gc.gridx = 0;
                gc.gridy = fila;
                gc.weightx = 0;
                gc.fill = GridBagConstraints.NONE;
                form.add(l, gc);
                gc.gridx = 1;
                gc.weightx = 1;
                gc.fill = GridBagConstraints.HORIZONTAL;
                estilizar(e.getValue());
                e.getValue().setPreferredSize(new Dimension(280, 36));
                form.add(e.getValue(), gc);
                fila++;
            }
            raiz.add(form);
            JButton ok = boton("Guardar", Estilo.PRIMARIO, () -> {
                guardar.ejecutar();
                d.dispose();
            });
            JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            botones.setOpaque(false);
            botones.add(boton("Cancelar", Estilo.SECUNDARIO, d::dispose));
            botones.add(ok);
            raiz.add(botones, BorderLayout.SOUTH);
            d.setContentPane(raiz);
            d.getRootPane().setDefaultButton(ok);
            d.getRootPane().registerKeyboardAction(e -> d.dispose(), KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
            d.pack();
            d.setLocationRelativeTo(padre);
            d.setVisible(true);
        }
    }
}
