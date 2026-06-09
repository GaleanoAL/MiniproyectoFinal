package modelo.carta;

import java.util.Random;
import modelo.juego.Jugador;

//  CARTAS MÁGICAS — cada una es un Strategy de efecto independiente
//  Cargadas dinámicamente desde /cartas/*.txt mediante Reflection (RF3-B)

/** Roba 2 cartas del mazo. */
public class PotOfGreed extends Magica {
    public PotOfGreed() { super("Pot of Greed"); }
    @Override
    public void activar(Jugador jugador, Jugador oponente) {
        jugador.robarCarta();
        jugador.robarCarta();
    }
}
