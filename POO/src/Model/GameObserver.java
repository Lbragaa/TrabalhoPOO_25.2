package Model;

/**
 * Observador de eventos do jogo para a UI.
 * A UI implementa esta interface e recebe callbacks do backend (Model/Façade).
 *
 * Requisito: a atualização da interface gráfica deve ocorrer
 * exclusivamente em resposta a estes callbacks (Observer).
 */
public interface GameObserver {
    /** Notifica os valores dos dados lançados. */
    default void onDice(int d1, int d2) {}

    /** Notifica movimento de peão (índice do jogador, de, para). */
    default void onMoved(int playerIndex, int fromCell, int toCell) {}

    /** Notifica mudança de turno (índice do jogador da vez). */
    default void onTurnChanged(int currentPlayerIndex) {}

    /** Notifica alteração de saldo do jogador. */
    default void onBalanceChanged(int playerIndex, int newBalance) {}

    /** Notifica que um jogador comprou a propriedade na célula informada. */
    default void onPropertyBought(int playerIndex, int cell) {}

    /** Notifica que um jogador construiu casa na célula; informa total de casas. */
    default void onHouseBuilt(int playerIndex, int cell, int numHouses) {}

    /** Notifica mudança no status de prisão do jogador (true = preso). */
    default void onJailStatus(int playerIndex, boolean preso) {}

    /** Notifica que um jogador entrou em falência. */
    default void onBankruptcy(int playerIndex) {}

    /** Notifica pagamento de aluguel: pagante -> dono, na célula, com valor. */
    default void onRentPaid(int payerIndex, int ownerIndex, int cell, int amount) {}

    /** Notifica que uma carta de Sorte/Revés foi puxada. */
    default void onChanceCard(int playerIndex, int cell, int cardNumber, String tipo, int valor) {}

     /** Notifica disparo de casa especial fixa (ex.: lucros/dividendos ou IR). */
    default void onSpecialCell(int playerIndex, int cell, int valor, String descricao) {}
}
