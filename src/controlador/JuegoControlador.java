package controlador;

import modelo.carta.*;
import modelo.comando.*;
import modelo.efecto.Activable;
import modelo.juego.Jugador;
import modelo.memento.JuegoMemento;
import modelo.persistencia.GestorPartidas;
import vista.VistaJuego;

import java.io.IOException;
import java.util.*;

/**
 * Este es el controlador principal del juego, el cerebro que conecta
 * lo logico en el modelo con lo que va a ver un usuario.
 *
 * Aplica 4 patrones de diseño:
 *   1. Observer:  cuando algo cambia, avisa a todas las vistas suscritas.
 *   2. Memento:   guarda una "foto" del estado para poder deshacer acciones.
 *   3. Command:   cada acción del jugador se encapsula como objeto, lo que
 *                permite ejecutarla y también revertirla.
 *   4, Singleton: GestorPartidas es único en toda la app, no hay duplicados.
 *
 * También usa tres estructuras de datos concretas (requisito RF1):
 *   - Queue<String>  eventos      -> cola FIFO para el log del turno.
 *   - Set<String>    cartasUsadas -> asegura que no se repitan registros.
 *   - Stack<Comando> historialCmd -> pila de comandos para el undo.
 */
public class JuegoControlador {

    private Jugador j1, j2;
    private boolean turnoJ1;
    private int     contadorTurnos = 0;

// Verificación de lo que puede hacer un jugador al iniciar una partida.
    private boolean haRobadoEsteTurno      = false;
    private boolean haJugadoCartaEsteTurno = false;
    private boolean haAtacadoEsteTurno     = false;
    private boolean primerTurnoRealizado   = false;

    // RF1 — Queue: log FIFO de eventos del turno
    private final Queue<String> eventos = new LinkedList<>();

    // RF1 — Set: cartas únicas activadas (unicidad garantizada)
    private final Set<String> cartasUsadas = new HashSet<>();

    // RF3 Command — historial para undo
    private final Stack<Comando> historialCmd = new Stack<>();

    // RF3 Memento — último snapshot en RAM
    private JuegoMemento ultimoEstado;

    // RF3 Singleton — gestor de archivos
    private final GestorPartidas gestor = GestorPartidas.getInstance();

    // RF3 Observer — registra los cambios.
    private final List<VistaJuego> vistas = new ArrayList<>();

    public JuegoControlador() {
        inicializarJuego();
    }

    /**  INICIALIZACIÓN:
     * Creamos el Pool de cartas que se usaran en la partida, y se repartiran en los dos mazos.
     * Genera 30 moubtrous con estadisticas escalonadas.
     * El ataque aumenta en 80 por cada uno y el nivel se rota entre 1 a 12.
    **/
    private void inicializarJuego() {
        List<Carta> pool = new ArrayList<>();
        String[] nombres = {
            "Mago Oscuro","Dragón Blanco","Exodia","Blue-Eyes","Red-Eyes",
            "Curse of Dragon","Gaia","Summoned Skull","Celtic Guardian","Dark Magician Girl",
            "Jinzo","Insect Queen","Morphing Jar","Sangan","Witch of the Black Forest",
            "Kuriboh","Man-Eater Bug","Cyber Dragon","Elemental HERO","Neo-Spacian",
            "Gravekeeper's","Vampire Lord","Zombie Master","Ryko","Lyla",
            "Judgment Dragon","Celestia","Wulf","Ehren","Gorz"
        };
        for (int i = 0; i < 30; i++)
            pool.add(new Monstruo(nombres[i % nombres.length] + " " + (i + 1),
                500 + i * 80, 500 + i * 60, 1 + (i % 12)));

        pool.add(new PotOfGreed());       pool.add(new PotOfGreed());
        pool.add(new BoostAtk());         pool.add(new AcesCoup());
        pool.add(new DarkHole());         pool.add(new Hinotama());
        pool.add(new ChangeOfHeart());    pool.add(new Raigeki());
        pool.add(new StandarOfCourage()); pool.add(new TyphoonOfMagicalSpace());
        pool.add(new AceleronMiauravilloso());
        for (int i = 0; i < 5; i++) pool.add(new MirrorForce());
        for (int i = 0; i < 5; i++) pool.add(new SakuretsuArmor());

     // Mezclamos el pool para que la distribución sea aleatoria
        Collections.shuffle(pool);

    // Creamos los dos mazos y los llenamos con 25 cartas cada uno,
    // tomando de a una del pool para que no se repitan entre mazos
        Stack<Carta> mazo1 = new Stack<>(), mazo2 = new Stack<>();
        for (int i = 0; i < 25; i++) {
            mazo1.push(pool.remove(0));
            mazo2.push(pool.remove(0));
        }

     // Creamos los jugadores con sus mazos ya listos
        j1 = new Jugador("Jugador 1", mazo1);
        j2 = new Jugador("Jugador 2", mazo2);

    // Cada jugador roba su mano inicial de 5 cartas
        for (int i = 0; i < 5; i++) { j1.robarCarta(); j2.robarCarta(); }
        turnoJ1 = new Random().nextBoolean();
    }

