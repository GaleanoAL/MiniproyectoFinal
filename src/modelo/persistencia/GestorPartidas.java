package modelo.persistencia;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import modelo.carta.*;
import modelo.juego.Jugador;

/**
 * Singleton — gestiona toda la E/S de archivos de texto del juego.
 * Formato legible en bloc de notas: secciones [BLOQUE] con clave=valor.
 * Sin serialización binaria ni librerías externas (RF2).
 */
public class GestorPartidas {

    private static GestorPartidas instancia;
    private GestorPartidas() {}
    public static GestorPartidas getInstance() {
        if (instancia == null) instancia = new GestorPartidas();
        return instancia;
    }

    private static final String ARCHIVO_GUARDADO   = "partida_guardada.txt";
    private static final String ARCHIVO_RESULTADOS = "resultados.txt";
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── GUARDAR ──────────────────────────────────────────────────────
    public void guardarPartida(Jugador j1, Jugador j2,
                               boolean turnoJ1, int turnoActual) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_GUARDADO))) {
            pw.println("[PARTIDA]");
            pw.println("fecha="        + LocalDateTime.now().format(FMT));
            pw.println("turno_actual=" + turnoActual);
            pw.println("es_turno_j1="  + turnoJ1);
            pw.println();
            escribirJugador(pw, "JUGADOR1", j1);
            pw.println();
            escribirJugador(pw, "JUGADOR2", j2);
        }
    }

    private void escribirJugador(PrintWriter pw, String tag, Jugador j) {
        pw.println("[" + tag + "]");
        pw.println("nombre="    + j.getNombre());
        pw.println("lp="        + j.getLp());
        pw.println("mano="      + serializarCartasCompleto(j.getMano()));
        pw.println("campo="     + serializarMonstruos(j.getCampo()));
        pw.println("mazo_size=" + j.getMazo().size());
    }

    // ── CARGAR ───────────────────────────────────────────────────────
    public Map<String, String> cargarPartida() throws IOException {
        File f = new File(ARCHIVO_GUARDADO);
        if (!f.exists()) return null;

        Map<String, String> datos = new LinkedHashMap<>();
        String seccion = "";
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.startsWith("[") && linea.endsWith("]"))
                    seccion = linea.substring(1, linea.length() - 1).toLowerCase() + ".";
                else if (linea.contains("=")) {
                    int eq = linea.indexOf('=');
                    datos.put(seccion + linea.substring(0, eq).trim(),
                              linea.substring(eq + 1).trim());
                }
            }
        }
        return datos;
    }

    public boolean hayPartidaGuardada() { return new File(ARCHIVO_GUARDADO).exists(); }

    // ── HISTORIAL ────────────────────────────────────────────────────
    public void registrarResultado(String n1, String n2, String ganador,
                                   int turnos, int lp1, int lp2) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_RESULTADOS, true))) {
            pw.printf("%s | %s vs %s | Ganador: %s | Turnos: %d | LP: %d - %d%n",
                LocalDateTime.now().format(FMT), n1, n2, ganador, turnos, lp1, lp2);
        }
    }

    // ── ESTADÍSTICAS ─────────────────────────────────────────────────
    public String leerEstadisticas() throws IOException {
        File f = new File(ARCHIVO_RESULTADOS);
        if (!f.exists() || f.length() == 0) return "No hay partidas registradas aún.";

        Map<String, Integer> victorias = new LinkedHashMap<>();
        int total = 0, maxTurnos = 0;
        String masLarga = "";

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;
                total++;
                String[] p = linea.split("\\|");
                if (p.length < 4) continue;
                String g = p[2].replace("Ganador:", "").trim();
                victorias.merge(g, 1, Integer::sum);
                try {
                    int t = Integer.parseInt(p[3].replace("Turnos:", "").trim());
                    if (t > maxTurnos) { maxTurnos = t; masLarga = linea; }
                } catch (NumberFormatException ignored) {}
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Total de partidas: ").append(total).append("\n\n");
        sb.append("Victorias por duelista:\n");
        victorias.forEach((j, w) -> sb.append("  ").append(j).append(": ").append(w).append("\n"));
        if (maxTurnos > 0)
            sb.append("\nPartida más larga (").append(maxTurnos).append(" turnos):\n  ").append(masLarga);
        return sb.toString();
    }

    // ── SERIALIZACIÓN texto plano ─────────────────────────────────────
    /**
     * Serializa cartas de mano con suficiente info para reconstruirlas:
     * Para monstruos: NOMBRE|Monstruo|atk|def|nivel
     * Para mágicas/trampas: CLASE_SIMPLE|Magica  o  CLASE_SIMPLE|Trampa
     */
    private String serializarCartasCompleto(List<Carta> cartas) {
        if (cartas.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Carta c : cartas) {
            if (c instanceof Monstruo m) {
                sb.append(m.getNombre()).append("|Monstruo|")
                  .append(m.getAtk()).append("|").append(m.getDef()).append("|").append(m.getNivel());
            } else {
                // Guardar nombre de clase simple para poder reconstruir via Reflection
                sb.append(c.getClass().getSimpleName()).append("|Carta");
            }
            sb.append(";");
        }
        return sb.toString();
    }

    private String serializarMonstruos(List<Monstruo> monstruos) {
        if (monstruos.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Monstruo m : monstruos)
            sb.append(m.getNombre()).append("|").append(m.getAtk()).append("|")
              .append(m.getDef()).append("|").append(m.getNivel()).append("|")
              .append(m.isEnAtaque() ? "ATK" : "DEF").append(";");
        return sb.toString();
    }

    public List<Monstruo> deserializarCampo(String datos) {
        List<Monstruo> lista = new ArrayList<>();
        if (datos == null || datos.isBlank()) return lista;
        for (String entry : datos.split(";")) {
            if (entry.isBlank()) continue;
            String[] p = entry.split("\\|");
            if (p.length < 5) continue;
            try {
                Monstruo m = new Monstruo(p[0],
                    Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                if ("DEF".equals(p[4])) m.cambiarModo();
                lista.add(m);
            } catch (NumberFormatException ignored) {}
        }
        return lista;
    }

    /**
     * Deserializa la mano: reconstruye Monstruos con sus stats,
     * y cartas mágicas/trampa via Reflection por nombre de clase.
     */
    public List<Carta> deserializarMano(String datos) {
        List<Carta> lista = new ArrayList<>();
        if (datos == null || datos.isBlank()) return lista;
        for (String entry : datos.split(";")) {
            if (entry.isBlank()) continue;
            String[] p = entry.split("\\|");
            if (p.length < 2) continue;
            try {
                if ("Monstruo".equals(p[1]) && p.length >= 5) {
                    lista.add(new Monstruo(p[0],
                        Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4])));
                } else if ("Carta".equals(p[1])) {
                    // Reflection: reconstruir carta mágica/trampa por nombre de clase
                    Class<?> clase = Class.forName("modelo.carta." + p[0]);
                    lista.add((Carta) clase.getDeclaredConstructor().newInstance());
                }
            } catch (Exception ignored) {}
        }
        return lista;
    }
}
