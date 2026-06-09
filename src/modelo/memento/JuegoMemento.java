package modelo.memento;

import java.util.ArrayList;
import java.util.List;
import modelo.carta.Carta;
import modelo.carta.Monstruo;

/**
 * Patrón Memento (RF3): captura una instantánea completa del estado del juego.
 * Usada por el controlador para implementar "Deshacer última jugada" (undo)
 * y como paso previo al guardado en disco (RF2).
 */
public class JuegoMemento {

    private final int     lpJ1, lpJ2;
    private final boolean turnoJ1;
    private final List<Carta>    manoJ1, manoJ2;
    private final List<Monstruo> campoJ1, campoJ2;

    public JuegoMemento(int lpJ1, int lpJ2, boolean turnoJ1,
                        List<Carta> manoJ1, List<Carta> manoJ2,
                        List<Monstruo> campoJ1, List<Monstruo> campoJ2) {
        this.lpJ1   = lpJ1;   this.lpJ2   = lpJ2;   this.turnoJ1 = turnoJ1;
        this.manoJ1  = new ArrayList<>(manoJ1);
        this.manoJ2  = new ArrayList<>(manoJ2);
        this.campoJ1 = new ArrayList<>(campoJ1);
        this.campoJ2 = new ArrayList<>(campoJ2);
    }

    public int          getLpJ1()    { return lpJ1; }
    public int          getLpJ2()    { return lpJ2; }
    public boolean      isTurnoJ1()  { return turnoJ1; }
    public List<Carta>    getManoJ1()  { return manoJ1; }
    public List<Carta>    getManoJ2()  { return manoJ2; }
    public List<Monstruo> getCampoJ1() { return campoJ1; }
    public List<Monstruo> getCampoJ2() { return campoJ2; }
}
