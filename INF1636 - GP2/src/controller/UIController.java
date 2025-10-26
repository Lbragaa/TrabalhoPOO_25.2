package controller;

import Model.GameFacade;
import Model.GameObserver;
import infra.UiState;
import view.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Controlador da UI (MVC).
 * <p>
 * Responsável por:
 * - Ligar eventos da interface (botões) às ações do modelo via {@link GameFacade} (Padrão Façade).
 * - Assinar eventos do jogo implementando {@link GameObserver} (Padrão Observer).
 * - Atualizar HUD, painel do tabuleiro e painel de propriedades/cartas.
 * <p>
 * Importante: conversa APENAS com o {@link GameFacade} (não acessa diretamente entidades do domínio).
 */
public class UIController implements GameObserver {
    private final BoardPanel board;          // Desenha o tabuleiro e peões (Java2D)
    private final DicePanel dice;            // Controles/rostos dos dados
    private final UiState ui;                // Estado de exibição (nomes, cores, posições)
    private final PropertyPanel property;    // Carta de propriedade / sorte-revés
    private final PlayerHudPanel hud;        // HUD do jogador da vez
    private final GameFacade game;           // Façade para o modelo

    /**
     * Constrói o controlador, registra-se como observador do jogo e inicializa o HUD.
     */
    public UIController(BoardPanel board, DicePanel dice, UiState ui,
                        PropertyPanel property, PlayerHudPanel hud,
                        GameFacade game) {
        this.board = board; this.dice = dice; this.ui = ui;
        this.property = property; this.hud = hud; this.game = game;

        this.game.addObserver(this);
        refreshHud(game.getIndiceJogadorDaVez());
        wire();
    }

    /**
     * Conecta ações de UI às operações do jogo:
     * - Rolar dados forçados (teste)
     * - Rolar dados aleatórios
     * - Ver propriedades do jogador da vez
     */
    private void wire() {
        dice.rollButton().addActionListener(e -> {
            int v1 = dice.forcedD1(), v2 = dice.forcedD2();
            game.rolarDadosForcado(v1, v2);
        });
        dice.randomButton().addActionListener(e -> game.rolarDadosAleatorio());

        hud.viewPropsButton().addActionListener(e -> showOwnedPropertiesDialog());
    }

    /**
     * Exibe lista de propriedades do jogador da vez e, ao selecionar, mostra a carta da propriedade.
     * Ao fechar, restaura a carta da célula atual do jogador.
     */
    private void showOwnedPropertiesDialog() {
        int playerIndex = game.getIndiceJogadorDaVez();
        int currentCell = game.getPosicao(playerIndex);

        List<String> names = game.getPropriedadesDoJogador(playerIndex);
        List<Integer> cells = game.getCelulasPropriedadesDoJogador(playerIndex);

        if (names.isEmpty()) {
            // Nota: para evitar diálogos informativos simples, poderíamos exibir um aviso no HUD.
            JOptionPane.showMessageDialog(board, "Este jogador não possui propriedades.",
                    "Propriedades", JOptionPane.INFORMATION_MESSAGE);
            restoreCurrentCellCard(playerIndex, currentCell);
            return;
        }

        JList<String> list = new JList<>(names.toArray(new String[0]));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(Math.min(10, names.size()));
        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(320, 200));

