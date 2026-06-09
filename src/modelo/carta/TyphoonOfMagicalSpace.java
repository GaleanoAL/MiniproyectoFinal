package modelo.carta;

import modelo.juego.Jugador;

/** Destruye el primer monstruo del campo enemigo. */
public class TyphoonOfMagicalSpace extends Magica {
    public TyphoonOfMagicalSpace() { super("Typhoon Of Magical Space"); }
    @Override
    public void activar(Jugador jugador, Jugador oponente) {
        if (!oponente.getCampo().isEmpty())
            oponente.getCampo().remove(0);
    }
}
