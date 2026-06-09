package modelo.juego;

import java.util.*;
import modelo.carta.Carta;
import modelo.carta.Monstruo;
import modelo.carta.Trampa;

/**
 * Representa a un duelista con su mazo, mano, campo y trampas.
 *
 * Estructuras de datos (RF1):
 *  - Stack<Carta>      mazo   → LIFO: roba siempre la carta del tope, igual que barajar físico.
 *  - LinkedList<Carta> mano   → acceso por índice y eliminación en posición O(n), natural para mano.
 *  - List<Monstruo>    campo  → LinkedList: inserción/eliminación frecuente en cualquier posición.
 *  - TreeMap<Integer,List<Monstruo>> indicePorNivel → árbol ordenado por nivel para consultas rápidas.
 */
public class Jugador {

    private String nombre;
    private int    lp = 8000;

    // RF1 — Stack: mazo de robo LIFO
    private final Stack<Carta> mazo;

    // RF1 — LinkedList: mano del jugador
    private final LinkedList<Carta> mano = new LinkedList<>();

    // RF1 — LinkedList usada como campo (inserción/eliminación frecuente)
    private final List<Monstruo> campo = new LinkedList<>();

    // Trampas colocadas boca abajo
    private final List<Trampa> trampasColocadas = new ArrayList<>();

    // RF1 — TreeMap: índice de monstruos en campo ordenado por nivel (árbol BST interno)
    private final TreeMap<Integer, List<Monstruo>> indicePorNivel = new TreeMap<>();

    public Jugador(String nombre, Stack<Carta> mazo) {
        this.nombre = nombre;
        this.mazo   = mazo;
    }

    // ── Acciones ─────────────────────────────────────────────────────
    public void robarCarta() {
        if (!mazo.isEmpty()) mano.add(mazo.pop());
    }

    public void recibirDano(int dano) {
        lp = Math.max(0, lp - dano);
    }

    /** Añade un monstruo al campo y lo registra en el índice TreeMap. */
    public void invocarMonstruo(Monstruo m) {
        campo.add(m);
        indicePorNivel.computeIfAbsent(m.getNivel(), k -> new ArrayList<>()).add(m);
    }

    /** Elimina un monstruo del campo y actualiza el índice TreeMap. */
    public void removerMonstruo(Monstruo m) {
        campo.remove(m);
        List<Monstruo> lista = indicePorNivel.get(m.getNivel());
        if (lista != null) { lista.remove(m); if (lista.isEmpty()) indicePorNivel.remove(m.getNivel()); }
    }

    /** Devuelve los monstruos de campo ordenados por nivel (via TreeMap). */
    public List<Monstruo> getMonstruosPorNivel() {
        List<Monstruo> ordenados = new ArrayList<>();
        indicePorNivel.values().forEach(ordenados::addAll);
        return ordenados;
    }

    // ── Getters / setters ─────────────────────────────────────────────
    public String getNombre()               { return nombre; }
    public void   setNombre(String n)       { nombre = n; }
    public int    getLp()                   { return lp; }
    public void   setLp(int lp)            { this.lp = lp; }
    public LinkedList<Carta>  getMano()     { return mano; }
    public List<Monstruo>     getCampo()    { return campo; }
    public Stack<Carta>       getMazo()     { return mazo; }
    public List<Trampa> getTrampasColocadas() { return trampasColocadas; }
    public boolean isMazoVacio()            { return mazo.isEmpty(); }

    public void setMano(List<Carta> nuevaMano) {
        mano.clear(); mano.addAll(nuevaMano);
    }

    public void setCampo(List<Monstruo> nuevoCampo) {
        campo.clear();
        indicePorNivel.clear();
        nuevoCampo.forEach(this::invocarMonstruo);
    }
}
