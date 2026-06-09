package modelo.carta;

import modelo.juego.Jugador;

/** Aumenta el ATK del primer monstruo tuyo en 500. */
public class BoostAtk extends Magica {
    public BoostAtk() { super("Boost ATK +500"); }
    @Override
    public void activar(Jugador jugador, Jugador oponente) {
        if (!jugador.getCampo().isEmpty())
            jugador.getCampo().get(0).setAtk(jugador.getCampo().get(0).getAtk() + 500);
    }
}
