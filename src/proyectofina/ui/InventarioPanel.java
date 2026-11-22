package proyectofina.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import proyectofina.*;
import proyectofina.servicios.*;

/** Inventario de productos terminados: búsqueda, registro, edición, ingreso de stock y baja. */
public final class InventarioPanel extends JPanel {
    private final Sistema s;
    private final JTable tabla = Gui.tabla("Código", "Producto", "Modelo", "Precio", "Costo", "Stock",
            "Comprometido", "Disponible", "Mínimo", "Estado");
    private final JTextField buscar = new JTextField(18);
    private final JLabel resumen = new JLabel();

    /** Arma el buscador, las acciones según el rol y la tabla. */
    public InventarioPanel(Sistema s) {
        super(new BorderLayout());
        this.s = s;
        var p = Gui.pagina("Inventario", "Busca por código, modelo o nombre. El lector puede escribir el código y pulsar Enter.");
        var cuerpo = new JPanel(new BorderLayout(8, 12));
        var barra = Gui.fila(new JLabel("Buscar"), buscar, Gui.boton("Buscar", this::actualizar), Gui.boton("Ver todo", () -> {
            buscar.setText("");
            actualizar();
        }));
        var acciones = Gui.fila();
        if (s.permite("INVENTARIO")) {
            acciones.add(Gui.boton("Nuevo", () -> editar(null)));
            acciones.add(Gui.boton("Editar", () -> editar(seleccion())));
            acciones.add(Gui.boton("Ingresar stock", this::ingresar));
            acciones.add(Gui.boton("Eliminar", this::eliminar));
        }
        buscar.addActionListener(e -> actualizar());
        var barras = new JPanel(new GridLayout(0, 1, 0, 4));
        barras.add(barra);
        if (acciones.getComponentCount() > 0) {
            barras.add(acciones);
        }
        cuerpo.add(barras, BorderLayout.NORTH);
        cuerpo.add(new JScrollPane(tabla));
        cuerpo.add(resumen, BorderLayout.SOUTH);
        p.add(cuerpo);
        add(p);
        actualizar();
    }

    /** Producto de la fila seleccionada. */
    private Inventario seleccion() {
        String id = Gui.elegido(tabla);
        return s.consultar().productos.stream().filter(p -> p.getIdProducto().equals(id)).findFirst().orElseThrow();
    }

    /** Recarga la tabla aplicando el texto de búsqueda. */
    public void actualizar() {
        String filtro = buscar.getText().trim().toLowerCase(Locale.ROOT);
        var e = s.consultar();
        var productos = e.productos.stream()
                .filter(p -> (p.getIdProducto() + " " + p.getNombre() + " " + p.getModelo() + " " + p.getCategoria())
                        .toLowerCase(Locale.ROOT).contains(filtro))
                .toList();
        Gui.datos(tabla, productos.stream().map(p -> new Object[]{
            p.getIdProducto(), p.getNombre(), p.getModelo(), Gui.soles(p.getPrecio()), Gui.soles(p.getCosto()), p.getStock(),
            e.stockComprometido(p.getIdProducto()), e.stockDisponible(p), p.getStockMinimo(), estado(e, p)
        }).toList());
        long bajos = productos.stream().filter(Inventario::tieneStockBajo).count();
        resumen.setText(productos.size() + " productos, " + bajos + " con stock bajo.");
        if (tabla.getRowCount() == 1) {
            tabla.setRowSelectionInterval(0, 0);
        }
    }

    /** Estado del producto para la última columna. */
    private static String estado(proyectofina.persistencia.Estado e, Inventario p) {
        if (e.stockComprometido(p.getIdProducto()) > p.getStock()) {
            return "FALTA PRODUCIR";
        }
        return p.tieneStockBajo() ? "STOCK BAJO" : "Disponible";
    }

    /** Pide confirmación y elimina el producto seleccionado. */
    private void eliminar() throws Exception {
        String id = Gui.elegido(tabla);
        if (Gui.confirmar(this, "¿Eliminar " + id + " del inventario? Sus ventas se conservarán.")) {
            s.inventario.eliminar(id);
            actualizar();
        }
    }

    /** Formulario de ingreso de stock. */
    private void ingresar() {
        var p = seleccion();
        var f = new Gui.Formulario().campo("Cantidad", "1");
        f.mostrar(this, "Ingreso de stock: " + p.getNombre(), () -> {
            s.inventario.ingresar(p.getIdProducto(), Gui.entero(f.valor("Cantidad")));
            actualizar();
        });
    }

    /** Formulario para registrar (p = null) o editar un producto. */
    private void editar(Inventario p) {
        boolean nuevo = p == null;
        var f = new Gui.Formulario()
                .campo("Código", nuevo ? "" : p.getIdProducto())
                .campo("Nombre", nuevo ? "" : p.getNombre())
                .campo("Modelo", nuevo ? "" : p.getModelo())
                .campo("Categoría", nuevo ? "Cerámica decorativa" : p.getCategoria())
                .campo("Unidad", nuevo ? "Unidad" : p.getUnidad())
                .campo("Descripción", nuevo ? "" : p.getDescripcion())
                .campo("Precio", nuevo ? "0.00" : String.valueOf(p.getPrecio()))
                .campo("Costo", nuevo ? "0.00" : String.valueOf(p.getCosto()))
                .campo("Stock inicial", nuevo ? "0" : String.valueOf(p.getStock()))
                .campo("Stock mínimo", nuevo ? "3" : String.valueOf(p.getStockMinimo()));
        f.mostrar(this, nuevo ? "Registrar producto" : "Editar producto (el stock cambia con los ingresos)", () -> {
            if (!nuevo && !f.valor("Código").equals(p.getIdProducto())) {
                throw new IllegalArgumentException("El código no se puede cambiar. Conserva " + p.getIdProducto());
            }
            if (!nuevo && Gui.entero(f.valor("Stock inicial")) != p.getStock()) {
                throw new IllegalArgumentException("Usa Ingresar stock para registrar un movimiento.");
            }
            s.inventario.guardar(new InventarioServicio.Datos(f.valor("Código"), f.valor("Nombre"), f.valor("Modelo"),
                    f.valor("Categoría"), f.valor("Unidad"), f.valor("Descripción"), Gui.decimal(f.valor("Precio")),
                    Gui.decimal(f.valor("Costo")), Gui.entero(f.valor("Stock inicial")), Gui.entero(f.valor("Stock mínimo"))), nuevo);
            actualizar();
        });
    }
}
