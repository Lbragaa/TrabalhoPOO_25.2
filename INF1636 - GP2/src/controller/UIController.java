package controller;

import Model.GameFacade;
import Model.GameObserver;
import infra.UiState;
import infra.ImageStore;
import view.*;
import view.MainFrame;

import javax.swing.*;
import java.awt.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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
    private GameFacade game;
    /** Marca se a prisão foi disparada por carta de Sorte/Revés para não sobrescrever a carta na lateral. */
    private boolean jailTriggeredByChanceCard = false;
    private boolean saveEnabled = true;

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
            jogarTurno(v1, v2);
        });
        dice.randomButton().addActionListener(e -> {
            List<Integer> dados = game.sortearDados();
            jogarTurno(dados.get(0), dados.get(1));
        });

        hud.viewPropsButton().addActionListener(e -> showOwnedPropertiesDialog());
        hud.endGameButton().addActionListener(e -> encerrarPartida());
        hud.saveButton().addActionListener(e -> salvarPartida());
        hud.loadButton().addActionListener(e -> carregarPartida());
        setSaveEnabled(true);
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
        boolean eSorteReves = game.isChanceCell(celula);
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
        setSaveEnabled(false); // jogada começou
    }

    @Override public void onMoved(int indiceJogador, int celulaOrigem, int celulaDestino) {
        ui.setPos(indiceJogador, celulaDestino);

        boolean eSorteReves = game.isChanceCell(celulaDestino);
        restoreCurrentCellCard(indiceJogador, celulaDestino, /*preverSorteReves=*/!eSorteReves);

        if (game.jogadorEstaPreso(indiceJogador)) {
            int celulaPrisao = game.getPosicaoPrisao();
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
                if (game.getSaldo(indiceJogador) < preco) {
                    // Não tem saldo: não mostra opção de compra
                    board.repaint();
                    return;
                }
                int opt = JOptionPane.showConfirmDialog(board,
                        ui.getNome(indiceJogador) + " caiu em \"" + nomeProp + "\" (R$ " + preco + "). Deseja comprar?",
                        "Comprar propriedade", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    game.comprarPropriedadeAtual(indiceJogador);
                }
            } else if (game.jogadorEhDonoDaPosicao(indiceJogador, celulaDestino) && game.podeConstruirAqui(indiceJogador)) {

                boolean podeCasa = game.podeConstruirCasaAqui(indiceJogador);
                boolean podeHotel = game.podeConstruirHotelAqui(indiceJogador);

                int valorCasa = game.getValorCasaAqui(indiceJogador);
                int valorHotel = game.getValorHotelAqui(indiceJogador);

                if (podeCasa && podeHotel) {
                    Object[] options = {
                            "Casa (R$ " + valorCasa + ")",
                            "Hotel (R$ " + valorHotel + ")",
                            "Cancelar"
                    };
                    int choice = JOptionPane.showOptionDialog(
                            board,
                            "Escolha o que construir nesta propriedade:",
                            "Construir",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]
                    );
                    if (choice == 0 && game.getSaldo(indiceJogador) >= valorCasa) {
                        game.construirCasaNoLocal(indiceJogador);
                    } else if (choice == 1 && game.getSaldo(indiceJogador) >= valorHotel) {
                        game.construirHotelNoLocal(indiceJogador);
                    }
                } else if (podeHotel) {
                    if (game.getSaldo(indiceJogador) >= valorHotel) {
                        int opt = JOptionPane.showConfirmDialog(
                                board,
                                "Construir hotel por R$ " + valorHotel + " nesta propriedade?",
                                "Construir hotel",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (opt == JOptionPane.YES_OPTION) {
                            game.construirHotelNoLocal(indiceJogador);
                        }
                    }
                } else if (podeCasa) {
                    if (game.getSaldo(indiceJogador) >= valorCasa) {
                        int opt = JOptionPane.showConfirmDialog(
                                board,
                                "Construir casa por R$ " + valorCasa + " nesta propriedade?",
                                "Construir casa",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (opt == JOptionPane.YES_OPTION) {
                            game.construirCasaNoLocal(indiceJogador);
                        }
                    }
                }
            }
        }

        board.repaint();
    }

    @Override public void onTurnChanged(int currentPlayerIndex) {
        ui.setJogadorDaVez(currentPlayerIndex);
        refreshHud(currentPlayerIndex);
        setSaveEnabled(true); // novo turno: pode salvar antes de agir
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
            if (!jailTriggeredByChanceCard) {
                int celulaPrisao = game.getPosicaoPrisao();
                Integer donoPrisao = game.getIndiceDonoDaPosicao(celulaPrisao);
                Color corDonoPrisao = (donoPrisao != null ? ui.getCor(donoPrisao) : null);
                property.showForCell(
                        celulaPrisao, false, corDonoPrisao,
                        "Prisão",
                        ui.getNome(indiceJogador) + " foi preso e enviado para a casa 10."
                );
            }
            JOptionPane.showMessageDialog(board,
                    ui.getNome(indiceJogador) + " está preso!",
                    "VOCÊ ESTÁ PRESO!", JOptionPane.INFORMATION_MESSAGE);
            ui.setPos(indiceJogador, game.getPosicaoPrisao());
        } else {
            int celula = game.getPosicao(indiceJogador);
            restoreCurrentCellCard(indiceJogador, celula, /*preverSorteReves=*/true);
        }
        int atual = game.getIndiceJogadorDaVez();
        refreshHud(atual);
        board.repaint();
        jailTriggeredByChanceCard = false; // reseta para próximos eventos
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
        ui.setAtivo(indiceJogador, false); // some da UI
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
        // Popup com a imagem da carta antes de liberar
        var img = ImageStore.loadCached("/sorteReves/chance9.png"); // 9 = SAIDA_LIVRE
        Icon icon = (img != null)
                ? new ImageIcon(img.getScaledInstance(
                        Math.min(320, img.getWidth()),
                        Math.min(420, img.getHeight()),
                        Image.SCALE_SMOOTH))
                : null;
        JOptionPane.showMessageDialog(
                board,
                "SUA CARTA DE LIBERAÇÃO FOI USADA E VOCÊ ESTÁ LIVRE",
                "Carta de liberação",
                JOptionPane.INFORMATION_MESSAGE,
                icon
        );

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
        if ("VAI_PARA_PRISAO".equals(tipo)) {
            jailTriggeredByChanceCard = true;
        }
        refreshHud(game.getIndiceJogadorDaVez());
        board.repaint();
    }

    private void setSaveEnabled(boolean enabled) {
        saveEnabled = enabled;
        hud.saveButton().setEnabled(enabled);
    }

    private void salvarPartida() {
        if (!saveEnabled) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvar partida");
        chooser.setFileFilter(new FileNameExtensionFilter("Texto (.txt)", "txt"));
        int opt = chooser.showSaveDialog(board);
        if (opt == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new java.io.File(file.getAbsolutePath() + ".txt");
            }
            try {
                game.salvarParaArquivo(file, coletarCores());
                JOptionPane.showMessageDialog(board, "Partida salva em:\n" + file.getAbsolutePath(),
                        "Salvar", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(board, "Erro ao salvar: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarPartida() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Carregar partida");
        chooser.setFileFilter(new FileNameExtensionFilter("Texto (.txt)", "txt"));
        int opt = chooser.showOpenDialog(board);
        if (opt == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            try {
                var snap = GameFacade.carregarSnapshot(file);
                // abre nova janela com o snapshot carregado
                java.awt.Window win = SwingUtilities.getWindowAncestor(board);
                new MainFrame(snap).setVisible(true);
                if (win != null) win.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(board, "Erro ao carregar: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Color[] coletarCores() {
        int n = ui.getNumJogadores();
        Color[] cores = new Color[n];
        for (int i = 0; i < n; i++) cores[i] = ui.getCor(i);
        return cores;
    }

    /** Orquestra a sequência completa de uma jogada (Controller). */
    private void jogarTurno(int d1, int d2) {
        setSaveEnabled(false);
        int idx = game.getIndiceJogadorDaVez();

        game.notificarRolagem(d1, d2);

        // Prisão: tenta sair com dupla
        if (game.jogadorEstaPreso(idx)) {
            boolean liberado = game.tentarLiberarComDupla(idx, d1, d2);
            game.notificarEstado();
            if (!liberado) { game.avancarTurnoENotificar(); return; }
        }

        game.moverJogadorComDados(idx, d1, d2);
        game.aplicarCasasEspeciais(idx);
        game.cobrarAluguelSeNecessario(idx);
        game.resolverChanceSeNecessario(idx);

        game.notificarEstado();
        game.usarCartaLiberacaoAutomatica(idx);
        game.notificarEstado();
        game.avancarTurnoENotificar();
    }

    @Override
    public void onGameEnded(int winnerIndex, int[] capitaisPorJogador) {
        // Ordena por capital desc para exibição
        List<Integer> ordem = new ArrayList<>();
        for (int i = 0; i < capitaisPorJogador.length; i++) ordem.add(i);
        ordem.sort((a, b) -> Integer.compare(capitaisPorJogador[b], capitaisPorJogador[a]));

        StringBuilder sb = new StringBuilder();
        int max = Integer.MIN_VALUE;
        for (int cap : capitaisPorJogador) if (cap > max) max = cap;

        List<String> vencedores = new ArrayList<>();
        for (int idx : ordem) {
            sb.append(ui.getNome(idx)).append(": R$ ").append(capitaisPorJogador[idx]).append("\n");
            if (capitaisPorJogador[idx] == max) vencedores.add(ui.getNome(idx));
        }

        String msg = "Capitais apurados:\n\n" + sb +
                "\nVencedor(es): " + String.join(", ", vencedores);
        JOptionPane.showMessageDialog(board, msg, "Partida encerrada", JOptionPane.INFORMATION_MESSAGE);
        // Opcional: fechar a janela principal
        SwingUtilities.getWindowAncestor(board).dispose();
    }

    /** Encerrar partida via botão/menu ou fechamento da janela. */
    public void encerrarPartida() {
        game.encerrarPartida();
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
            if (!ui.isAtivo(i)) continue;
            if (!first) sb.append("  |  ");
            sb.append(ui.getNome(i))
              .append(": R$ ")
              .append(game.getSaldo(i));
            first = false;
        }
        hud.updateAllBalances(sb.toString());
    }
}
