package modelo.carta;

import modelo.juego.Jugador;

/** Toma control del primer monstruo del campo enemigo. */
public class ChangeOfHeart extends Magica {
    public ChangeOfHeart() { super("Change of Heart"); }
    @Override
    public void activar(Jugador jugador, Jugador oponente) {
        if (!oponente.getCampo().isEmpty())
            jugador.getCampo().add(oponente.getCampo().remove(0));
    }
}
