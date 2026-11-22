package proyectofina;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import proyectofina.servicios.*;
import proyectofina.ui.*;

/**
 * Renderiza las pantallas Swing reales del sistema final con los datos de
 * demostracion (taller de ceramica) y verifica la navegacion por rol.
 * Las imagenes quedan en docs/evidencias/ef/ para el informe.
 */
public final class PruebasVisualesEF {
    static Aplicacion ventana;
    static Sistema sistema;
    static final List<String> log = new ArrayList<>();
    static final Path carpeta = Path.of("docs", "evidencias", "ef");

    /** Todos los componentes dentro de un contenedor. */
    static List<Component> hijos(Container padre) {
        var lista = new ArrayList<Component>();
        for (Component c : padre.getComponents()) {
            lista.add(c);
            if (c instanceof Container p) {
                lista.addAll(hijos(p));
            }
        }
        return lista;
    }

    /** Busca un botón por su texto. */
    static JButton boton(String texto) {
        return hijos(ventana).stream().filter(c -> c instanceof JButton b && b.getText().equals(texto)).map(c -> (JButton) c)
                .findFirst().orElseThrow(() -> new AssertionError("No existe el boton " + texto));
    }

    /** Indica si existe un botón con ese texto. */
    static boolean hayBoton(String texto) {
        return hijos(ventana).stream().anyMatch(c -> c instanceof JButton b && b.getText().equals(texto));
    }

    /** Guarda una captura de la ventana actual. */
    static void render(String nombre) throws Exception {
        ventana.validate();
        var imagen = new BufferedImage(ventana.getWidth(), ventana.getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = imagen.createGraphics();
        ventana.printAll(g);
        g.dispose();
        ImageIO.write(imagen, "png", carpeta.resolve(nombre + ".png").toFile());
        log.add("PASS | Pantalla " + nombre);
    }

    /** Recorre las 12 pantallas y verifica el menú de cada rol. */
    public static void main(String[] args) throws Exception {
        Files.createDirectories(carpeta);
        sistema = new Sistema(new PruebasEF.RepositorioMemoria());
        Gui.tema();
        SwingUtilities.invokeAndWait(() -> {
            try {
                ventana = new Aplicacion(sistema);
                ventana.setSize(1366, 800);
                ventana.setVisible(true);
                render("01-acceso");
                sistema.ingresar("U001", "1234");
                ventana.inicio();
                render("02-resumen");
                String[][] paginas = {{"Inventario", "03-inventario"}, {"Materias primas", "04-materias-primas"},
                    {"Producción", "05-produccion"}, {"Pedidos", "06-pedidos"}, {"Clientes", "07-clientes"},
                    {"Nueva venta", "08-venta"}, {"Historial", "09-historial"}, {"Reportes", "10-reportes"},
                    {"Usuarios", "11-usuarios"}, {"Auditoría y cierre", "12-auditoria"}};
                for (String[] p : paginas) {
                    boton(p[0]).doClick();
                    if (p[0].equals("Nueva venta")) {
                        var codigo = hijos(ventana).stream().filter(c -> "codigoVenta".equals(c.getName()))
                                .map(c -> (JTextField) c).findFirst().orElseThrow();
                        codigo.setText("P001");
                        boton("Agregar").doClick();
                        codigo.setText("P006");
                        boton("Agregar").doClick();
                    }
                    if (p[0].equals("Reportes")) {
                        hijos(ventana).stream().filter(c -> c instanceof JComboBox<?>).map(c -> (JComboBox<?>) c).findFirst().orElseThrow()
                                .setSelectedItem("PRODUCCION");
                    }
                    render(p[1]);
                }
                sistema.salir();
                sistema.ingresar("U002", "1234");
                ventana.inicio();
                if (hayBoton("Producción") || hayBoton("Usuarios") || hayBoton("Auditoría y cierre") || !hayBoton("Pedidos")) {
                    throw new AssertionError("Menu del vendedor");
                }
                log.add("PASS | Vendedor: ve Pedidos y Ventas; no ve Producción, Usuarios ni Auditoría");
                render("13-rol-vendedor");
                sistema.salir();
                sistema.ingresar("U003", "1234");
                ventana.inicio();
                if (!hayBoton("Producción") || !hayBoton("Materias primas") || hayBoton("Nueva venta") || hayBoton("Usuarios")) {
                    throw new AssertionError("Menu del supervisor");
                }
                log.add("PASS | Supervisor de almacén: ve Producción y Materias primas; no ve Nueva venta ni Usuarios");
                boton("Producción").doClick();
                render("14-rol-supervisor-produccion");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Files.writeString(carpeta.resolve("resultados-visuales-ef.txt"), "Ejecucion: " + java.time.ZonedDateTime.now()
                + "\nImagenes de componentes Swing reales (printAll) con los datos de demostracion del taller de ceramica.\n"
                + String.join("\n", log) + "\n");
        System.out.println(String.join("\n", log));
        SwingUtilities.invokeAndWait(ventana::dispose);
        System.exit(0);
    }
}
