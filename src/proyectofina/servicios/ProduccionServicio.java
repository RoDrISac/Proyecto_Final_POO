package proyectofina.servicios;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import proyectofina.*;
import proyectofina.persistencia.Estado;

/**
 * Area de produccion del taller (S08 N.° 2, 3, 4, 5 y 11): materias primas,
 * recetas, registro de produccion con revision de calidad y fallas.
 */
public final class ProduccionServicio {
    private final Sistema s;

    ProduccionServicio(Sistema s) {
        this.s = s;
    }

    public record DatosMateria(String codigo, String nombre, String unidad, double stock, double minimo, double costo) {
    }

    /** Registrar (nuevo = true) o editar una materia prima. El stock solo cambia con ingresos y consumos. */
    public void guardarMateria(DatosMateria d, boolean nuevo) throws IOException {
        String id = Sistema.codigo(d.codigo());
        Sistema.texto(d.nombre(), "Nombre");
        Sistema.dinero(d.costo());
        if (d.stock() < 0 || d.minimo() < 0 || d.stock() > 1_000_000 || d.minimo() > 1_000_000) {
            throw new IllegalArgumentException("Stock y mínimo: de 0 a 1 000 000.");
        }
        s.cambiar("INVENTARIO", nuevo ? "REGISTRAR_MATERIA" : "EDITAR_MATERIA", id + " " + d.nombre(), e -> {
            MateriaPrima actual = e.materias.stream().filter(m -> m.getIdMateria().equalsIgnoreCase(id)).findFirst().orElse(null);
            if (nuevo && actual != null) {
                throw new IllegalArgumentException("Ya existe una materia prima con el código " + id + ".");
            }
            if (!nuevo && actual == null) {
                throw new IllegalArgumentException("La materia prima ya no existe.");
            }
            if (nuevo) {
                MateriaPrima m = new MateriaPrima(id, d.nombre(), d.unidad(), d.stock(), d.minimo(), d.costo());
                e.materias.add(m);
                e.movimientosMateria.add(new Estado.MovimientoMateria(new Date(), s.usuario().getIdUsuario(), id,
                        "INICIAL", d.stock(), d.stock(), ""));
            } else {
                actual.setNombre(d.nombre());
                actual.setUnidad(d.unidad());
                actual.setStockMinimo(d.minimo());
                actual.setCostoUnitario(d.costo());
            }
        });
    }

    /** Compra o ingreso de materia prima; recalcula el costo promedio. */
    public void ingresarMateria(String codigo, double cantidad, double costoUnitario) throws IOException {
        if (cantidad <= 0 || cantidad > 1_000_000) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        Sistema.dinero(costoUnitario);
        s.cambiar("INVENTARIO", "INGRESO_MATERIA", codigo + " +" + cantidad + " a S/ " + costoUnitario, e -> {
            Sistema.validarFechaOperacion(e, new Date());
            MateriaPrima m = buscarMateria(e, codigo);
            m.ingresar(cantidad, costoUnitario);
            e.movimientosMateria.add(new Estado.MovimientoMateria(new Date(), s.usuario().getIdUsuario(),
                    m.getIdMateria(), "INGRESO", cantidad, m.getStock(), "Compra"));
        });
    }

    /** Elimina una materia prima que no se use en ninguna receta. */
    public void eliminarMateria(String codigo) throws IOException {
        s.cambiar("INVENTARIO", "ELIMINAR_MATERIA", codigo, e -> {
            MateriaPrima m = buscarMateria(e, codigo);
            boolean enUso = e.recetas.stream().anyMatch(r -> r.getInsumos().stream().anyMatch(i -> i.idMateria().equals(m.getIdMateria())));
            if (enUso) {
                throw new IllegalArgumentException(m.getNombre() + " se usa en una receta. Quítala de la receta antes de eliminarla.");
            }
            e.materias.remove(m);
        });
    }

    /** Define la receta de un producto: materia prima por cada unidad fabricada. */
    public void guardarReceta(String idProducto, Map<String, Double> insumos) throws IOException {
        if (insumos.isEmpty()) {
            throw new IllegalArgumentException("La receta necesita al menos una materia prima.");
        }
        s.cambiar("INVENTARIO", "GUARDAR_RECETA", idProducto + " " + insumos, e -> {
            Inventario p = InventarioServicio.buscar(e, idProducto);
            Receta r = new Receta(p.getIdProducto());
            for (var i : insumos.entrySet()) {
                buscarMateria(e, i.getKey());
                r.agregarInsumo(i.getKey(), i.getValue());
            }
            e.recetas.removeIf(x -> x.getIdProducto().equals(p.getIdProducto()));
            e.recetas.add(r);
        });
    }

