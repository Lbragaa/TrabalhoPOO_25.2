package controller;

import Model.GameFacade;
import Model.GameObserver;
import infra.UiState;
import view.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador da UI (MVC).
 * Responsável por conectar eventos da interface ao GameFacade (Façade)
 * e assinar eventos do jogo (Observer) para refletir mudanças na UI.
 * Importante: conversa APENAS com o GameFacade (não acessa entidades do domínio).
 */
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

        hud.viewPropsButton().addActionListener(e -> showOwnedPropertiesDialog());
    }

    private void showOwnedPropertiesDialog() {
        int indiceJogador = game.getIndiceJogadorDaVez();
        int celulaAtual = game.getPosicao(indiceJogador);

        List<String> nomes = game.getPropriedadesDoJogador(indiceJogador);
        List<Integer> celulas = game.getCelulasPropriedadesDoJogador(indiceJogador);

        if (nomes.isEmpty()) {
            JOptionPane.showMessageDialog(board, "Este jogador não possui propriedades.",
                    "Propriedades", JOptionPane.INFORMATION_MESSAGE);
            restoreCurrentCellCard(indiceJogador, celulaAtual, /*preverSorteReves=*/false);
            return;
        }

        // Agora mostramos casas e hotéis.
        List<String> exibicao = new ArrayList<>(nomes.size());
        for (int i = 0; i < nomes.size(); i++) {
            int cel = celulas.get(i);
            int casas = game.getNumeroCasasNaPosicao(cel);   // 0 se não for Terreno
            int hoteis = game.getNumeroHoteisNaPosicao(cel); // 0 ou 1
            exibicao.add(nomes.get(i) + " (casas: " + casas + ", hoteis: " + hoteis + ")");
        }

        JList<String> list = new JList<>(exibicao.toArray(new String[0]));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(360, 220));

        list.addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                int sel = list.getSelectedIndex();
                if (sel >= 0 && sel < celulas.size()) {
                    int cel = celulas.get(sel);
                    Integer indiceDono = game.getIndiceDonoDaPosicao(cel);
                    Color corDono = (indiceDono != null ? ui.getCor(indiceDono) : null);
                    String titulo  = "Propriedade";
                    String detalhe = nomes.get(sel) + " — \nProprietário: " +
                            (indiceDono != null ? ui.getNome(indiceDono) : "sem dono");
                    property.showForCell(cel, /*isChance=*/false, corDono, titulo, detalhe);
                    board.repaint();
                }
            }
        });

        JOptionPane.showMessageDialog(board, sp,
                "Propriedades de " + ui.getNome(indiceJogador),
                JOptionPane.PLAIN_MESSAGE);

        restoreCurrentCellCard(indiceJogador, celulaAtual, /*preverSorteReves=*/false);
    }

    private void restoreCurrentCellCard(int indiceJogador, int celula, boolean preverSorteReves) {
        boolean eSorteReves = CardResolver.isChanceCell(celula);
        boolean mostrarChance = (preverSorteReves && eSorteReves);

        Integer indiceDono = (!mostrarChance ? game.getIndiceDonoDaPosicao(celula) : null);
        Color corDono = (indiceDono != null ? ui.getCor(indiceDono) : null);

        String titulo  = mostrarChance ? "Sorte/Revés" : ("Casa " + celula);
        String donoTxt = (!mostrarChance ? " \nProprietário: " + (indiceDono != null ? ui.getNome(indiceDono) : "sem dono") : "");

        String detalhe = mostrarChance
                ? "Casa de Sorte/Revés — pré-visualização (não sorteamos carta agora)."
                : ui.getNome(indiceJogador) + " está na casa " + celula + "." + donoTxt;

        property.showForCell(celula, mostrarChance, corDono, titulo, detalhe);
        board.repaint();
    }

    // ----------------- Callbacks do Observer -----------------

    @Override public void onDice(int d1, int d2) {
        dice.setForced(d1, d2);
        dice.setFaces(d1, d2);
    }

    @Override public void onMoved(int indiceJogador, int celulaOrigem, int celulaDestino) {
        ui.setPos(indiceJogador, celulaDestino);

        boolean eSorteReves = CardResolver.isChanceCell(celulaDestino);
        restoreCurrentCellCard(indiceJogador, celulaDestino, /*preverSorteReves=*/!eSorteReves);

        if (game.jogadorEstaPreso(indiceJogador)) {
            int celulaPrisao = 10;
            Integer donoPrisao = game.getIndiceDonoDaPosicao(celulaPrisao);
            Color corDonoPrisao = (donoPrisao != null ? ui.getCor(donoPrisao) : null);
            property.showForCell(
                    celulaPrisao, false, corDonoPrisao,
                    "Prisão",
                    ui.getNome(indiceJogador) + " foi preso e enviado para a casa 10."
            );
        }

        if (game.posicaoTemPropriedade(celulaDestino)) {
            if (game.propriedadeDisponivel(celulaDestino)) {
                String nomeProp = game.getNomePropriedade(celulaDestino);
                int preco = game.getPrecoPropriedade(celulaDestino);
                int opt = JOptionPane.showConfirmDialog(board,
                        ui.getNome(indiceJogador) + " caiu em \"" + nomeProp + "\" (R$ " + preco + "). Deseja comprar?",
                        "Comprar propriedade", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    game.comprarPropriedadeAtual(indiceJogador);
                }
            } else if (game.jogadorEhDonoDaPosicao(indiceJogador, celulaDestino) && game.podeConstruirAqui(indiceJogador)) {

                boolean construirHotel = game.proximaConstrucaoEhHotelAqui(indiceJogador);
                int valor = construirHotel
                        ? game.getValorHotelAqui(indiceJogador)
                        : game.getValorCasaAqui(indiceJogador);

                String tipo = construirHotel ? "hotel" : "casa";
                String titulo = construirHotel ? "Construir hotel" : "Construir casa";

                int opt = JOptionPane.showConfirmDialog(board,
                        "Construir " + tipo + " por R$ " + valor + " nesta propriedade?",
                        titulo, JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    game.construirCasaNoLocal(indiceJogador);
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

    @Override public void onBalanceChanged(int indiceJogador, int novoSaldo) {
        int atual = game.getIndiceJogadorDaVez();
        refreshHud(atual);
        board.repaint();
    }

    @Override public void onPropertyBought(int indiceJogador, int celula) {
        Integer indiceDono = game.getIndiceDonoDaPosicao(celula);
        Color corDono = (indiceDono != null ? ui.getCor(indiceDono) : null);
        String titulo  = "Propriedade adquirida";
        String detalhe = ui.getNome(indiceJogador) + " comprou a casa " + celula + ".";
        property.showForCell(celula, false, corDono, titulo, detalhe);

        int atual = game.getIndiceJogadorDaVez();
        refreshHud(atual);
        board.repaint();
    }

    @Override public void onHouseBuilt(int indiceJogador, int celula, int numeroCasas) {
        Integer indiceDono = game.getIndiceDonoDaPosicao(celula);
        Color corDono = (indiceDono != null ? ui.getCor(indiceDono) : null);

        int hoteis = game.getNumeroHoteisNaPosicao(celula);
        String titulo  = "Construção";
        String detalhe;

        if (hoteis > 0) {
            detalhe = ui.getNome(indiceJogador) + " construiu um hotel na casa " + celula + ".";
        } else {
            detalhe = ui.getNome(indiceJogador) + " construiu casa (#" + numeroCasas + ") na casa " + celula + ".";
        }

        property.showForCell(celula, false, corDono, titulo, detalhe);

        int atual = game.getIndiceJogadorDaVez();
        refreshHud(atual);
        board.repaint();
    }

    @Override public void onJailStatus(int indiceJogador, boolean preso) {
        if (preso) {
            int celulaPrisao = 10;
            Integer donoPrisao = game.getIndiceDonoDaPosicao(celulaPrisao);
            Color corDonoPrisao = (donoPrisao != null ? ui.getCor(donoPrisao) : null);
            property.showForCell(
                    celulaPrisao, false, corDonoPrisao,
                    "Prisão",
                    ui.getNome(indiceJogador) + " foi preso e enviado para a casa 10."
            );
            ui.setPos(indiceJogador, 10);
        } else {
            int celula = game.getPosicao(indiceJogador);
            restoreCurrentCellCard(indiceJogador, celula, /*preverSorteReves=*/true);
        }
        int atual = game.getIndiceJogadorDaVez();
        refreshHud(atual);
        board.repaint();
    }

    @Override public void onRentPaid(int indicePagador, int indiceDono, int celula, int valor) {
        Color corDono = ui.getCor(indiceDono);
        String titulo = "Aluguel pago";
        String detalhe = ui.getNome(indicePagador) + " pagou R$ " + valor +
                " para " + ui.getNome(indiceDono) + " na casa " + celula + ".";
        property.showForCell(celula, false, corDono, titulo, detalhe);
        board.repaint();
    }

    @Override public void onBankruptcy(int indiceJogador) {
        JOptionPane.showMessageDialog(board,
                ui.getNome(indiceJogador) + " entrou em falência.",
                "Falência", JOptionPane.INFORMATION_MESSAGE);
        int atual = game.getIndiceJogadorDaVez();
        refreshHud(atual);
        board.repaint();
    }

    @Override
    public void onSpecialCell(int indiceJogador, int celula, int valor, String descricao) {
        String titulo = (valor >= 0) ? "Lucros/Dividendos" : "Imposto de Renda";
        Color corDono = null; // casas especiais não têm dono
        property.showForCell(celula, false, corDono, titulo,
                ui.getNome(indiceJogador) + ": " + descricao);
        refreshHud(game.getIndiceJogadorDaVez());
        board.repaint();
    }

    @Override
    public void onReleaseCardUsed(int indiceJogador) {
        int celulaPrisao = 10;
        property.showForCell(
                celulaPrisao, false, null,
                "Carta de liberação usada",
                ui.getNome(indiceJogador) + " usou a carta e saiu da prisão automaticamente."
        );
        refreshHud(game.getIndiceJogadorDaVez());
        board.repaint();
    }

    @Override
    public void onChanceCard(int indiceJogador, int celula, int numero, String tipo, int valor) {
        String detalhe;
        switch (tipo) {
            case "RECEBER"         -> detalhe = "Receba R$ " + valor + " do banco.";
            case "PAGAR"           -> detalhe = "Pague R$ " + valor + " ao banco.";
            case "RECEBER_DE_CADA" -> detalhe = "Receba R$ " + valor + " de cada jogador.";
            case "SAIDA_LIVRE"     -> detalhe = "Guarde esta carta de liberação da prisão.";
            case "VAI_PARA_PRISAO" -> detalhe = "Vá diretamente para a prisão (casa 10).";
            default                -> detalhe = "Carta de Sorte/Revés.";
        }
        property.showChanceCard(numero, "Sorte/Revés", detalhe);
        refreshHud(game.getIndiceJogadorDaVez());
        board.repaint();
    }

    private void refreshHud(int indiceJogador) {
        if (hud == null) return;

        int saldo = game.getSaldo(indiceJogador);
        List<String> props = game.getPropriedadesDoJogador(indiceJogador);
        hud.updateHud(ui.getNome(indiceJogador), ui.getCor(indiceJogador), saldo, props);

        StringBuilder sb = new StringBuilder("Saldos: ");
        int n = game.getNumeroJogadores();

        boolean first = true;
        for (int i = 0; i < n; i++) {
            if (!first) sb.append("  |  ");
            sb.append(ui.getNome(i))
              .append(": R$ ")
              .append(game.getSaldo(i));
            first = false;
        }
        hud.updateAllBalances(sb.toString());
    }
}
