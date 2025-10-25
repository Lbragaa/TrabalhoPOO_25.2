package controller;

import Model.GameFacade;
import Model.GameObserver;
import infra.UiState;
import view.*;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
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

        // Observa o backend
        this.game.addObserver(this);

        // HUD inicial
        refreshHud(game.getIndiceJogadorDaVez());

        wire();
    }

    private void wire() {
        dice.rollButton().addActionListener(e -> {
            int v1 = dice.forcedD1(), v2 = dice.forcedD2();
            game.rolarDadosForcado(v1, v2);     // backend faz tudo e nos notifica
        });
        dice.randomButton().addActionListener(e -> game.rolarDadosAleatorio());
    }

    // ----------------- GameObserver (callbacks do backend) -----------------
    @Override public void onDice(int d1, int d2) {
        dice.setForced(d1, d2);
        dice.setFaces(d1, d2);
    }

    @Override public void onMoved(int playerIndex, int fromCell, int toCell) {
        // Sincroniza a posição local para o desenho
        ui.setPos(playerIndex, toCell);

        // Atualiza painel lateral (imagem/carta)
        boolean isChance = CardResolver.isChanceCell(toCell);
        Color ownerColor = (!isChance ? ui.getCor(playerIndex) : null);
        String title  = isChance ? "Sorte/Revés" : ("Casa " + toCell);
        String detail = isChance
                ? "Carta sorteada aleatoriamente."
                : ui.getNome(playerIndex) + " parou na casa " + toCell + ".";
        property.showForCell(toCell, isChance, ownerColor, title, detail);

        // Se o motor prendeu o jogador (caiu na 30), avisa visualmente
        if (game.jogadorEstaPreso(playerIndex)) {
            JOptionPane.showMessageDialog(board,
                    ui.getNome(playerIndex) + " foi preso e foi para a casa " +
                    // casa de visita/prisão (10)
                    "10.",
                    "Prisão", JOptionPane.INFORMATION_MESSAGE);
        }

        // Interações de compra/construção
        if (game.posicaoTemPropriedade(toCell)) {
            if (game.propriedadeDisponivel(toCell)) {
                String nomeProp = game.getNomePropriedade(toCell);
                int preco = game.getPrecoPropriedade(toCell);
                int opt = JOptionPane.showConfirmDialog(board,
                        ui.getNome(playerIndex) + " caiu em \"" + nomeProp + "\" (R$ " + preco + "). Deseja comprar?",
                        "Comprar propriedade",
                        JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    game.comprarPropriedadeAtual(playerIndex);
                    refreshHud(playerIndex);
                    board.repaint();
                }
            } else if (game.jogadorEhDonoDaPosicao(playerIndex, toCell) && game.podeConstruirAqui(playerIndex)) {
                int valorCasa = game.getValorCasaAqui(playerIndex);
                int opt = JOptionPane.showConfirmDialog(board,
                        "Construir casa por R$ " + valorCasa + " nesta propriedade?",
                        "Construir casa",
                        JOptionPane.YES_NO_OPTION);
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
        int saldo = game.getSaldo(playerIndex);              // agora vem do backend
        List<String> props = Collections.emptyList();        // (preencher na iteração 3)
        hud.updateHud(ui.getNome(playerIndex), ui.getCor(playerIndex), saldo, props);
    }
}
