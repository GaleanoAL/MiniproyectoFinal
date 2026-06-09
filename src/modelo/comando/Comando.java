package modelo.comando;

/**
 * Patrón Command (RF3): cada acción del jugador es un objeto con execute() y undo().
 * El controlador apila los comandos ejecutados; el último puede deshacerse.
 */
public interface Comando {
    void execute();
    void undo();
    String descripcion();
}
