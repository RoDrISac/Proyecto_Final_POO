package proyectofina;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Receta (lista de materiales) de un producto: cuanta materia prima se usa
 * para fabricar UNA unidad. La receta se compone de sus insumos (composicion).
 */
public class Receta implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String idProducto;
    private final List<Insumo> insumos = new ArrayList<>();

    /** Receta vacía de un producto. */
    public Receta(String idProducto) {
        this.idProducto = idProducto;
    }

    /** Linea de la receta: materia prima y cantidad por unidad de producto. */
    public record Insumo(String idMateria, double cantidadPorUnidad) implements java.io.Serializable {
        public Insumo {
            if (idMateria == null || idMateria.isBlank()) {
                throw new IllegalArgumentException("Elige una materia prima.");
            }
            if (!Double.isFinite(cantidadPorUnidad) || cantidadPorUnidad <= 0) {
                throw new IllegalArgumentException("La cantidad por unidad debe ser mayor a cero.");
            }
        }
    }

    /** Agrega o reemplaza la cantidad de una materia prima. */
    public void agregarInsumo(String idMateria, double cantidadPorUnidad) {
        quitarInsumo(idMateria);
        insumos.add(new Insumo(idMateria, cantidadPorUnidad));
    }

    /** Quita una materia prima de la receta. */
    public void quitarInsumo(String idMateria) {
        insumos.removeIf(i -> i.idMateria().equals(idMateria));
    }

    /** Materia prima total que se necesita para fabricar cierta cantidad. */
    public Map<String, Double> calcularNecesidades(int cantidad) {
        Map<String, Double> necesidades = new LinkedHashMap<>();
        for (Insumo i : insumos) {
            necesidades.put(i.idMateria(), Math.round(i.cantidadPorUnidad() * cantidad * 1000.0) / 1000.0);
        }
        return necesidades;
    }

    /** Indica si la receta no tiene materias primas. */
    public boolean estaVacia() {
        return insumos.isEmpty();
    }

    // ---------- Getters y setters ----------
    public String getIdProducto() {
        return idProducto;
    }

    public List<Insumo> getInsumos() {
        return List.copyOf(insumos);
    }
}
