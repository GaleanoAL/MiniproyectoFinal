package modelo.carta;

/** Carta de Monstruo. Puede alternar entre modo ATK y DEF una sola vez por turno. */
public class Monstruo extends Carta {

    private int atk, def;
    private final int nivel;
    private boolean enAtaque        = true;
    private boolean cambioEsteTurno = false;

    public Monstruo(String nombre, int atk, int def, int nivel) {
        super(nombre);
        this.atk = atk; this.def = def; this.nivel = nivel;
    }

    public boolean puedeCambiarModo() { return !cambioEsteTurno; }
    public void cambiarModo()  { if (puedeCambiarModo()) { enAtaque = !enAtaque; cambioEsteTurno = true; } }
    public void resetCambio()  { cambioEsteTurno = false; }

    public int     getAtk()      { return atk; }
    public void    setAtk(int v) { atk = v; }
    public int     getDef()      { return def; }
    public void    setDef(int v) { def = v; }
    public int     getNivel()    { return nivel; }
    public boolean isEnAtaque()  { return enAtaque; }

    @Override
    public String toString() {
        return String.format("%s [Nv.%d ATK:%d DEF:%d %s]", nombre, nivel, atk, def, enAtaque ? "ATK" : "DEF");
    }
}
