package vista;

import controlador.JuegoControlador;
import modelo.carta.Carta;
import modelo.carta.Monstruo;
import modelo.carta.Trampa;
import modelo.juego.Jugador;

import java.util.List;
import java.util.Scanner;

/**
 * Vista de consola — implementa VistaJuego (Observer).
 * Muestra el estado del juego en terminal y gestiona el menú de acciones.
 */
public class VistaConsola implements VistaJuego {

    private final JuegoControlador controlador;
    private final Scanner          sc;
    private boolean                juegoTerminado = false;

    public VistaConsola(JuegoControlador controlador) {
        this.controlador = controlador;
        this.sc          = new Scanner(System.in);
    }

    // ── Observer ─────────────────────────────────────────────────────
    @Override
    public void actualizar(Jugador j1, Jugador j2, boolean turnoJ1) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.printf( "║  %-20s  LP: %-6d  Campo: %d  ║%n", j1.getNombre(), j1.getLp(), j1.getCampo().size());
        System.out.printf( "║  %-20s  LP: %-6d  Campo: %d  ║%n", j2.getNombre(), j2.getLp(), j2.getCampo().size());
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("  Turno: " + (turnoJ1 ? j1.getNombre() : j2.getNombre()));
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        System.out.println("  » " + mensaje);
    }

    @Override
    public void mostrarGanador(String nombre) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║          ¡" + nombre.toUpperCase() + " GANA EL DUELO! ");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("  Resultado guardado en resultados.txt");
        juegoTerminado = true;
    }

    @Override
    public int pedirEleccion(String titulo, String[] opciones) {
        System.out.println("  " + titulo);
        for (int i = 0; i < opciones.length; i++)
            System.out.println("    " + i + ". " + opciones[i]);
        System.out.print("  > ");
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    // ── Menú principal ───────────────────────────────────────────────
    public void iniciar() {
        menuPrincipal();
    }

    private void menuPrincipal() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║   YU-GI-OH! — MENÚ PRINCIPAL ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.println("  1. Nuevo duelo");
        System.out.println("  2. Cargar partida guardada");
        System.out.println("  3. Ver estadísticas históricas");
        System.out.println("  4. Salir");
        System.out.print("  > ");

        switch (sc.nextLine().trim()) {
            case "1" -> iniciarDuelo();
            case "2" -> { controlador.cargarPartida(); if (!juegoTerminado) iniciarDuelo(); }
            case "3" -> { System.out.println(controlador.obtenerEstadisticas()); menuPrincipal(); }
            case "4" -> System.out.println("  ¡Hasta la próxima!");
            default  -> { System.out.println("  Opción no válida."); menuPrincipal(); }
        }
    }

    private void iniciarDuelo() {
        System.out.println("\n  ╔══════════════════════════╗");
        System.out.println("  ║  ¡ES HORA DEL DUELO!      ║");
        System.out.println("  ╚══════════════════════════╝");
        controlador.onRobar();
        while (!juegoTerminado) procesarTurno();
    }

    // ── Turno ────────────────────────────────────────────────────────
    private void procesarTurno() {
        Jugador actual = controlador.getActual();
        mostrarMano(actual);

        System.out.println("\n  Acciones:");
        System.out.println("   1. Jugar carta    2. Atacar");
        System.out.println("   3. Cambiar modo   4. Pasar turno");
        System.out.println("   5. Guardar        6. Cargar");
        System.out.println("   7. Deshacer jugada");
        System.out.print("  > ");

        switch (sc.nextLine().trim()) {
            case "1" -> accionJugarCarta();
            case "2" -> accionAtacar();
            case "3" -> accionCambiarModo();
            case "4" -> controlador.onPasarTurno();
            case "5" -> controlador.guardarPartida();
            case "6" -> controlador.cargarPartida();
            case "7" -> controlador.onDeshacer();
            default  -> System.out.println("  Opción no válida.");
        }
    }

    private void mostrarMano(Jugador actual) {
        System.out.println("\n  -- Mano de " + actual.getNombre() + " --");
        List<Carta> mano = actual.getMano();
        if (mano.isEmpty()) { System.out.println("   (vacía)"); return; }
        for (int i = 0; i < mano.size(); i++) {
            Carta c = mano.get(i);
            String extra = switch (c) {
                case Monstruo m -> " [Nv." + m.getNivel() + " ATK:" + m.getAtk() + " DEF:" + m.getDef() + "]";
                case Trampa t   -> " [Trampa]";
                default         -> " [Magia]";
            };
            System.out.println("   " + i + ". " + c.getNombre() + extra);
        }
    }

    private void accionJugarCarta() {
        System.out.print("  Índice de carta: ");
        try { controlador.onJugarCarta(Integer.parseInt(sc.nextLine().trim())); }
        catch (NumberFormatException e) { System.out.println("  Índice inválido."); }
    }

    private void accionAtacar() {
        Jugador atacante = controlador.getActual();
        Jugador defensor = controlador.getOponente();
        if (atacante.getCampo().isEmpty()) { System.out.println("  Sin monstruos."); return; }

        System.out.println("  Tu campo:");
        for (int i = 0; i < atacante.getCampo().size(); i++) {
            Monstruo m = atacante.getCampo().get(i);
            System.out.println("   " + i + ". " + m.getNombre() + " ATK:" + m.getAtk());
        }
        System.out.print("  Atacante [índice]: ");
        try {
            int idxAtk = Integer.parseInt(sc.nextLine().trim());
            if (defensor.getCampo().isEmpty()) { controlador.onAtacar(idxAtk, -1); return; }
            System.out.println("  Campo enemigo:");
            for (int i = 0; i < defensor.getCampo().size(); i++) {
                Monstruo m = defensor.getCampo().get(i);
                System.out.printf("   %d. %s [%s] ATK:%d DEF:%d%n",
                    i, m.getNombre(), m.isEnAtaque() ? "ATK" : "DEF", m.getAtk(), m.getDef());
            }
            System.out.print("  Defensor [índice]: ");
            controlador.onAtacar(idxAtk, Integer.parseInt(sc.nextLine().trim()));
        } catch (NumberFormatException e) { System.out.println("  Índice inválido."); }
    }

    private void accionCambiarModo() {
        Jugador actual = controlador.getActual();
        if (actual.getCampo().isEmpty()) { System.out.println("  Sin monstruos."); return; }
        System.out.println("  Tu campo:");
        for (int i = 0; i < actual.getCampo().size(); i++) {
            Monstruo m = actual.getCampo().get(i);
            System.out.println("   " + i + ". " + m.getNombre() + " [" + (m.isEnAtaque() ? "ATK" : "DEF") + "]");
        }
        System.out.print("  Monstruo [índice]: ");
        try { controlador.onCambiarModo(Integer.parseInt(sc.nextLine().trim())); }
        catch (NumberFormatException e) { System.out.println("  Índice inválido."); }
    }
}
