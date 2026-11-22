package proyectofina.servicios;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import proyectofina.*;
import proyectofina.persistencia.Estado;

/** Reportes manuales y automáticos (S08 N.° 10, 18, 19 y 20; producción, pedidos y clientes). */
public final class ReportesServicio {
    public static final String[] TIPOS = {
        "INVENTARIO", "STOCK_BAJO", "VENTAS", "MAS_VENDIDOS", "VENTAS_DIARIAS", "VENTAS_MENSUALES", "MOVIMIENTOS",
        "PRODUCCION", "MATERIAS_PRIMAS", "PEDIDOS", "CLIENTES_CATEGORIA"
    };
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Sistema s;

    /** Servicio de reportes del sistema. */
    public ReportesServicio(Sistema s) {
        this.s = s;
    }

    /** Texto del reporte elegido; las fechas filtran movimientos y producción. */
    public String contenido(String tipo, LocalDate desde, LocalDate hasta) {
        s.exigir("REPORTES");
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
        }
        Estado e = s.consultar();
        return switch (tipo) {
            case "PRODUCCION" -> produccion(e, desde, hasta);
            case "MATERIAS_PRIMAS" -> materias(e);
            case "PEDIDOS" -> pedidos(e);
            case "CLIENTES_CATEGORIA" -> clientes(e);
            case "MOVIMIENTOS" -> movimientos(e, desde, hasta);
            default -> new Reporte(e.ventas, e.productos).generar(tipo).getContenido();
        };
    }

    /** Entradas, ventas, producciones y bajas del período (S08 N.° 19). */
    private static String movimientos(Estado e, LocalDate desde, LocalDate hasta) {
        var b = new StringBuilder("COLORES Y CREACIONES - MOVIMIENTOS\nPeriodo: " + desde + " a " + hasta
                + "\n\nFecha y hora | Usuario | Producto | Tipo | Cantidad | Saldo\n");
        for (var m : e.movimientos) {
            if (!enPeriodo(m.fecha(), desde, hasta)) {
                continue;
            }
            var fecha = m.fecha().toInstant().atZone(ZoneId.systemDefault());
            b.append(fecha.format(FECHA_HORA)).append(" | ").append(m.usuario()).append(" | ").append(m.producto())
                    .append(" | ").append(m.tipo()).append(" | ").append(m.cantidad()).append(" | ").append(m.saldo()).append('\n');
        }
        return b.toString();
    }

    /** Encabezado común con la fecha de generación. */
    private static String encabezado(String titulo) {
        return "COLORES Y CREACIONES - " + titulo + "\nGenerado: " + LocalDateTime.now().format(FECHA_HORA) + "\n\n";
    }

    /** Indica si la fecha está dentro del rango (ambos extremos incluidos). */
    private static boolean enPeriodo(Date fecha, LocalDate desde, LocalDate hasta) {
        LocalDate d = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return !d.isBefore(desde) && !d.isAfter(hasta);
    }

    /** Producción y fallas del período con su costo (S08 N.° 3, 4 y 5). */
    private static String produccion(Estado e, LocalDate desde, LocalDate hasta) {
        var b = new StringBuilder(encabezado("PRODUCCION Y COSTOS"))
                .append("Periodo: ").append(desde).append(" a ").append(hasta).append("\n\n");
        double costoLotes = 0;
        double perdidas = 0;
        int piezas = 0;
        int aprobadas = 0;
        var f = new SimpleDateFormat("dd/MM/yyyy");
        for (var o : e.operaciones) {
            if (!enPeriodo(o.getFecha(), desde, hasta)) {
                continue;
            }
            b.append(f.format(o.getFecha())).append(" | ").append(o.describir()).append('\n');
            if (o instanceof Produccion p) {
                costoLotes += p.calcularCostoTotal();
                piezas += p.getCantidad();
                aprobadas += p.getAprobadas();
            } else {
                perdidas += o.calcularCostoTotal();
            }
        }
        double porcentaje = piezas == 0 ? 0 : (piezas - aprobadas) * 100.0 / piezas;
        b.append(String.format("%nPiezas producidas: %d | Aprobadas: %d | Rechazadas en calidad: %d (%.1f %%)%n",
                piezas, aprobadas, piezas - aprobadas, porcentaje));
        b.append(String.format("Costo de producción: S/ %.2f | Pérdidas por fallas: S/ %.2f%n", costoLotes, perdidas));
        return b.toString();
    }

    /** Stock y valor del almacén de materias primas. */
    private static String materias(Estado e) {
        var b = new StringBuilder(encabezado("MATERIAS PRIMAS"));
        b.append(String.format("%-7s %-28s %10s %-7s %10s %10s %10s%n", "Cod.", "Materia prima", "Stock",
                "Unidad", "Mínimo", "Costo", "Valor"));
        double valor = 0;
        for (var m : e.materias) {
            double subtotal = m.getStock() * m.getCostoUnitario();
            valor += subtotal;
            b.append(String.format("%-7s %-28s %10s %-7s %10s %10.2f %10.2f%s%n", m.getIdMateria(), m.getNombre(),
                    MateriaPrima.formato(m.getStock()), m.getUnidad(), MateriaPrima.formato(m.getStockMinimo()),
                    m.getCostoUnitario(), subtotal, m.tieneStockBajo() ? " *" : ""));
        }
        return b.append(String.format("%nValor del almacén de materias primas: S/ %.2f   (* = stock bajo)%n", valor)).toString();
    }

    /** Pedidos y stock comprometido por producto. */
    private static String pedidos(Estado e) {
        var b = new StringBuilder(encabezado("PEDIDOS"));
        for (var p : e.pedidos) {
            b.append(p.getResumen()).append("\n\n");
        }
        b.append("Stock comprometido por pedidos activos:\n");
        for (var p : e.productos) {
            int comprometido = e.stockComprometido(p.getIdProducto());
            if (comprometido > 0) {
                String situacion = comprometido > p.getStock() ? "faltan " + (comprometido - p.getStock()) : "cubierto";
                b.append(String.format("  %-6s %-26s comprometido %4d | stock %4d | %s%n",
                        p.getIdProducto(), p.getNombre(), comprometido, p.getStock(), situacion));
            }
        }
        return b.toString();
    }

    /** Segmentación de clientes para campañas (S06). */
    private static String clientes(Estado e) {
        var b = new StringBuilder(encabezado("CLIENTES POR CATEGORIA"));
        for (String categoria : Cliente.CATEGORIAS) {
            var lista = e.clientes.stream()
                    .filter(c -> !c.esClienteVarios() && c.getCategoria().equals(categoria))
                    .toList();
            double total = e.ventas.stream()
                    .filter(v -> lista.stream().anyMatch(c -> c.getIdCliente().equals(v.getCliente().getIdCliente())))
                    .mapToDouble(Venta::getMontoTotal)
                    .sum();
            b.append(String.format("%s: %d clientes | compras S/ %.2f%n", categoria.toUpperCase(), lista.size(), total));
            for (var c : lista) {
                b.append("   ").append(c.mostrarPersona()).append('\n');
            }
            b.append('\n');
        }
        return b.toString();
    }

    /** Guarda el reporte en PDF o TXT y devuelve el archivo. */
    public File exportar(String tipo, String contenido, String formato) throws IOException {
        s.exigir("REPORTES");
        String marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
        File f = new File(GestorArchivos.CARPETA_REPORTES, "REP-" + marca + "-" + tipo + "." + formato.toLowerCase());
        if (formato.equals("PDF")) {
            ArchivoPDF.escribir(f, Arrays.asList(contenido.split("\n")));
        } else {
            GestorArchivos.escribirTexto(f, contenido);
        }
        return f;
    }

    /** Reportes diarios y mensuales que se actualizan al iniciar y cerrar sesión. */
    public void automaticos() throws IOException {
        if (!s.permite("REPORTES")) {
            return;
        }
        Estado e = s.consultar();
        for (String tipo : List.of("VENTAS_DIARIAS", "VENTAS_MENSUALES")) {
            new Reporte(e.ventas, e.productos).generar(tipo).exportar("TXT");
        }
    }
}
