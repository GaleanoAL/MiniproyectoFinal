package vista;

import controlador.JuegoControlador;
import modelo.carta.Carta;
import modelo.carta.Monstruo;
import modelo.carta.Trampa;
import modelo.juego.Jugador;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Vista gráfica — implementa VistaJuego (Observer).
 * Diseño tipo "tablero de mesa" horizontal: campo del oponente arriba,
 * campo propio abajo, mano en la base — todo clickeable directamente.
 */
public class VentanaYuGiOh extends JFrame implements VistaJuego {

    // ── Paleta ────────────────────────────────────────────────────────
    private static final Color C_BG        = new Color(18, 42, 18);   // verde mesa
    private static final Color C_FELT      = new Color(22, 55, 22);   // feltro oscuro
    private static final Color C_LANE      = new Color(14, 35, 14);   // carril del campo
    private static final Color C_HEADER    = new Color(10, 20, 10);   // cabecera jugador
    private static final Color C_GOLD      = new Color(218, 180, 60);
    private static final Color C_WHITE     = new Color(240, 240, 230);
    private static final Color C_LP_OK     = new Color(80, 220, 100);
    private static final Color C_LP_WARN   = new Color(230, 180, 30);
    private static final Color C_LP_CRIT   = new Color(220, 60, 60);
    private static final Color C_ATK       = new Color(210, 70, 70);
    private static final Color C_DEF       = new Color(70, 130, 220);
    private static final Color C_MONSTER   = new Color(160, 110, 30);
    private static final Color C_MAGIC     = new Color(50, 110, 200);
    private static final Color C_TRAP      = new Color(150, 40, 150);
    private static final Color C_BTN       = new Color(30, 70, 30);
    private static final Color C_BTN_HOT   = new Color(180, 50, 50);
    private static final Color C_BTN_PASS  = new Color(60, 100, 60);
    private static final Color C_PANEL_LOG = new Color(5, 15, 5);
    private static final Color C_LOG_TEXT  = new Color(140, 220, 140);
    private static final Color C_SELECTED  = new Color(255, 230, 50);
    private static final Color C_INACTIVE  = new Color(80, 80, 80);

    private static final Font F_NAME  = new Font("SansSerif", Font.BOLD,  13);
    private static final Font F_LP    = new Font("Monospaced",Font.BOLD,  20);
    private static final Font F_CARD  = new Font("SansSerif", Font.BOLD,  10);
    private static final Font F_BTN   = new Font("SansSerif", Font.BOLD,  11);
    private static final Font F_LOG   = new Font("Monospaced",Font.PLAIN, 11);
    private static final Font F_STAT  = new Font("Monospaced",Font.BOLD,  10);
    private static final Font F_TITLE = new Font("Serif",     Font.BOLD,  14);
    private static final Font F_TINY  = new Font("SansSerif", Font.PLAIN,  9);

    // ── Estado UI ─────────────────────────────────────────────────────
    private final JuegoControlador ctrl;

    private JLabel   lblLpJ1, lblLpJ2, lblNomJ1, lblNomJ2, lblTurno, lblMazoJ1, lblMazoJ2;
    private JPanel   zonaCampoJ1, zonaCampoJ2, zonaTrampasJ1, zonaTrampasJ2;
    private JPanel   zonaMano;
    private JTextArea areaLog;
    private JButton  btnAtacar, btnDeshacer, btnJugar, btnCambiar, btnPasar;

    private int idxAtacanteSeleccionado = -1;

    // ─────────────────────────────────────────────────────────────────
    public VentanaYuGiOh(JuegoControlador ctrl) {
        this.ctrl = ctrl;

        setTitle("Yu-Gi-Oh! — Duelo");
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Construir UI PRIMERO
        construirUI();
        setVisible(true);

        // 2. Registrar como observer DESPUÉS de que la UI existe
        ctrl.agregarVista(this);

        // 3. Pedir datos (puede llamar notificarVistas → actualizar, pero ya la UI está lista)
        pedirDatosIniciales();
    }

