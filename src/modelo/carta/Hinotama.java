package modelo.carta;

import modelo.juego.Jugador;

/** Inflige 500 puntos de daño directo al oponente. */
public class Hinotama extends Magica {
    public Hinotama() { super("Hinotama"); }
    @Override
    public void activar(Jugador jugador, Jugador oponente) {
        oponente.recibirDano(500);
    }
}
