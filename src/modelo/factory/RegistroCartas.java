package modelo.factory;

import java.io.*;
import java.util.Properties;
import modelo.carta.Carta;

/**
 * Carga cartas dinámicamente desde archivos .txt en /cartas usando java.lang.reflect.
 * Agregar una carta nueva NO requiere recompilar ni tocar ningún switch — solo crear el .txt.
 *
 * Formato de cada archivo:
 *   nombre=Pot of Greed
 *   clase=modelo.carta.PotOfGreed
 *   tipo=Magica
 *   descripcion=Roba 2 cartas del mazo
 */
public class RegistroCartas {

    private static final String CARPETA = "cartas";

    /** Crea y devuelve un CartaFactory con todas las cartas registradas via Reflection. */
    public static CartaFactory crearFactory() {
        CartaFactory factory = new CartaFactory();

        File dir = new File(CARPETA);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("[RegistroCartas] Carpeta '" + CARPETA + "' no encontrada. Usando registro estático.");
            registroEstatico(factory);
            return factory;
        }

        File[] archivos = dir.listFiles(f -> f.getName().endsWith(".txt"));
        if (archivos == null || archivos.length == 0) {
            System.err.println("[RegistroCartas] Sin archivos .txt en /cartas. Usando registro estático.");
            registroEstatico(factory);
            return factory;
        }

        for (File archivo : archivos) {
            try {
                Properties props = new Properties();
                try (FileReader fr = new FileReader(archivo)) { props.load(fr); }

                String nombreCarta = props.getProperty("nombre");
                String nombreClase = props.getProperty("clase");
                if (nombreCarta == null || nombreClase == null) continue;

                // Reflection: Class.forName() + getDeclaredConstructor()
                @SuppressWarnings("unchecked")
                Class<? extends Carta> clase = (Class<? extends Carta>) Class.forName(nombreClase);

                factory.registrar(nombreCarta, clase);
                // Alias por nombre de archivo (sin extensión) para carga por ID
                factory.registrar(archivo.getName().replace(".txt", ""), clase);

                System.out.println("[RegistroCartas] Cargada: " + nombreCarta + " ← " + nombreClase);

            } catch (ClassNotFoundException e) {
                System.err.println("[RegistroCartas] Clase no encontrada en " + archivo.getName() + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("[RegistroCartas] Error al cargar " + archivo.getName() + ": " + e.getMessage());
            }
        }
        return factory;
    }

    /** Fallback estático si no existe la carpeta /cartas. */
    private static void registroEstatico(CartaFactory factory) {
        String[] clases = {
            "modelo.carta.PotOfGreed", "modelo.carta.Raigeki",    "modelo.carta.DarkHole",
            "modelo.carta.Hinotama",   "modelo.carta.ChangeOfHeart", "modelo.carta.StandarOfCourage",
            "modelo.carta.TyphoonOfMagicalSpace", "modelo.carta.AcesCoup",
            "modelo.carta.BoostAtk",   "modelo.carta.AceleronMiauravilloso",
            "modelo.carta.MirrorForce","modelo.carta.SakuretsuArmor"
        };
        for (String fc : clases) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Carta> c = (Class<? extends Carta>) Class.forName(fc);
                factory.registrar(fc.substring(fc.lastIndexOf('.') + 1), c);
            } catch (ClassNotFoundException e) {
                System.err.println("[RegistroCartas] " + e.getMessage());
            }
        }
    }
}
