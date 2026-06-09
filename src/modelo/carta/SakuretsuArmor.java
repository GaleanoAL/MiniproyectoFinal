package modelo.carta;

import modelo.juego.Jugador;

/** Trampa: destruye el primer monstruo atacante del oponente. */
public class SakuretsuArmor extends Trampa {
    public SakuretsuArmor() { super("Sakuretsu Armor"); }
    @Override
    public boolean condicion(Jugador propietario, Jugador atacante) {
        return !atacante.getCampo().isEmpty();
    }
    @Override
    public void activar(Jugador propietario, Jugador atacante) {
        if (!atacante.getCampo().isEmpty())
            atacante.getCampo().remove(0);
    }
}
