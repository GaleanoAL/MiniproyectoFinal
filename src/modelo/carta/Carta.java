package modelo.carta;

/** Clase base para todas las cartas. Instanciada dinámicamente por CartaFactory via Reflection. */
public abstract class Carta {
    protected final String nombre;
    public Carta(String nombre) { this.nombre = nombre; }
    public String getNombre()   { return nombre; }
    @Override public String toString() { return nombre; }
}
