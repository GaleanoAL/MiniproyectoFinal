package modelo.carta;

import java.util.Random;
import modelo.juego.Jugador;

/** Lanza moneda: cara = tú robas 2 cartas; cruz = el oponente roba 2. */
public class AcesCoup extends Magica {
    public AcesCoup() { super("Aces Coup"); }
    @Override
    public void activar(Jugador jugador, Jugador oponente) {
        if (new Random().nextBoolean()) {
            jugador.robarCarta(); jugador.robarCarta();
        } else {
            oponente.robarCarta(); oponente.robarCarta();
        }
    }
}
