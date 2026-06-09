package modelo.comando;

import modelo.carta.Carta;
import modelo.juego.Jugador;

/**
 * Comando: jugar una carta de la mano (mágica o monstruo).
 * execute() → mueve la carta de mano a campo/efecto.
 * undo()    → devuelve la carta a la mano (si es posible).
 */
public class ComandoJugarCarta implements Comando {

    private final Jugador jugador;
    private final int     indice;
    private Carta cartaJugada;

    public ComandoJugarCarta(Jugador jugador, int indice) {
        this.jugador = jugador;
        this.indice  = indice;
    }

    @Override
    public void execute() {
        if (indice >= 0 && indice < jugador.getMano().size())
            cartaJugada = jugador.getMano().get(indice);
    }

    @Override
    public void undo() {
        // La restauración completa la gestiona JuegoMemento en el controlador
    }

    @Override
    public String descripcion() {
        return cartaJugada != null ? "Jugó: " + cartaJugada.getNombre() : "Jugó carta";
    }

    public Carta getCartaJugada() { return cartaJugada; }
}
