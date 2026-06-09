import controlador.JuegoControlador;
import vista.VistaConsola;
import vista.VentanaYuGiOh;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada del programa.
 * Lanza simultáneamente la vista de consola y la interfaz gráfica (MVC multi-vista).
 */
public class App {
    public static void main(String[] args) {
        JuegoControlador controlador = new JuegoControlador();

        // Vista consola registrada primero (Observer)
        VistaConsola consola = new VistaConsola(controlador);
        controlador.agregarVista(consola);

        // Vista gráfica en el hilo de Swing
        SwingUtilities.invokeLater(() -> new VentanaYuGiOh(controlador));

        // Hilo principal atiende la consola
        consola.iniciar();
    }
}