    //  CONSTRUCCIÓN UI
    
    private void construirUI() {
        getContentPane().setBackground(C_BG);
        setLayout(new BorderLayout(4, 4));

        add(crearBarraSuperior(),   BorderLayout.NORTH);
        add(crearTablero(),         BorderLayout.CENTER);
        add(crearPanelDerecho(),    BorderLayout.EAST);
    }

    // ── Barra superior: estado de jugadores ──────────────────────────
    private JPanel crearBarraSuperior() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(C_HEADER);
        bar.setBorder(new EmptyBorder(6, 10, 6, 10));

        bar.add(crearInfoJugador(true),  BorderLayout.WEST);
        bar.add(crearCentroTurno(),      BorderLayout.CENTER);
        bar.add(crearInfoJugador(false), BorderLayout.EAST);
        return bar;
    }

    private JPanel crearInfoJugador(boolean esJ1) {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 1));
        p.setBackground(C_HEADER);
        p.setPreferredSize(new Dimension(260, 70));

        JLabel nom = new JLabel(esJ1 ? "Jugador 1" : "Jugador 2",
                                esJ1 ? SwingConstants.LEFT : SwingConstants.RIGHT);
        nom.setFont(F_NAME);
        nom.setForeground(C_GOLD);

        JLabel lp = new JLabel("HP: 8000",
                               esJ1 ? SwingConstants.LEFT : SwingConstants.RIGHT);
        lp.setFont(F_LP);
        lp.setForeground(C_LP_OK);

        JLabel mazo = new JLabel("Mazo: 25 | Trampas: 0",
                                 esJ1 ? SwingConstants.LEFT : SwingConstants.RIGHT);
        mazo.setFont(F_TINY);
        mazo.setForeground(new Color(160, 200, 160));

        if (esJ1) { lblNomJ1 = nom; lblLpJ1 = lp; lblMazoJ1 = mazo; }
        else      { lblNomJ2 = nom; lblLpJ2 = lp; lblMazoJ2 = mazo; }

        p.add(nom); p.add(lp); p.add(mazo);
        return p;
    }

    private JPanel crearCentroTurno() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_HEADER);
        lblTurno = new JLabel("TURNO DE...", SwingConstants.CENTER);
        lblTurno.setFont(F_TITLE);
        lblTurno.setForeground(C_WHITE);
        p.add(lblTurno);
        return p;
    }

    // ── Tablero central ───────────────────────────────────────────────
    private JPanel crearTablero() {
        JPanel tablero = new JPanel(new GridLayout(5, 1, 0, 3));
        tablero.setBackground(C_BG);
        tablero.setBorder(new EmptyBorder(4, 8, 4, 4));

        // Fila 1: campo monstruos J2 (oponente)
        zonaCampoJ2    = crearZonaCampo();
        // Fila 2: trampas J2
        zonaTrampasJ2  = crearZonaTrampas("Trampas oponente");
        // Fila 3: divisor central
        JPanel divisor = crearDivisorCentral();
        // Fila 4: trampas J1
        zonaTrampasJ1  = crearZonaTrampas("Mis trampas");
        // Fila 5: campo monstruos J1 (propio)
        zonaCampoJ1    = crearZonaCampo();

        tablero.add(envolver(zonaCampoJ2,   "CAMPO OPONENTE",  false));
        tablero.add(envolver(zonaTrampasJ2, null,              false));
        tablero.add(divisor);
        tablero.add(envolver(zonaTrampasJ1, null,              false));
        tablero.add(envolver(zonaCampoJ1,   "MI CAMPO",        false));

        // Panel inferior: mano + log
        JPanel base = new JPanel(new BorderLayout(4, 0));
        base.setBackground(C_BG);
        base.setBorder(new EmptyBorder(0, 8, 6, 4));
        zonaManoyLog(base);

        JPanel wrap = new JPanel(new BorderLayout(0, 3));
        wrap.setBackground(C_BG);
        wrap.add(tablero, BorderLayout.CENTER);
        wrap.add(base,    BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel crearZonaCampo() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        p.setBackground(C_LANE);
        p.setPreferredSize(new Dimension(0, 100));
        return p;
    }

    private JPanel crearZonaTrampas(String titulo) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        p.setBackground(new Color(20, 40, 20));
        p.setPreferredSize(new Dimension(0, 44));
        JLabel lbl = new JLabel(titulo + ":");
        lbl.setFont(F_TINY);
        lbl.setForeground(new Color(160, 160, 160));
        p.add(lbl);
        return p;
    }

    private JPanel crearDivisorCentral() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(10, 30, 10));
        p.setPreferredSize(new Dimension(0, 28));
        JLabel lbl = new JLabel("─────────────────  ZONA DE COMBATE  ─────────────────");
        lbl.setFont(new Font("Monospaced", Font.BOLD, 11));
        lbl.setForeground(new Color(60, 120, 60));
        p.add(lbl);
        return p;
    }

    private JPanel envolver(JPanel inner, String titulo, boolean borde) {
        if (titulo == null) return inner;
        JPanel w = new JPanel(new BorderLayout());
        w.setBackground(C_LANE);
        JLabel lbl = new JLabel(" " + titulo, SwingConstants.LEFT);
        lbl.setFont(F_TINY);
        lbl.setForeground(new Color(140, 180, 140));
        lbl.setBorder(new EmptyBorder(0, 4, 0, 0));
        w.add(lbl, BorderLayout.NORTH);
        w.add(inner, BorderLayout.CENTER);
        return w;
    }

    private void zonaManoyLog(JPanel base) {
        // Zona mano (izquierda)
        zonaMano = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        zonaMano.setBackground(new Color(10, 25, 10));
        JScrollPane scrollMano = new JScrollPane(zonaMano,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollMano.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60, 100, 60), 1),
            " MI MANO ", TitledBorder.LEFT, TitledBorder.TOP, F_TINY, new Color(140, 200, 140)));
        scrollMano.setPreferredSize(new Dimension(700, 120));
        scrollMano.getViewport().setBackground(new Color(10, 25, 10));

        // Log (derecha)
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setBackground(C_PANEL_LOG);
        areaLog.setForeground(C_LOG_TEXT);
        areaLog.setFont(F_LOG);
        areaLog.setLineWrap(true);
        areaLog.setWrapStyleWord(true);
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60, 100, 60), 1),
            " LOG ", TitledBorder.LEFT, TitledBorder.TOP, F_TINY, new Color(140, 200, 140)));
        scrollLog.setPreferredSize(new Dimension(340, 120));

        base.add(scrollMano, BorderLayout.CENTER);
        base.add(scrollLog,  BorderLayout.EAST);
    }

    // ── Panel derecho: botones de acción ──────────────────────────────
    private JPanel crearPanelDerecho() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_HEADER);
        p.setBorder(new EmptyBorder(8, 6, 8, 8));
        p.setPreferredSize(new Dimension(130, 0));

        // ACCIONES PRINCIPALES
        seccion(p, "ACCIONES");

        btnJugar   = btn("JUGAR CARTA",   C_MAGIC,   e -> accionJugarCarta());
        btnAtacar  = btn(" ATACAR",      C_ATK,     e -> iniciarAtaque());
        btnCambiar = btn(" MODO ATK/DEF",C_BTN,     e -> accionCambiarModo());
        btnPasar   = btn(" PASAR TURNO", C_BTN_PASS,e -> { idxAtacanteSeleccionado = -1; refrescarCampo(); ctrl.onPasarTurno(); });

        p.add(btnJugar);   gap(p, 4);
        p.add(btnAtacar);  gap(p, 4);
        p.add(btnCambiar); gap(p, 4);
        p.add(btnPasar);

        gap(p, 14);
        seccion(p, "SISTEMA");

        btnDeshacer = btn(" DESHACER",   new Color(80, 50, 120), e -> ctrl.onDeshacer());
        p.add(btnDeshacer);             gap(p, 4);
        p.add(btn(" GUARDAR",          new Color(30, 90, 60),  e -> ctrl.guardarPartida())); gap(p, 4);
        p.add(btn(" CARGAR",           new Color(60, 50, 90),  e -> ctrl.cargarPartida())); gap(p, 4);
        p.add(btn(" STATS",            new Color(70, 60, 20),  e -> mostrarStats()));

        p.add(Box.createVerticalGlue());

        // Indicador de selección de atacante
        JLabel hint = new JLabel("<html><center><i>Clic en tu<br>monstruo<br>para atacar</i></center></html>",
                                 SwingConstants.CENTER);
        hint.setFont(F_TINY);
        hint.setForeground(new Color(120, 160, 120));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(hint);
        return p;
    }

    private JButton btn(String t, Color bg, ActionListener al) {
        JButton b = new JButton("<html><center>" + t + "</center></html>");
        b.setFont(F_BTN);
        b.setForeground(C_WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setBorder(new EmptyBorder(5, 6, 5, 6));
        Color ho = bg.brighter();
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(ho); }
            public void mouseExited(MouseEvent e)  { b.setBackground(bg); }
        });
        b.addActionListener(al);
        return b;
    }

    private void seccion(JPanel p, String t) {
        JLabel l = new JLabel("── " + t + " ──", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 9));
        l.setForeground(C_GOLD);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        p.add(l); gap(p, 4);
    }

    private void gap(JPanel p, int h) { p.add(Box.createVerticalStrut(h)); }

    //  INICIO

    private void pedirDatosIniciales() {
        // Pantalla de bienvenida al usuario, aqui el jugador decide si, jugar una nueva
        // partida o ejecutar una partida guardada en el txt.
        String[] opMenu = {"Nuevo duelo", "Cargar partida guardada"};
        int op = JOptionPane.showOptionDialog(this,
            "¡Es hora del duelo!\n¿Qué deseas hacer?",
            "Yu-Gi-Oh!", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
            null, opMenu, opMenu[0]);

        if (op == 1) {
            // Cargar partida: los nombres vienen del archivo
            ctrl.cargarPartida();
            // Sincronizar etiquetas con los nombres ya cargados
            lblNomJ1.setText(ctrl.getActual().getNombre().equals(ctrl.getOponente().getNombre())
                ? "Jugador 1" : ctrl.getJ1Nombre());
            lblNomJ2.setText(ctrl.getJ2Nombre());
            return;
        }

        // Nuevo duelo: pedir nombres
        String n1 = JOptionPane.showInputDialog(this, "Nombre del Combaiente 1:", "Combatiente 1");
        String n2 = JOptionPane.showInputDialog(this, "Nombre del Combatiente 2:", "Combatiente 2");
        n1 = (n1 == null || n1.isBlank()) ? "Combatiente 1"  : n1.trim();
        n2 = (n2 == null || n2.isBlank()) ? "Combatiente 2" : n2.trim();
        ctrl.setNombres(n1, n2);
        lblNomJ1.setText(n1);
        lblNomJ2.setText(n2);
        ctrl.onRobar();
        ctrl.notificarVistasPublic();
    }

 
    //  OBSERVER: actualizar todo lo que esta sucediendo en el campo de batalla.

    @Override
    public void actualizar(Jugador j1, Jugador j2, boolean turnoJ1) {
        // Nombres
        lblNomJ1.setText(j1.getNombre());
        lblNomJ2.setText(j2.getNombre());

        // LP con color según estado
        setLP(lblLpJ1, j1.getLp());
        setLP(lblLpJ2, j2.getLp());

        // Mazo / trampas info
        lblMazoJ1.setText("Mazo: " + j1.getMazo().size()
            + "  |  Mano: " + j1.getMano().size()
            + "  |  Trampas: " + j1.getTrampasColocadas().size());
        lblMazoJ2.setText("Mazo: " + j2.getMazo().size()
            + "  |  Mano: " + j2.getMano().size()
            + "  |  Trampas: " + j2.getTrampasColocadas().size());

        // Turno
        Jugador actual = turnoJ1 ? j1 : j2;
        lblTurno.setText("TURNO  ▶  " + actual.getNombre().toUpperCase());
        lblTurno.setForeground(turnoJ1 ? new Color(120, 200, 255) : new Color(255, 160, 120));

        // Campos
        renderCampo(zonaCampoJ1, j1.getCampo(), true);
        renderCampo(zonaCampoJ2, j2.getCampo(), false);

        // Trampas
        renderTrampas(zonaTrampasJ1, j1.getTrampasColocadas());
        renderTrampas(zonaTrampasJ2, j2.getTrampasColocadas());

        // Mano del jugador activo
        renderMano(actual.getMano());

        // Botones
        btnDeshacer.setEnabled(ctrl.hayComandoDeshacer());

        revalidate(); repaint();
    }

    private void setLP(JLabel lbl, int lp) {
        lbl.setText("LP  " + String.format("%,d", lp));
        lbl.setForeground(lp >= 4000 ? C_LP_OK : lp >= 2000 ? C_LP_WARN : C_LP_CRIT);
    }

 
    //  RENDER CAMPO

    private void renderCampo(JPanel zona, List<Monstruo> campo, boolean esMio) {
        zona.removeAll();
        for (int i = 0; i < campo.size(); i++) {
            Monstruo m   = campo.get(i);
            int      idx = i;
            zona.add(tarjetaMonstruo(m, idx, esMio));
        }
        if (campo.isEmpty()) {
            JLabel vacio = new JLabel("  (campo vacío)");
            vacio.setFont(F_TINY);
            vacio.setForeground(new Color(80, 110, 80));
            zona.add(vacio);
        }
        zona.revalidate(); zona.repaint();
    }

    private void renderTrampas(JPanel zona, List<Trampa> trampas) {
        zona.removeAll();
        if (trampas.isEmpty()) {
            JLabel v = new JLabel("(sin trampas)");
            v.setFont(F_TINY);
            v.setForeground(new Color(80, 100, 80));
            zona.add(v);
        }
        for (Trampa t : trampas) {
            JLabel lbl = new JLabel("🪤 " + t.getNombre());
            lbl.setFont(F_TINY);
            lbl.setForeground(new Color(200, 130, 200));
            lbl.setBorder(new EmptyBorder(1, 4, 1, 4));
            zona.add(lbl);
        }
        zona.revalidate(); zona.repaint();
    }

    private void renderMano(List<Carta> mano) {
        zonaMano.removeAll();
        for (int i = 0; i < mano.size(); i++) {
            Carta c   = mano.get(i);
            int   idx = i;
            zonaMano.add(tarjetaMano(c, idx));
        }
        if (mano.isEmpty()) {
            JLabel v = new JLabel("  (mano vacía)");
            v.setFont(F_TINY);
            v.setForeground(new Color(100, 130, 100));
            zonaMano.add(v);
        }
        zonaMano.revalidate(); zonaMano.repaint();
    }

    //  TARJETAS VISUALES
  
    /** Tarjeta de monstruo en el campo — clickeable para atacar (campo propio) o cambiar modo. */
    private JPanel tarjetaMonstruo(Monstruo m, int idx, boolean esMio) {
        boolean seleccionado = esMio && idx == idxAtacanteSeleccionado;
        Color bgCard = m.isEnAtaque() ? new Color(55, 20, 20) : new Color(15, 30, 65);
        Color borde  = seleccionado ? C_SELECTED : m.isEnAtaque() ? C_ATK : C_DEF;

        JPanel card = new JPanel(new BorderLayout(0, 1));
        card.setPreferredSize(new Dimension(105, 90));
        card.setBackground(bgCard);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borde, seleccionado ? 3 : 1, true),
            new EmptyBorder(3, 4, 3, 4)));
        card.setCursor(esMio ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        // Nombre
        String n = m.getNombre().length() > 13 ? m.getNombre().substring(0, 12) + "…" : m.getNombre();
        JLabel lblN = new JLabel("<html><center>" + n + "</center></html>", SwingConstants.CENTER);
        lblN.setFont(F_CARD);
        lblN.setForeground(C_WHITE);

        // Nivel + modo
        JLabel lblModo = new JLabel((m.isEnAtaque() ? "⚔ ATK" : "🛡 DEF") + "  Nv." + m.getNivel(),
                                    SwingConstants.CENTER);
        lblModo.setFont(F_TINY);
        lblModo.setForeground(m.isEnAtaque() ? C_ATK : C_DEF);

        // Stats
        JLabel lblAtk = new JLabel("ATK " + m.getAtk(), SwingConstants.LEFT);
        JLabel lblDef = new JLabel("DEF " + m.getDef(), SwingConstants.RIGHT);
        lblAtk.setFont(F_STAT);  lblAtk.setForeground(C_ATK);
        lblDef.setFont(F_STAT);  lblDef.setForeground(C_DEF);
        JPanel stats = new JPanel(new GridLayout(1, 2));
        stats.setBackground(bgCard);
        stats.add(lblAtk); stats.add(lblDef);

        card.add(lblN,    BorderLayout.CENTER);
        card.add(lblModo, BorderLayout.NORTH);
        card.add(stats,   BorderLayout.SOUTH);

        if (esMio) {
            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { clicEnMiMonstruo(idx); }
                public void mouseEntered(MouseEvent e) { card.setBackground(bgCard.brighter()); }
                public void mouseExited(MouseEvent e)  { card.setBackground(bgCard); }
            });
        } else {
            // Campo enemigo: clic para atacar si hay atacante seleccionado
            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { clicEnMonstruoEnemigo(idx); }
                public void mouseEntered(MouseEvent e) {
                    if (idxAtacanteSeleccionado >= 0) card.setBackground(new Color(60, 30, 30));
                }
                public void mouseExited(MouseEvent e)  { card.setBackground(bgCard); }
            });
        }
        return card;
    }

    /** Tarjeta de carta en la mano — siempre clickeable para jugarla. */
    private JPanel tarjetaMano(Carta c, int idx) {
        boolean esTrampa   = c instanceof Trampa;
        boolean esMagica   = c instanceof modelo.carta.Magica;
        boolean esMonstruo = c instanceof Monstruo;

        Color bg     = esTrampa ? new Color(50, 10, 50) : esMagica ? new Color(10, 25, 70) : new Color(55, 35, 5);
        Color borde  = esTrampa ? C_TRAP : esMagica ? C_MAGIC : C_MONSTER;
        String tipo  = esTrampa ? "TRAMPA" : esMagica ? "MAGIA" : "MONSTRUO";
        String extra = esMonstruo
            ? "Nv." + ((Monstruo)c).getNivel() + "  ATK:" + ((Monstruo)c).getAtk() + "\nDEF:" + ((Monstruo)c).getDef()
            : "";

        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setPreferredSize(new Dimension(108, 108));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borde, 2, true),
            new EmptyBorder(4, 4, 4, 4)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setToolTipText("Clic para jugar: " + c.getNombre());

        // Tipo en la cima
        JLabel lblTipo = new JLabel(tipo, SwingConstants.CENTER);
        lblTipo.setFont(new Font("SansSerif", Font.BOLD, 8));
        lblTipo.setForeground(borde);
        lblTipo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borde));

        // Nombre
        String nom = c.getNombre().length() > 16 ? c.getNombre().substring(0, 15) + "…" : c.getNombre();
        JLabel lblN = new JLabel("<html><center>" + nom + "</center></html>", SwingConstants.CENTER);
        lblN.setFont(F_CARD);
        lblN.setForeground(C_WHITE);

        // Stats si es monstruo
        JPanel sur = new JPanel(new GridLayout(esMonstruo ? 2 : 1, 1));
        sur.setBackground(bg);
        if (esMonstruo) {
            Monstruo m = (Monstruo) c;
            JLabel a = new JLabel("ATK " + m.getAtk(), SwingConstants.CENTER);
            JLabel d = new JLabel("DEF " + m.getDef(), SwingConstants.CENTER);
            a.setFont(F_STAT); a.setForeground(C_ATK);
            d.setFont(F_STAT); d.setForeground(C_DEF);
            sur.add(a); sur.add(d);
        }

        // Botón JUGAR pequeño al fondo
        JButton btnJugar = new JButton("JUGAR");
        btnJugar.setFont(new Font("SansSerif", Font.BOLD, 9));
        btnJugar.setForeground(Color.WHITE);
        btnJugar.setBackground(borde.darker());
        btnJugar.setFocusPainted(false);
        btnJugar.setBorderPainted(false);
        btnJugar.setOpaque(true);
        btnJugar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnJugar.addActionListener(e -> ctrl.onJugarCarta(idx));

        JPanel centro = new JPanel(new BorderLayout(0, 2));
        centro.setBackground(bg);
        centro.add(lblN, BorderLayout.CENTER);
        centro.add(sur,  BorderLayout.SOUTH);

        card.add(lblTipo,  BorderLayout.NORTH);
        card.add(centro,   BorderLayout.CENTER);
        card.add(btnJugar, BorderLayout.SOUTH);

        // Todo el panel también clickeable
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { ctrl.onJugarCarta(idx); }
            public void mouseEntered(MouseEvent e) { card.setBackground(bg.brighter()); centro.setBackground(bg.brighter()); sur.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e)  { card.setBackground(bg); centro.setBackground(bg); sur.setBackground(bg); }
        });
        return card;
    }

    //  LÓGICA DE INTERACCIÓN EN EL CAMPO
    
    private void clicEnMiMonstruo(int idx) {
        // Si ya hay uno seleccionado, cambiar modo o deseleccionar
        if (idxAtacanteSeleccionado == idx) {
            // Segundo clic en el mismo: cambiar modo
            ctrl.onCambiarModo(idx);
            idxAtacanteSeleccionado = -1;
        } else {
            idxAtacanteSeleccionado = idx;
            mostrarMensaje("Monstruo seleccionado para atacar. Ahora clic en el enemigo o en el campo vacío (ataque directo).");
        }
        refrescarCampo();
    }

    private void clicEnMonstruoEnemigo(int idx) {
        if (idxAtacanteSeleccionado < 0) {
            mostrarMensaje("Primero selecciona tu monstruo atacante haciendo clic en él.");
            return;
        }
        ctrl.onAtacar(idxAtacanteSeleccionado, idx);
        idxAtacanteSeleccionado = -1;
        refrescarCampo();
    }

    private void refrescarCampo() {
        // Re-render del campo del jugador actual para actualizar resaltado
        Jugador j1 = ctrl.getJ1(), j2 = ctrl.getJ2();
        renderCampo(zonaCampoJ1, j1.getCampo(), true);
        renderCampo(zonaCampoJ2, j2.getCampo(), false);
    }

    //  ACCIONES DE BOTONES SIDEBAR
    
    private void accionJugarCarta() {
        Jugador actual = ctrl.getActual();
        if (actual.getMano().isEmpty()) { mostrarMensaje("No tienes cartas en mano."); return; }
        // Diálogo de selección con detalles completos
        JPanel panel = new JPanel(new GridLayout(0, 1, 2, 2));
        panel.setBackground(C_FELT);
        List<Carta> mano = actual.getMano();
        JToggleButton[] btns = new JToggleButton[mano.size()];
        ButtonGroup   bg   = new ButtonGroup();
        for (int i = 0; i < mano.size(); i++) {
            Carta c = mano.get(i);
            String desc = switch(c) {
                case Monstruo m -> "  [MONSTRUO Nv." + m.getNivel() + "]  ATK:" + m.getAtk() + " / DEF:" + m.getDef();
                case Trampa t   -> "  [TRAMPA]  Se activa automáticamente";
                default         -> "  [MAGIA]   Efecto al activar";
            };
            JToggleButton tb = new JToggleButton((i+1) + ". " + c.getNombre() + desc);
            tb.setFont(F_BTN);
            tb.setBackground(new Color(25, 50, 25));
            tb.setForeground(C_WHITE);
            tb.setFocusPainted(false);
            tb.setHorizontalAlignment(SwingConstants.LEFT);
            bg.add(tb); btns[i] = tb; panel.add(tb);
        }
        JScrollPane sp = new JScrollPane(panel);
        sp.setPreferredSize(new Dimension(480, Math.min(300, mano.size() * 40 + 20)));
        int res = JOptionPane.showConfirmDialog(this, sp,
            "Jugar carta — " + actual.getNombre(),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        for (int i = 0; i < btns.length; i++)
            if (btns[i].isSelected()) { ctrl.onJugarCarta(i); return; }
        mostrarMensaje("No seleccionaste ninguna carta.");
    }

    private void iniciarAtaque() {
        Jugador atacante = ctrl.getActual();
        if (atacante.getCampo().isEmpty()) { mostrarMensaje("No tienes monstruos en campo."); return; }
        if (ctrl.getOponente().getCampo().isEmpty()) {
            // Ataque directo: elegir atacante
            Monstruo m = elegirMonstruoDialog(atacante.getCampo(), "Elige tu monstruo para ataque DIRECTO");
            if (m == null) return;
            ctrl.onAtacar(atacante.getCampo().indexOf(m), -1);
        } else {
            // Elegir atacante y defensor
            Monstruo atk = elegirMonstruoDialog(atacante.getCampo(), "Elige tu monstruo ATACANTE");
            if (atk == null) return;
            Monstruo def = elegirMonstruoDialog(ctrl.getOponente().getCampo(), "Elige el monstruo OBJETIVO");
            if (def == null) return;
            ctrl.onAtacar(atacante.getCampo().indexOf(atk), ctrl.getOponente().getCampo().indexOf(def));
        }
    }

    private void accionCambiarModo() {
        Jugador actual = ctrl.getActual();
        if (actual.getCampo().isEmpty()) { mostrarMensaje("No tienes monstruos."); return; }
        Monstruo m = elegirMonstruoDialog(actual.getCampo(), "Cambiar modo ATK/DEF");
        if (m != null) ctrl.onCambiarModo(actual.getCampo().indexOf(m));
    }

    private Monstruo elegirMonstruoDialog(List<Monstruo> lista, String titulo) {
        String[] ops = lista.stream()
            .map(m -> m.getNombre() + "  " + (m.isEnAtaque() ? "[ATK " : "[DEF ") + m.getAtk() + "/" + m.getDef() + "]  Nv." + m.getNivel())
            .toArray(String[]::new);
        int idx = JOptionPane.showOptionDialog(this, titulo, "Selección",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, ops, null);
        return idx >= 0 ? lista.get(idx) : null;
    }

    private void mostrarStats() {
        JTextArea ta = new JTextArea(ctrl.obtenerEstadisticas());
        ta.setEditable(false);
        ta.setBackground(C_HEADER);
        ta.setForeground(C_GOLD);
        ta.setFont(F_LOG);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(440, 200));
        JOptionPane.showMessageDialog(this, sp, "Estadísticas", JOptionPane.PLAIN_MESSAGE);
    }

    //  OBSERVER: mensajes y ganador

    @Override
    public void mostrarMensaje(String msg) {
        if (areaLog != null) {
            areaLog.append(msg + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        }
    }

    @Override
    public void mostrarGanador(String nombre) {
        mostrarMensaje("★ ¡" + nombre + " GANA EL DUELO! ★");
        JOptionPane.showMessageDialog(this,
            "🏆  ¡" + nombre + " ha ganado el duelo!\n\nResultado guardado en resultados.txt",
            "FIN DEL DUELO", JOptionPane.INFORMATION_MESSAGE);
        if (JOptionPane.showConfirmDialog(this, "¿Ver estadísticas?", "Stats",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) mostrarStats();
    }

    @Override
    public int pedirEleccion(String titulo, String[] opciones) {
        return JOptionPane.showOptionDialog(this, titulo, "Selección",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, opciones, null);
    }
}
