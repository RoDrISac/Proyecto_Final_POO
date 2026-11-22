package proyectofina;

import proyectofina.persistencia.sql.ConexionBD;
import proyectofina.persistencia.sql.RepositorioSQL;

/** Carga los datos de demostracion en una BD recien creada (scripts 01 y 02). */
public final class ReiniciarDemo {
    /** Carga los datos de demostración en una base recién creada. */
    public static void main(String[] args) throws Exception {
        var e = new RepositorioSQL(ConexionBD.desdeArchivo(ConexionBD.CONFIG)).cargar();
        System.out.println("Demo cargada: " + e.productos.size() + " productos, " + e.materias.size() + " materias primas, "
                + e.ventas.size() + " ventas, " + e.operaciones.size() + " operaciones, " + e.pedidos.size() + " pedidos.");
    }
}
