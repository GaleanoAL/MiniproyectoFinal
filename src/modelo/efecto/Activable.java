package modelo.efecto;

import modelo.juego.Jugador;

/**
 * Patrón Strategy: cada carta mágica/trampa encapsula su propio algoritmo de efecto.
 * Permite intercambiar efectos sin modificar el controlador.
 */
public interface Activable {
    void activar(Jugador jugador, Jugador oponente);
}
