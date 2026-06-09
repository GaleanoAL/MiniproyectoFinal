package modelo.carta;

import modelo.juego.Jugador;

/** Destruye todos los monstruos de ambos campos. */
public class DarkHole extends Magica {
    public DarkHole() { super("Dark Hole"); }
    @Override
    public void activar(Jugador jugador, Jugador oponente) {
        jugador.getCampo().clear();
        oponente.getCampo().clear();
    }
}
