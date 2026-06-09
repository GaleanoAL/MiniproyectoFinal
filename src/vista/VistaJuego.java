package vista;

import modelo.juego.Jugador;

/**
 * Patrón Observer (RF3): interfaz que deben implementar todas las vistas suscritas al controlador.
 * El controlador notifica sin conocer los detalles de ninguna vista concreta.
 */
public interface VistaJuego {
    void actualizar(Jugador j1, Jugador j2, boolean turnoJ1);
    void mostrarMensaje(String mensaje);
    void mostrarGanador(String nombreGanador);
    int  pedirEleccion(String titulo, String[] opciones);
}
