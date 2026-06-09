package modelo.carta;

import modelo.juego.Jugador;

/** Destruye todos los monstruos del oponente. */
public class Raigeki extends Magica {
    public Raigeki() { super("Raigeki"); }
    @Override
    public void activar(Jugador jugador, Jugador oponente) {
        oponente.getCampo().clear();
    }
}
