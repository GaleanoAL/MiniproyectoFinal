package modelo.carta;

import modelo.efecto.Activable;

/** Carta Mágica: se activa inmediatamente al jugarse. Cada subclase define su Strategy de efecto. */
public abstract class Magica extends Carta implements Activable {
    public Magica(String nombre) { super(nombre); }
}
