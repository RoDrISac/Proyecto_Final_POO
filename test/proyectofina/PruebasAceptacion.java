package proyectofina;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import proyectofina.persistencia.*;
import proyectofina.servicios.*;

/** Pruebas de aceptación repetibles, sin dependencias externas ni datos del usuario. */
public final class PruebasAceptacion {
    static int aprobadas=0;
    static final List<String> resultados=new ArrayList<>();
    interface Prueba {void ejecutar() throws Exception;}
    static void verificar(String id,boolean condicion){if(!condicion)throw new AssertionError(id);aprobadas++;resultados.add("PASS | "+id);System.out.println(resultados.get(resultados.size()-1));}
    static void rechaza(String id,Prueba p) throws Exception {boolean rechazo=false;try{p.ejecutar();}catch(IllegalArgumentException|IllegalStateException e){rechazo=true;}verificar(id,rechazo);}
    static InventarioServicio.Datos producto(String id,String nombre,int stock,double precio){return new InventarioServicio.Datos(id,nombre,"Modelo A","Textil","Unidad","Prueba controlada",precio,5,stock,2);}
    public static void main(String[] args) throws Exception {
        Path salida=Path.of("docs/evidencias");Files.createDirectories(salida);
        Path datos=Files.createTempDirectory("colores-t3-test-");System.setProperty("app.datos",datos.toString());
        Path archivo=datos.resolve("estado-t3.dat");RepositorioArchivo repo=new RepositorioArchivo(archivo);Sistema s=new Sistema(repo);
        rechaza("PA01a - Sesión requerida",s::consultar);
        rechaza("PA01b - Credenciales inválidas",()->s.ingresar("U001","incorrecta"));
        s.ingresar("U001","1234");verificar("PA01c - Acceso administrador",s.permite("USUARIOS"));
        s.inventario.guardar(producto("PTEST","Producto histórico",10,12.50),true);
        verificar("PA02 - RF01/RF06 registro con unidad",s.consultar().productos.get(0).getUnidad().equals("Unidad"));
        rechaza("PA03a - Código duplicado",()->s.inventario.guardar(producto("ptest","Otro",3,1),true));
        rechaza("PA03b - Precio negativo",()->s.inventario.guardar(producto("NEG","Otro",3,-1),true));
        rechaza("PA03c - NaN rechazado",()->s.inventario.guardar(producto("NAN","Otro",3,Double.NaN),true));
        rechaza("PA03d - Stock negativo",()->s.inventario.guardar(producto("NEG","Otro",-1,5),true));
        rechaza("PA03e - Máximo dos decimales",()->s.inventario.guardar(producto("DEC","Otro",1,1.234),true));
        s.inventario.ingresar("ptest",4);verificar("PA04 - RF34/RF35 ingreso por código y saldo",s.consultar().productos.get(0).getStock()==14);
        s.clientes.registrar("CTEST","Cliente","Prueba","12345678","Dirección de prueba","999999999","demo@example.com");
        rechaza("PA05a - Cliente DNI inválido",()->s.clientes.registrar("C2","A","B","123","","",""));
        rechaza("PA05b - Cliente duplicado",()->s.clientes.registrar("C2","A","B","12345678","","",""));
        rechaza("PA05c - Correo inválido",()->s.clientes.registrar("C3","A","B","87654321","","","sin-arroba"));
        rechaza("PA06a - RF40 venta sin stock",()->s.ventas.confirmar("CTEST",Map.of("PTEST",15),"Yape","Boleta"));
        verificar("PA06b - Rechazo sin efectos parciales",s.consultar().ventas.isEmpty()&&s.consultar().productos.get(0).getStock()==14);
        rechaza("PA06c - Carrito vacío",()->s.ventas.confirmar("CTEST",Map.of(),"Yape","Boleta"));
        var v=s.ventas.confirmar("CTEST",Map.of("PTEST",12),"Yape","Boleta");
        verificar("PA07 - RF09/RF38/RF42/RF43/RF44 total, pago y stock",v.getMontoTotal()==150&&v.getMetodoPago().equals("Yape")&&s.consultar().productos.get(0).getStock()==2);
        verificar("PA08 - RF15 alerta stock bajo",s.consultar().productos.get(0).tieneStockBajo());
        var pdf=s.ventas.comprobante(v);verificar("PA09 - RF25 PDF generado",Files.readString(pdf.toPath(),java.nio.charset.StandardCharsets.ISO_8859_1).startsWith("%PDF-1.4"));
        Files.copy(pdf.toPath(),salida.resolve("comprobante-prueba.pdf"),StandardCopyOption.REPLACE_EXISTING);
        var comp=new Comprobante(v);verificar("PA10 - RF39 desglose IGV",comp.generarLineas().stream().anyMatch(l->l.contains("IGV (18%)")));
        s.inventario.guardar(producto("PTEST","Nombre nuevo",2,20),false);
        verificar("PA11 - RF07 edición sin alterar historia",s.consultar().ventas.get(0).getDetalles().get(0).getProducto().getNombre().equals("Producto histórico"));
        var v2=s.ventas.confirmar("CTEST",Map.of("PTEST",1),"Efectivo","Recibo");
        verificar("PA12 - RF27 numeración única",!new Comprobante(v).getIdComprobante().equals(new Comprobante(v2).getIdComprobante()));
        s.inventario.eliminar("PTEST");verificar("PA13 - RF08 baja conserva ventas",s.consultar().productos.isEmpty()&&s.consultar().ventas.size()==2);
        var recargado=new Sistema(new RepositorioArchivo(archivo));recargado.ingresar("U001","1234");
        verificar("PA14 - RF27 persistencia al reiniciar",recargado.consultar().ventas.size()==2&&recargado.consultar().clientes.size()==1);
        var pdf2=recargado.ventas.comprobante(recargado.consultar().ventas.get(0));
        verificar("PA15 - RF29 reimpresión idéntica",Arrays.equals(Files.readAllBytes(salida.resolve("comprobante-prueba.pdf")),Files.readAllBytes(pdf2.toPath())));
        var informes=new ReportesServicio(recargado);for(String tipo:ReportesServicio.TIPOS){String texto=informes.contenido(tipo,LocalDate.now().minusDays(1),LocalDate.now());verificar("PA16-"+tipo+" - RF10/RF18/RF19/RF20 reporte",texto.contains("COLORES Y CREACIONES"));}
        String mov=informes.contenido("MOVIMIENTOS",LocalDate.now(),LocalDate.now());verificar("PA17 - RF19 movimientos trazables",mov.contains("ENTRADA")&&mov.contains("VENTA")&&mov.contains("U001"));
        Files.writeString(salida.resolve("movimientos-prueba.txt"),mov);
        verificar("PA18 - RF19 filtro excluye otras fechas",!informes.contenido("MOVIMIENTOS",LocalDate.now().minusYears(2),LocalDate.now().minusYears(1)).contains("PTEST"));
        rechaza("PA19 - Fechas invertidas",()->informes.contenido("MOVIMIENTOS",LocalDate.now(),LocalDate.now().minusDays(1)));
        var exportado=informes.exportar("VENTAS",informes.contenido("VENTAS",LocalDate.now(),LocalDate.now()),"PDF");Files.copy(exportado.toPath(),salida.resolve("reporte-prueba.pdf"),StandardCopyOption.REPLACE_EXISTING);
        recargado.salir();recargado.ingresar("U002","1234");
        rechaza("PA20a - RF30 vendedor no modifica inventario",()->recargado.inventario.guardar(producto("X","X",1,1),true));
        rechaza("PA20b - RF30 vendedor no gestiona usuarios",()->recargado.usuarios.cambiarRol("U001","Vendedor"));
        rechaza("PA20c - RF30 vendedor no genera reportes",()->new ReportesServicio(recargado).contenido("VENTAS",LocalDate.now(),LocalDate.now()));
        recargado.salir();recargado.ingresar("U003","1234");rechaza("PA20d - RF30 supervisor no vende",()->recargado.ventas.confirmar("CTEST",Map.of("X",1),"Yape","Boleta"));
        recargado.salir();recargado.ingresar("U001","1234");recargado.usuarios.registrar("U004","Prueba","Usuario","44444444","clave","Vendedor");recargado.usuarios.cambiarRol("U004","Supervisor de almacen");
        verificar("PA21 - Gestión de usuarios y roles",recargado.consultar().usuarios.stream().anyMatch(u->u.getIdUsuario().equals("U004")&&u.getRol().tenerPermiso("INVENTARIO")));
        rechaza("PA22 - Administrador no degrada su propio rol",()->recargado.usuarios.cambiarRol("U001","Vendedor"));
        Estado antes=recargado.consultar();Repositorio falla=new Repositorio(){public Estado cargar(){return antes.copia();}public void guardar(Estado e)throws IOException{throw new IOException("Fallo de disco simulado");}};
        Sistema sf=new Sistema(falla);sf.ingresar("U001","1234");boolean fallo=false;try{sf.inventario.guardar(producto("FALLA","Falla",1,1),true);}catch(IOException ex){fallo=true;}
        verificar("PA23 - Fallo de guardado no altera memoria",fallo&&sf.consultar().productos.isEmpty());
        var bloqueo=new Sistema(repo);for(int i=0;i<3;i++)rechaza("PA24-"+i+" - Intento inválido",()->bloqueo.ingresar("U001","mal"));rechaza("PA24d - Bloqueo tras tres fallos",()->bloqueo.ingresar("U001","1234"));
        Estado copia=recargado.consultar();copia.ventas.clear();verificar("PA25 - Encapsulamiento mediante copia",recargado.consultar().ventas.size()==2);
        String detalle=String.join("\n",new Comprobante(v).generarLineas()).replace(',','.');
        verificar("PA26 - RF39 cálculo exacto IGV incluido",detalle.contains("127.12")&&detalle.contains("22.88")&&detalle.contains("150.00"));
        recargado.inventario.guardar(producto("DECIMAL","Decimal",10,0.10),true);
        var decimal=recargado.ventas.confirmar("CTEST",Map.of("DECIMAL",3),"Plin","Recibo");
        verificar("PA27 - RF38 precisión monetaria",decimal.getMontoTotal()==0.30);
        var cesta=new LinkedHashMap<String,Integer>();cesta.put("DECIMAL",1);cesta.put("INEXISTENTE",1);
        int cantidadAntes=recargado.consultar().ventas.size();
        rechaza("PA28a - RF40 carrito parcialmente inválido",()->recargado.ventas.confirmar("CTEST",cesta,"Plin","Recibo"));
        verificar("PA28b - Venta rechazada sin descuento parcial",recargado.consultar().productos.get(0).getStock()==7&&recargado.consultar().ventas.size()==cantidadAntes);
        Estado actual=recargado.consultar();Repositorio fallaVenta=new Repositorio(){public Estado cargar(){return actual.copia();}public void guardar(Estado e)throws IOException{throw new IOException("Fallo de disco simulado");}};
        var sv=new Sistema(fallaVenta);sv.ingresar("U001","1234");boolean falloVenta=false;try{sv.ventas.confirmar("CTEST",Map.of("DECIMAL",1),"Yape","Boleta");}catch(IOException ex){falloVenta=true;}
        verificar("PA29 - RF27 fallo de disco revierte venta y stock",falloVenta&&sv.consultar().ventas.size()==cantidadAntes&&sv.consultar().productos.get(0).getStock()==7);
        var productoRanking=new Producto("RANK","Producto ranking","M","C",10,"D");
        var antigua=new Venta("V0999",Date.from(LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()),v.getCliente(),v.getVendedor(),"Efectivo","Boleta");antigua.agregarProducto(productoRanking,100,10);
        var reciente=new Venta("V1000",new Date(),v.getCliente(),v.getVendedor(),"Efectivo","Boleta");reciente.agregarProducto(productoRanking,2,10);
        var ranking=new Reporte(List.of(antigua,reciente),List.of()).generar("MAS_VENDIDOS").getContenido();
        verificar("PA30 - RF20 ranking solo del mes actual",ranking.contains("2 unidades")&&!ranking.contains("102 unidades")&&!ranking.contains("100 unidades"));
        String reporteInventario=new Reporte(List.of(),recargado.consultar().productos).generar("INVENTARIO").getContenido();verificar("PA31 - RF18 contenido y saldo",reporteInventario.contains("DECIMAL")&&reporteInventario.contains("Productos listados: 1"));
        var reportesAuto=new ReportesServicio(recargado);reportesAuto.automaticos();verificar("PA32 - Reportes automáticos del día y mes",Files.list(datos.resolve("reportes")).filter(p->p.getFileName().toString().matches("REP-[0-9]{6,8}-VENTAS_(DIARIAS|MENSUALES)\\.txt")).count()==2);
        List<Persona> personas=List.of(v.getCliente(),v.getVendedor());
        verificar("PA33 - Polimorfismo Persona",personas.get(0).mostrarPersona().contains("CTEST")&&personas.get(1).mostrarPersona().contains("Rol:"));
        String resumen="EJECUCIÓN REAL: "+java.time.ZonedDateTime.now()+"\nJava: "+System.getProperty("java.version")+"\nDatos aislados: "+datos+"\n\n"+String.join("\n",resultados)+"\n\nTOTAL: "+aprobadas+" verificaciones aprobadas; 0 fallos.\nNo incluye prueba de impresora física ni lector físico.\n";
        Files.writeString(salida.resolve("resultados-pruebas.txt"),resumen);System.out.println("TOTAL PASS: "+aprobadas);
    }
}
