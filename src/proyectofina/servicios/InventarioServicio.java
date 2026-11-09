package proyectofina.servicios;
import java.io.IOException;
import java.util.Date;
import proyectofina.*;
import proyectofina.persistencia.Estado;
public final class InventarioServicio {
    private final Sistema s;
    InventarioServicio(Sistema s) {
        this.s = s;
    }
    public record Datos(String codigo, String nombre, String modelo, String categoria, String unidad, String descripcion, double precio, double costo, int stock, int minimo) {
    }
    public void guardar(Datos d, boolean nuevo) throws IOException {
        String id = Sistema.codigo(d.codigo());
        Sistema.texto(d.nombre(),"Nombre");
        Sistema.texto(d.modelo(),"Modelo");
        Sistema.texto(d.categoria(),"Categoría");
        Sistema.texto(d.unidad(),"Unidad");
        Sistema.dinero(d.precio());
        Sistema.dinero(d.costo());
        if (d.stock()<0 || d.minimo()<0 || d.stock()>1000000 || d.minimo()>1000000) throw new IllegalArgumentException("Stock y mínimo: de 0 a 1 000 000.");
        s.cambiar("INVENTARIO", e -> {
            Inventario actual = e.productos.stream().filter(p -> p.getIdProducto().equalsIgnoreCase(id)).findFirst().orElse(null);              if (nuevo && actual != null) throw new IllegalArgumentException("Ya existe ese código.");              if (!nuevo && actual == null) throw new IllegalArgumentException("El producto ya no existe.");              Inventario p = new Inventario(id,d.nombre().trim(),d.modelo().trim(),d.categoria().trim(),d.precio(),d.descripcion(),nuevo?d.stock():actual.getStock(),d.minimo(),d.costo(),new Date());              p.setUnidad(d.unidad().trim());              if (actual != null) e.productos.remove(actual);              e.productos.add(p);              if(nuevo) e.movimientos.add(new Estado.Movimiento(new Date(),s.usuario().getIdUsuario(),id,"INICIAL",p.getStock(),p.getStock()));
        }
        );
    }
    public void ingresar(String codigo,int cantidad) throws IOException {
        if(cantidad<=0 || cantidad>1000000) throw new IllegalArgumentException("La cantidad debe ser de 1 a 1 000 000.");
        s.cambiar("INVENTARIO", e -> {
            Inventario p = buscar(e,codigo);              if ((long)p.getStock()+cantidad>1000000) throw new IllegalArgumentException("El saldo excede 1 000 000.");              p.actualizarStock(p.getStock()+cantidad);  p.setFechaIngreso(new Date());              e.movimientos.add(new Estado.Movimiento(new Date(),s.usuario().getIdUsuario(),p.getIdProducto(),"ENTRADA",cantidad,p.getStock()));
        }
        );
    }
    public void eliminar(String codigo) throws IOException {
        s.cambiar("INVENTARIO", e -> {
            Inventario p=buscar(e,codigo);  e.productos.remove(p);              e.movimientos.add(new Estado.Movimiento(new Date(),s.usuario().getIdUsuario(),p.getIdProducto(),"BAJA",-p.getStock(),0));
        }
        );
    }
    static Inventario buscar(Estado e,String id) {
        return e.productos.stream().filter(p->p.getIdProducto().equalsIgnoreCase(id.trim())).findFirst().orElseThrow(()->new IllegalArgumentException("Producto no encontrado: "+id));
    }
}
