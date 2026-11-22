package proyectofina;

/**
 * Materia prima del taller de ceramica (arcilla, esmalte, pigmento, etc.).
 * Requerimiento S08 N.° 2: registrar nombre, stock y costo de cada materia prima.
 */
public class MateriaPrima implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static final String[] UNIDADES = {"kg", "g", "litro", "ml", "unidad", "metro"};

    private final String idMateria;
    private String nombre;
    private String unidad;
    private double stock;
    private double stockMinimo;
    private double costoUnitario;

    /** Crea una materia prima validando nombre, unidad, mínimo, costo y stock. */
    public MateriaPrima(String idMateria, String nombre, String unidad, double stock, double stockMinimo, double costoUnitario) {
        if (idMateria == null || idMateria.isBlank()) {
            throw new IllegalArgumentException("El código de la materia prima es obligatorio.");
        }
        this.idMateria = idMateria;
        setNombre(nombre);
        setUnidad(unidad);
        setStockMinimo(stockMinimo);
        setCostoUnitario(costoUnitario);
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        this.stock = stock;
    }

    /** Compra o ingreso: suma stock y recalcula el costo promedio ponderado. */
    public void ingresar(double cantidad, double costo) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad que ingresa debe ser mayor a cero.");
        }
        if (costo < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo.");
        }
        double valorActual = stock * costoUnitario;
        stock = redondear(stock + cantidad);
        costoUnitario = Math.round((valorActual + cantidad * costo) / stock * 10000.0) / 10000.0;
    }

    /** Consumo por produccion o por falla: nunca deja el stock negativo. */
    public void descontar(double cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero.");
        }
        if (cantidad > stock + 1e-9) {
            throw new IllegalArgumentException("Materia prima insuficiente: " + nombre + ". Disponible: "
                    + formato(stock) + " " + unidad + ", se necesitan " + formato(cantidad) + ".");
        }
        stock = redondear(stock - cantidad);
    }

    /** Alerta cuando el stock llega al mínimo. */
    public boolean tieneStockBajo() {
        return stock <= stockMinimo;
    }

    /** Texto resumido para listados. */
    public String mostrarInfo() {
        return idMateria + " - " + nombre + " | " + formato(stock) + " " + unidad
                + (tieneStockBajo() ? "  <-- STOCK BAJO" : "");
    }

    /** Muestra un número sin decimales innecesarios (2 en vez de 2.000). */
    public static String formato(double n) {
        return n == Math.rint(n) ? String.valueOf((long) n) : String.format(java.util.Locale.US, "%.3f", n).replaceAll("0+$", "");
    }

    /** Redondea a 3 decimales para evitar errores de punto flotante. */
    private static double redondear(double n) {
        return Math.round(n * 1000.0) / 1000.0;
    }

    // ---------- Getters y setters ----------
    public String getIdMateria() {
        return idMateria;
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la materia prima es obligatorio.");
        }
        this.nombre = nombre.trim();
    }

    public String getUnidad() {
        return unidad;
    }

    public final void setUnidad(String unidad) {
        if (unidad == null || unidad.isBlank()) {
            throw new IllegalArgumentException("La unidad de medida es obligatoria.");
        }
        this.unidad = unidad.trim();
    }

    public double getStock() {
        return stock;
    }

    public double getStockMinimo() {
        return stockMinimo;
    }

    public final void setStockMinimo(double stockMinimo) {
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
        this.stockMinimo = stockMinimo;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public final void setCostoUnitario(double costoUnitario) {
        if (!Double.isFinite(costoUnitario) || costoUnitario < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo.");
        }
        this.costoUnitario = costoUnitario;
    }
}