    public void setNombres(String n1, String n2) {
        j1.setNombre(n1); j2.setNombre(n2);
    }

    //  OBSERVER

    public void agregarVista(VistaJuego v) { vistas.add(v); }

    private void notificarVistas()            { vistas.forEach(v -> v.actualizar(j1, j2, turnoJ1)); }
    private void notificarMensaje(String msg) { registrarEvento(msg); vistas.forEach(v -> v.mostrarMensaje(msg)); }
    private void notificarGanador(String n)   { vistas.forEach(v -> v.mostrarGanador(n)); }

    //  PERSISTENCIA (RF2) 

    public void guardarPartida() {
    // Antes de guardar en disco, también hacemos un snapshot en RAM por si acaso.
        ultimoEstado = crearMemento();
        try {
            // El gestor (Singleton) escribe el estado en el archivo de texto
            gestor.guardarPartida(j1, j2, turnoJ1, contadorTurnos);
            notificarMensaje("Partida guardada correctamente en partida_guardada.txt");
        } catch (IOException e) {
            notificarMensaje("Error al guardar: " + e.getMessage());
        }
    }

    public void cargarPartida() {
        // Primero intentamos cargar desde el archivo en disco
        if (gestor.hayPartidaGuardada()) {
            try {
                Map<String, String> datos = gestor.cargarPartida();
                if (datos != null) {
                    // Nombres
                    j1.setNombre(datos.getOrDefault("jugador1.nombre", j1.getNombre()));
                    j2.setNombre(datos.getOrDefault("jugador2.nombre", j2.getNombre()));
                    // HP
                    j1.setLp(Integer.parseInt(datos.getOrDefault("jugador1.lp", "8000")));
                    j2.setLp(Integer.parseInt(datos.getOrDefault("jugador2.lp", "8000")));
                    // Turno
                    turnoJ1        = Boolean.parseBoolean(datos.getOrDefault("partida.es_turno_j1", "true"));
                    contadorTurnos = Integer.parseInt(datos.getOrDefault("partida.turno_actual", "0"));
                    // Campo (monstruos)
                    String raw1 = datos.getOrDefault("jugador1.campo", "");
                    String raw2 = datos.getOrDefault("jugador2.campo", "");
                    if (!raw1.isBlank()) j1.setCampo(gestor.deserializarCampo(raw1));
                    if (!raw2.isBlank()) j2.setCampo(gestor.deserializarCampo(raw2));
                    // Mano — CORREGIDO: ahora sí se restaura
                    String mano1 = datos.getOrDefault("jugador1.mano", "");
                    String mano2 = datos.getOrDefault("jugador2.mano", "");
                    if (!mano1.isBlank()) j1.setMano(gestor.deserializarMano(mano1));
                    if (!mano2.isBlank()) j2.setMano(gestor.deserializarMano(mano2));
                    // Reset de flags del turno
                    haRobadoEsteTurno = haJugadoCartaEsteTurno = haAtacadoEsteTurno = true;
                    primerTurnoRealizado = true;

                    notificarVistas();
                    notificarMensaje("Partida cargada. Turno " + contadorTurnos
                        + " — " + getActual().getNombre());
                    return;
                }
            } catch (IOException | NumberFormatException e) {
                notificarMensaje("Error cargando desde disco: " + e.getMessage());
            }
        }
        // Fallback Memento RAM
        if (ultimoEstado == null) { notificarMensaje("No hay partida guardada."); return; }
        restaurarMemento(ultimoEstado);
        ultimoEstado = null;
        notificarVistas();
        notificarMensaje("Estado restaurado desde memoria.");
    }

    public String obtenerEstadisticas() {
        try { return gestor.leerEstadisticas(); }
        catch (IOException e) { return "Error: " + e.getMessage(); }
    }

    // MEMENTO helpers
    // Toma una prueba del estado actual del juego: HP, manos y campos de ambos jugadores

