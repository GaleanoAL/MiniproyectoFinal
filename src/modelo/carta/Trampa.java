package modelo.carta;

import modelo.efecto.Activable;
import modelo.juego.Jugador;

/** Carta Trampa: se coloca boca abajo, se activa automáticamente al cumplirse su condición. */
public abstract class Trampa extends Carta implements Activable {

    private boolean disponible = true;

    public Trampa(String nombre) { super(nombre); }

    public boolean isDisponible()           { return disponible; }
    public void    setDisponible(boolean d) { disponible = d; }

    /** Condición que activa la trampa durante el ataque del oponente. */
    public abstract boolean condicion(Jugador propietario, Jugador atacante);
}
