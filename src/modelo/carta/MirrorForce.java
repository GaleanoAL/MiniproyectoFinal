package modelo.carta;

import modelo.juego.Jugador;

/** Trampa: destruye TODOS los monstruos atacantes del oponente cuando declara un ataque. */
public class MirrorForce extends Trampa {
    public MirrorForce() { super("Mirror Force"); }
    @Override
    public boolean condicion(Jugador propietario, Jugador atacante) {
        return !atacante.getCampo().isEmpty();
    }
    @Override
    public void activar(Jugador propietario, Jugador atacante) {
        atacante.getCampo().clear();
    }
}
