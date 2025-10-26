package Model;

/**
 * Observador de eventos do jogo para a UI.
 * <p>A UI implementa esta interface e recebe callbacks do backend.</p>
 */
public interface GameObserver {
    /** Notifica os valores dos dados lançados. */
    default void onDice(int d1, int d2) {}

    /** Notifica movimento de peão (índice do jogador, de, para). */
    default void onMoved(int playerIndex, int fromCell, int toCell) {}

    /** Notifica mudança de turno (índice do jogador da vez). */
    default void onTurnChanged(int currentPlayerIndex) {}
}
