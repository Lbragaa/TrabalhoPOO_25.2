package controller;

import Model.GameFacade;
import Model.GameObserver;
import infra.UiState;
import view.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Controller da UI que conversa APENAS com o GameFacade. */
public class UIController implements GameObserver {
    private final BoardPanel board;
    private final DicePanel dice;
    private final UiState ui;
    private final PropertyPanel property;
    private final PlayerHudPanel hud;
    private final GameFacade game;

    public UIController(BoardPanel board, DicePanel dice, UiState ui,
                        PropertyPanel property, PlayerHudPanel hud,
                        GameFacade game) {
        this.board = board; this.dice = dice; this.ui = ui;
        this.property = property; this.hud = hud; this.game = game;

        this.game.addObserver(this);
        refreshHud(game.getIndiceJogadorDaVez());
        wire();
    }

    private void wire() {
        dice.rollButton().addActionListener(e -> {
            int v1 = dice.forcedD1(), v2 = dice.forcedD2();
            game.rolarDadosForcado(v1, v2);
        });
        dice.randomButton().addActionListener(e -> game.rolarDadosAleatorio());

        // Ver propriedades do jogador da vez
        hud.viewPropsButton().addActionListener(e -> showOwnedPropertiesDialog());
    }

    private void showOwnedPropertiesDialog() {
        int playerIndex = game.getIndiceJogadorDaVez();
        int currentCell = game.getPosicao(playerIndex);

        List<String> names = game.getPropriedadesDoJogador(playerIndex);
        List<Integer> cells = game.getCelulasPropriedadesDoJogador(playerIndex);

        if (names.isEmpty()) {
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

        // Selecionar => mostrar carta da propriedade escolhida
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

        // Ao fechar o diálogo, restaurar carta da casa atual
        restoreCurrentCellCard(playerIndex, currentCell);
    }

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

    // ----------------- GameObserver -----------------
    @Override public void onDice(int d1, int d2) {
        dice.setForced(d1, d2);
        dice.setFaces(d1, d2);
    }

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

        if (game.jogadorEstaPreso(playerIndex)) {
            JOptionPane.showMessageDialog(board,
                    ui.getNome(playerIndex) + " foi preso e foi para a casa 10.",
                    "Prisão", JOptionPane.INFORMATION_MESSAGE);
        }

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

    @Override public void onTurnChanged(int currentPlayerIndex) {
        ui.setJogadorDaVez(currentPlayerIndex);
        refreshHud(currentPlayerIndex);
        board.repaint();
    }

    // ----------------- HUD -----------------
    private void refreshHud(int playerIndex) {
        if (hud == null) return;
        int saldo = game.getSaldo(playerIndex);
        List<String> props = game.getPropriedadesDoJogador(playerIndex);
        hud.updateHud(ui.getNome(playerIndex), ui.getCor(playerIndex), saldo, props);
    }
}