        list.addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                int sel = list.getSelectedIndex();
                if (sel >= 0 && sel < cells.size()) {
                    int cell = cells.get(sel);
                    Integer ownerIdx = game.getIndiceDonoDaPosicao(cell);
                    Color ownerColor = (ownerIdx != null ? ui.getCor(ownerIdx) : null);
                    String title  = "Propriedade";
                    String detail = names.get(sel) + " — \nProprietário: " +
                            (ownerIdx != null ? ui.getNome(ownerIdx) : "sem dono");
                    property.showForCell(cell, false, ownerColor, title, detail);
                    board.repaint();
                }
            }
        });

        JOptionPane.showMessageDialog(board, sp,
                "Propriedades de " + ui.getNome(playerIndex),
                JOptionPane.PLAIN_MESSAGE);

        restoreCurrentCellCard(playerIndex, currentCell);
    }

    /**
     * Restaura a carta da célula atual (propriedade ou sorte/revés) no painel de propriedades.
     */
    private void restoreCurrentCellCard(int playerIndex, int cell) {
        boolean isChance = CardResolver.isChanceCell(cell);
        Integer ownerIdx = (!isChance ? game.getIndiceDonoDaPosicao(cell) : null);
        Color ownerColor = (ownerIdx != null ? ui.getCor(ownerIdx) : null);
        String title  = isChance ? "Sorte/Revés" : ("Casa " + cell);
        String ownerTxt = (!isChance ? " \nProprietário: " + (ownerIdx != null ? ui.getNome(ownerIdx) : "sem dono") : "");
        String detail = isChance
                ? "Carta sorteada aleatoriamente."
                : ui.getNome(playerIndex) + " está na casa " + cell + "." + ownerTxt;
        property.showForCell(cell, isChance, ownerColor, title, detail);
        board.repaint();
    }

    // ----------------- Callbacks do Observer -----------------

    /** Atualiza faces e valores forçados dos dados quando o modelo notifica. */
    @Override public void onDice(int d1, int d2) {
        dice.setForced(d1, d2);
        dice.setFaces(d1, d2);
    }

    /**
     * Ao mover o peão:
     * - atualiza posição exibida,
     * - mostra carta da célula,
     * - trata compra/ construção quando aplicável,
     * - exibe informação de prisão sem diálogo (no painel de propriedades).
     */
    @Override public void onMoved(int playerIndex, int fromCell, int toCell) {
        ui.setPos(playerIndex, toCell);

        boolean isChance = CardResolver.isChanceCell(toCell);
        Integer ownerIdx = (!isChance ? game.getIndiceDonoDaPosicao(toCell) : null);
        Color ownerColor = (ownerIdx != null ? ui.getCor(ownerIdx) : null);
        String title  = isChance ? "Sorte/Revés" : ("Casa " + toCell);
        String ownerTxt = (!isChance ? " \nProprietário: " + (ownerIdx != null ? ui.getNome(ownerIdx) : "sem dono") : "");
        String detail = isChance
                ? "Carta sorteada aleatoriamente."
                : ui.getNome(playerIndex) + " parou na casa " + toCell + "." + ownerTxt;
        property.showForCell(toCell, isChance, ownerColor, title, detail);

        // ---- Sem diálogo: mostrar aviso de prisão no painel de propriedades ----
        if (game.jogadorEstaPreso(playerIndex)) {
            int cellPrisao = 10;
            Integer donoPrisao = game.getIndiceDonoDaPosicao(cellPrisao);
            Color corDonoPrisao = (donoPrisao != null ? ui.getCor(donoPrisao) : null);
            property.showForCell(
                    cellPrisao, /*isChance=*/false, corDonoPrisao,
                    "Prisão",
                    ui.getNome(playerIndex) + " foi preso e enviado para a casa 10."
            );
            board.repaint();
        }

        // Decisões de propriedade (comprar/construir)
        if (game.posicaoTemPropriedade(toCell)) {
            if (game.propriedadeDisponivel(toCell)) {
                String nomeProp = game.getNomePropriedade(toCell);
                int preco = game.getPrecoPropriedade(toCell);
                int opt = JOptionPane.showConfirmDialog(board,
                        ui.getNome(playerIndex) + " caiu em \"" + nomeProp + "\" (R$ " + preco + "). Deseja comprar?",
                        "Comprar propriedade", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    game.comprarPropriedadeAtual(playerIndex);
                    refreshHud(playerIndex);
                    board.repaint();
                }
            } else if (game.jogadorEhDonoDaPosicao(playerIndex, toCell) && game.podeConstruirAqui(playerIndex)) {
                int valorCasa = game.getValorCasaAqui(playerIndex);
                int opt = JOptionPane.showConfirmDialog(board,
                        "Construir casa por R$ " + valorCasa + " nesta propriedade?",
                        "Construir casa", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    game.construirCasaNoLocal(playerIndex);
                    refreshHud(playerIndex);
                    board.repaint();
                }
            }
        }

        board.repaint();
    }

    /** Ao trocar o turno, atualiza HUD e destaque do jogador da vez. */
    @Override public void onTurnChanged(int currentPlayerIndex) {
        ui.setJogadorDaVez(currentPlayerIndex);
        refreshHud(currentPlayerIndex);
        board.repaint();
    }

    // ----------------- HUD -----------------

    /**
     * Atualiza o HUD com nome, cor, saldo e lista de propriedades do jogador.
     */
    private void refreshHud(int playerIndex) {
        if (hud == null) return;
        int saldo = game.getSaldo(playerIndex);
        List<String> props = game.getPropriedadesDoJogador(playerIndex);
        hud.updateHud(ui.getNome(playerIndex), ui.getCor(playerIndex), saldo, props);
    }
}