    private JuegoMemento crearMemento() {
        return new JuegoMemento(j1.getLp(), j2.getLp(), turnoJ1,
            j1.getMano(), j2.getMano(), j1.getCampo(), j2.getCampo());
    }

    private void restaurarMemento(JuegoMemento m) {
        j1.setLp(m.getLpJ1());   j2.setLp(m.getLpJ2());
        j1.setMano(m.getManoJ1()); j2.setMano(m.getManoJ2());
        j1.setCampo(m.getCampoJ1()); j2.setCampo(m.getCampoJ2());
        turnoJ1 = m.isTurnoJ1();
    }

    //  ACCIONES DE JUEGO
    
    public void onRobar() {
        if (haRobadoEsteTurno) return;
        Jugador actual = getActual();
        if (actual.isMazoVacio()) {
            notificarMensaje(actual.getNombre() + " no tiene cartas. ¡Pierde!");
            notificarGanador(getOponente().getNombre());
            return;
        }
        actual.robarCarta();
        haRobadoEsteTurno = true;
        notificarMensaje(actual.getNombre() + " robó una carta.");
        notificarVistas();
    }

    public void onJugarCarta(int index) {
        if (haJugadoCartaEsteTurno) { notificarMensaje("Ya jugaste una carta este turno."); return; }
        if (!haRobadoEsteTurno) onRobar();

        Jugador actual = getActual(), oponente = getOponente();
        if (index < 0 || index >= actual.getMano().size()) return;

        // Snapshot Memento antes de la acción
        ultimoEstado = crearMemento();

        // Command
        ComandoJugarCarta cmd = new ComandoJugarCarta(actual, index);
        cmd.execute();
        historialCmd.push(cmd);

        Carta carta = actual.getMano().get(index);
        cartasUsadas.add(carta.getNombre());

        if (carta instanceof Monstruo m) {
            int nec = m.getNivel() >= 7 ? 2 : m.getNivel() >= 4 ? 1 : 0;
            if (nec > 0) {
                if (actual.getCampo().size() < nec) {
                    notificarMensaje("Necesitas " + nec + " sacrificio(s) para " + m.getNombre());
                    historialCmd.pop(); ultimoEstado = null; return;
                }
                int[] sacs = pedirSacrificios(nec, actual);
                if (sacs == null) { historialCmd.pop(); ultimoEstado = null; return; }
                Arrays.sort(sacs);
                for (int i = sacs.length - 1; i >= 0; i--) {
                    Monstruo s = actual.getCampo().get(sacs[i]);
                    actual.removerMonstruo(s);
                    notificarMensaje("Sacrificaste: " + s.getNombre());
                }
            }
            actual.getMano().remove(index);
            actual.invocarMonstruo(m);
            notificarMensaje(actual.getNombre() + " invoca " + m.getNombre()
                + " (ATK:" + m.getAtk() + " / DEF:" + m.getDef() + ")");

        } else if (carta instanceof Trampa t) {
            actual.getMano().remove(index);
            actual.getTrampasColocadas().add(t);
            notificarMensaje(actual.getNombre() + " coloca trampa: " + t.getNombre());

        } else if (carta instanceof Activable a) {
            actual.getMano().remove(index);
            a.activar(actual, oponente);
            notificarMensaje(actual.getNombre() + " activa " + carta.getNombre());
        }

        haJugadoCartaEsteTurno = true;
        notificarVistas();
        verificarVictoria();
    }

    public void onDeshacer() {
        if (ultimoEstado == null || historialCmd.isEmpty()) {
            notificarMensaje("No hay acción para deshacer."); return;
        }
        Comando c = historialCmd.pop();
        c.undo();
        restaurarMemento(ultimoEstado);
        ultimoEstado = null;
        haJugadoCartaEsteTurno = false;
        notificarVistas();
        notificarMensaje("Deshecha: " + c.descripcion());
    }

