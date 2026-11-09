package proyectofina.servicios;
import java.io.*;
import java.util.*;
import proyectofina.*;
import proyectofina.persistencia.Estado;
public final class VentasServicio {
    private final Sistema s;
    public static final String[] PAGOS= {
        "Efectivo","Yape","Plin","Transferencia","Tarjeta"
    }
    ;
    VentasServicio(Sistema s) {
        this.s=s;
    }
    public Venta confirmar(String clienteId,Map<String,Integer> carrito,String pago,String tipo) throws IOException {
        if(carrito.isEmpty()) throw new IllegalArgumentException("Agrega al menos un producto.");
        if(!List.of(PAGOS).contains(pago)||!List.of("Boleta","Recibo").contains(tipo)) throw new IllegalArgumentException("Pago o comprobante inválido.");
        String[] nuevoId= {
            null
        }
        ;
        s.cambiar("VENTAS",e-> {
            var cliente=e.clientes.stream().filter(c->c.getIdCliente().equals(clienteId)).findFirst().orElseThrow(()->new IllegalArgumentException("Selecciona un cliente."));              var vendedor=e.usuarios.stream().filter(u->u.getIdUsuario().equals(s.usuario().getIdUsuario())).findFirst().orElseThrow();              int numero=e.ventas.stream().mapToInt(v->Integer.parseInt(v.getIdVenta().substring(1))).max().orElse(0)+1;              Venta v=new Venta(String.format("V%04d",numero),new Date(),cliente,vendedor,pago,tipo);              for(var item:carrito.entrySet()) {
                Inventario p=InventarioServicio.buscar(e,item.getKey());  int cantidad=item.getValue();                  if(cantidad<=0||cantidad>p.getStock()) throw new IllegalArgumentException("Stock insuficiente para "+p.getNombre()+". Disponible: "+p.getStock());                  v.agregarProducto(p,cantidad,p.getPrecio());  p.actualizarStock(p.getStock()-cantidad);                  e.movimientos.add(new Estado.Movimiento(new Date(),vendedor.getIdUsuario(),p.getIdProducto(),"VENTA",-cantidad,p.getStock()));
            }
            v.setEstado("Concluido");  e.ventas.add(v);  cliente.obtenerHistorialVentas().add(v); nuevoId[0]=v.getIdVenta();
        }
        );
        return s.consultar().ventas.stream().filter(v->v.getIdVenta().equals(nuevoId[0])).findFirst().orElseThrow();
    }
    public File comprobante(Venta v) throws IOException {
        s.exigir("VENTAS");
        Comprobante c=new Comprobante(v);
        c.emitirPDF();
        return c.getArchivo();
    }
}
