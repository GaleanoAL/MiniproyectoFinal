package modelo.factory;

import java.util.HashMap;
import java.util.Map;
import modelo.carta.Carta;

/**
 * Patrón Factory (RF3): crea instancias de Carta a partir de un nombre registrado.
 * El registro interno usa un HashMap<String, Class> (RF1 — Tabla Hash):
 * permite búsqueda O(1) por nombre de carta sin ningún if/switch.
 *
 * Las clases se cargan dinámicamente via Reflection (Class.forName + getDeclaredConstructor).
 */
public class CartaFactory {

    // RF1 — HashMap: mapeo nombre → clase, búsqueda O(1)
    private final Map<String, Class<? extends Carta>> registro = new HashMap<>();

    public void registrar(String nombre, Class<? extends Carta> clase) {
        registro.put(nombre, clase);
    }

    public Carta crear(String nombre) throws Exception {
        Class<? extends Carta> clase = registro.get(nombre);
        if (clase == null) throw new IllegalArgumentException("Carta no registrada: " + nombre);
        // Reflection: instanciación dinámica sin new explícito
        return clase.getDeclaredConstructor().newInstance();
    }

    public boolean contiene(String nombre) {
        return registro.containsKey(nombre);
    }

    public Map<String, Class<? extends Carta>> getRegistro() {
        return registro;
    }
}
