package Model;

/** A UI implementa isso para ser notificada pelo backend. */
public interface GameObserver {
    default void onDice(int d1, int d2) {}
    default void onMoved(int playerIndex, int fromCell, int toCell) {}
    default void onTurnChanged(int currentPlayerIndex) {}
}
