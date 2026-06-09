package modelo.carta;

import modelo.juego.Jugador;

/** Aumenta el DEF del primer monstruo tuyo en 200. */
public class AceleronMiauravilloso extends Magica {
    public AceleronMiauravilloso() { super("Aceleron Miauravilloso"); }
    @Override
    public void activar(Jugador jugador, Jugador oponente) {
        if (!jugador.getCampo().isEmpty())
            jugador.getCampo().get(0).setDef(jugador.getCampo().get(0).getDef() + 200);
    }
}
