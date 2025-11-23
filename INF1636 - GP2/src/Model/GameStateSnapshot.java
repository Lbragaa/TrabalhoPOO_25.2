package Model;

import java.util.List;

/** Snapshot imutável do estado completo do jogo para salvar/carregar. */
public record GameStateSnapshot(
        int bancoSaldo,
        int[] ordem,
        int ponteiro,
        List<PlayerData> players,
        List<PropertyData> propriedades,
        List<Carta> deck
) {
    public record PlayerData(String nome, int corIndex, int saldo, int posicao,
                             boolean preso, boolean falido, int cartasLiberacao) {}
    public record PropertyData(int posicao, int ownerIndex, int casas, int hotel) {}
}
