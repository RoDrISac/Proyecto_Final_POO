package proyectofina.persistencia;
import java.io.IOException;
/** Contrato intercambiable: la aplicación no depende del formato de almacenamiento. */
public interface Repositorio {
    Estado cargar() throws IOException;
    void guardar(Estado estado) throws IOException;
}
