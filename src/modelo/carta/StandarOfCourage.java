package modelo.carta;

import modelo.juego.Jugador;

/** Aumenta el ATK de todos tus monstruos en 200. */
public class StandarOfCourage extends Magica {
    public StandarOfCourage() { super("Standar Of Courage"); }
    @Override
    public void activar(Jugador jugador, Jugador oponente) {
        jugador.getCampo().forEach(m -> m.setAtk(m.getAtk() + 200));
    }
}