    /**
     * Registro de produccion: descuenta la materia prima de la receta, hace la
     * revision de calidad y suma al inventario solo las piezas aprobadas.
     */
    public Produccion producir(String idProducto, int cantidad, int aprobadas, String observacion, double manoObra) throws IOException {
        if (cantidad <= 0 || cantidad > 100_000) {
            throw new IllegalArgumentException("La cantidad producida debe ser de 1 a 100 000.");
        }
        if (aprobadas < cantidad && (observacion == null || observacion.isBlank())) {
            throw new IllegalArgumentException("Indica por qué se rechazaron piezas en la revisión de calidad.");
        }
        Sistema.dinero(manoObra);
        String usuario = s.usuario().getIdUsuario();
        String[] id = {null};
        s.cambiar("INVENTARIO", "REGISTRAR_PRODUCCION", idProducto + " x" + cantidad + " (aprobadas " + aprobadas + ")", e -> {
            Date ahora = new Date();
            Sistema.validarFechaOperacion(e, ahora);
            Inventario p = InventarioServicio.buscar(e, idProducto);
            Produccion pr = new Produccion(siguienteId(e, "PR"), ahora, usuario, p.getIdProducto(), p.getNombre(),
                    cantidad, aprobadas, observacion, manoObra);
            consumir(e, pr, receta(e, p), cantidad, usuario);
            if ((long) p.getStock() + aprobadas > 1_000_000) {
                throw new IllegalArgumentException("El stock excedería 1 000 000.");
            }
            p.actualizarStock(p.getStock() + aprobadas);
            if (aprobadas > 0) {
                p.setCosto(pr.calcularCostoUnitario());
                p.setFechaIngreso(ahora);
            }
            e.operaciones.add(pr);
            e.movimientos.add(new Estado.Movimiento(ahora, usuario, p.getIdProducto(), "PRODUCCION", aprobadas, p.getStock()));
            id[0] = pr.getIdOperacion();
        });
        return (Produccion) s.consultar().operaciones.stream().filter(o -> o.getIdOperacion().equals(id[0])).findFirst().orElseThrow();
    }

    /** Falla de produccion: se pierden las piezas y se descuenta la materia prima usada. */
    public FallaProduccion registrarFalla(String idProducto, int piezas, String motivo) throws IOException {
        if (piezas <= 0 || piezas > 100_000) {
            throw new IllegalArgumentException("Las piezas falladas deben ser de 1 a 100 000.");
        }
        String usuario = s.usuario().getIdUsuario();
        String[] id = {null};
        s.cambiar("INVENTARIO", "REGISTRAR_FALLA", idProducto + " x" + piezas + ": " + motivo, e -> {
            Date ahora = new Date();
            Sistema.validarFechaOperacion(e, ahora);
            Inventario p = InventarioServicio.buscar(e, idProducto);
            FallaProduccion f = new FallaProduccion(siguienteId(e, "FA"), ahora, usuario, p.getNombre() + " x" + piezas + ": " + motivo);
            consumir(e, f, receta(e, p), piezas, usuario);
            e.operaciones.add(f);
            id[0] = f.getIdOperacion();
        });
        return (FallaProduccion) s.consultar().operaciones.stream().filter(o -> o.getIdOperacion().equals(id[0])).findFirst().orElseThrow();
    }

    /** Cuanta materia prima se necesita y si alcanza (para mostrar antes de producir). */
    public Map<String, String> verificarMateriales(String idProducto, int cantidad) {
        Estado e = s.consultar();
        Inventario p = InventarioServicio.buscar(e, idProducto);
        Map<String, String> r = new LinkedHashMap<>();
        for (var n : receta(e, p).calcularNecesidades(cantidad).entrySet()) {
            MateriaPrima m = buscarMateria(e, n.getKey());
            r.put(m.getNombre(), MateriaPrima.formato(n.getValue()) + " " + m.getUnidad() + " de " + MateriaPrima.formato(m.getStock())
                    + (n.getValue() > m.getStock() ? "  <-- INSUFICIENTE" : "  OK"));
        }
        return r;
    }

    /** Descuenta la materia prima de la receta y la registra en la operación y en el kardex. */
    private static void consumir(Estado e, OperacionProduccion op, Receta receta, int piezas, String usuario) {
        for (var n : receta.calcularNecesidades(piezas).entrySet()) {
            MateriaPrima m = buscarMateria(e, n.getKey());
            op.agregarConsumo(m, n.getValue());
            m.descontar(n.getValue());
            e.movimientosMateria.add(new Estado.MovimientoMateria(op.getFecha(), usuario, m.getIdMateria(),
                    op.getTipo().equals("FALLA") ? "FALLA" : "CONSUMO", -n.getValue(), m.getStock(), op.getIdOperacion()));
        }
    }

    /** Receta del producto o error si no tiene. */
    static Receta receta(Estado e, Inventario p) {
        return e.recetas.stream().filter(r -> r.getIdProducto().equals(p.getIdProducto()) && !r.estaVacia()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(p.getNombre() + " no tiene receta. Defínela en Producción > Receta."));
    }

    /** Busca una materia prima por código o informa que no existe. */
    static MateriaPrima buscarMateria(Estado e, String id) {
        return e.materias.stream().filter(m -> m.getIdMateria().equalsIgnoreCase(id.trim())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Materia prima no encontrada: " + id));
    }

    /** Siguiente número para PR (producción) o FA (falla). */
    private static String siguienteId(Estado e, String prefijo) {
        int n = e.operaciones.stream().filter(o -> o.getIdOperacion().startsWith(prefijo))
                .mapToInt(o -> Integer.parseInt(o.getIdOperacion().substring(2))).max().orElse(0) + 1;
        return String.format("%s%04d", prefijo, n);
    }
}