    public void onAtacar(int idxAtacante, int idxDefensor) {
        if (!primerTurnoRealizado) { notificarMensaje("El primer jugador no puede atacar en su primer turno."); return; }
        if (haAtacadoEsteTurno)    { notificarMensaje("Ya atacaste este turno."); return; }

        Jugador atacante = getActual(), defensor = getOponente();
        if (atacante.getCampo().isEmpty()) { notificarMensaje("No tienes monstruos para atacar."); return; }

        Monstruo atkM = atacante.getCampo().get(idxAtacante);

        // Verificar trampas
        Iterator<Trampa> it = defensor.getTrampasColocadas().iterator();
        while (it.hasNext()) {
            Trampa t = it.next();
            if (t.isDisponible() && t.condicion(defensor, atacante)) {
                it.remove();
                t.activar(defensor, atacante);
                notificarMensaje("¡TRAMPA! " + t.getNombre());
                haAtacadoEsteTurno = true;
                notificarVistas(); verificarVictoria(); return;
            }
        }

        // Combate
        if (defensor.getCampo().isEmpty()) {
            defensor.recibirDano(atkM.getAtk());
            notificarMensaje("¡ATAQUE DIRECTO! " + atkM.getNombre() + " → " + atkM.getAtk() + " dmg");
        } else {
            Monstruo defM = defensor.getCampo().get(idxDefensor);
            if (atkM.getAtk() > defM.getDef()) {
                int diff = atkM.getAtk() - defM.getDef();
                defensor.removerMonstruo(defM);
                defensor.recibirDano(diff);
                notificarMensaje(atkM.getNombre() + " destruye a " + defM.getNombre() + " — daño: " + diff);
            } else {
                notificarMensaje(atkM.getNombre() + " no pudo vencer a " + defM.getNombre());
            }
        }

        haAtacadoEsteTurno = true;
        notificarVistas(); verificarVictoria();
    }

    public void onCambiarModo(int index) {
        Jugador actual = getActual();
        if (index < 0 || index >= actual.getCampo().size()) return;
        Monstruo m = actual.getCampo().get(index);
        if (!m.puedeCambiarModo()) { notificarMensaje(m.getNombre() + " ya cambió de modo."); return; }
        m.cambiarModo();
        notificarMensaje(m.getNombre() + " → " + (m.isEnAtaque() ? "ATAQUE" : "DEFENSA"));
        notificarVistas();
    }

    public void onPasarTurno() {
        contadorTurnos++;
        turnoJ1 = !turnoJ1;
        haRobadoEsteTurno = haJugadoCartaEsteTurno = haAtacadoEsteTurno = false;
        primerTurnoRealizado = true;
        historialCmd.clear();
        ultimoEstado = null;
        j1.getCampo().forEach(Monstruo::resetCambio);
        j2.getCampo().forEach(Monstruo::resetCambio);
        notificarMensaje("── Turno " + contadorTurnos + " — " + getActual().getNombre() + " ──");
        notificarVistas();
        onRobar();
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private int[] pedirSacrificios(int cantidad, Jugador jugador) {
        String[] ops = jugador.getCampo().stream()
            .map(m -> m.getNombre() + " ATK:" + m.getAtk())
            .toArray(String[]::new);
        int[] res = new int[cantidad];
        for (int i = 0; i < cantidad; i++) {
            int idx = vistas.get(0).pedirEleccion("Sacrificio " + (i+1) + "/" + cantidad, ops);
            if (idx < 0) return null;
            res[i] = idx;
        }
        return res;
    }

    private void verificarVictoria() {
        String ganador = null;
        if      (j1.getLp() <= 0)                            ganador = j2.getNombre();
        else if (j2.getLp() <= 0)                            ganador = j1.getNombre();
        else if (j1.isMazoVacio() && j1.getMano().isEmpty()) ganador = j2.getNombre();
        else if (j2.isMazoVacio() && j2.getMano().isEmpty()) ganador = j1.getNombre();
        if (ganador != null) {
            try { gestor.registrarResultado(j1.getNombre(), j2.getNombre(),
                    ganador, contadorTurnos, j1.getLp(), j2.getLp()); }
            catch (IOException e) { notificarMensaje("No se pudo guardar resultado."); }
            notificarGanador(ganador);
        }
    }

    private void registrarEvento(String e) {
        eventos.offer(e);
        if (eventos.size() > 60) eventos.poll();
    }

    // ── Getters ───────────────────────────────────────────────────────
    public Jugador       getActual()          { return turnoJ1 ? j1 : j2; }
    public Jugador       getOponente()        { return turnoJ1 ? j2 : j1; }
    public Queue<String> getEventos()         { return eventos; }
    public Set<String>   getCartasUsadas()    { return cartasUsadas; }
    public int           getContadorTurnos()  { return contadorTurnos; }
    public boolean       hayComandoDeshacer() { return !historialCmd.isEmpty(); }
    public Jugador       getJ1()               { return j1; }
    public Jugador       getJ2()               { return j2; }
    public String        getJ1Nombre()         { return j1.getNombre(); }
    public String        getJ2Nombre()         { return j2.getNombre(); }
    public void          notificarVistasPublic() { notificarVistas(); }
}
