package view;

import Model.GameFacade;
import controller.UIController;
import infra.UiState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Janela principal: monta a UI, inicializa GameFacade e conecta o UIController. */
public class MainFrame extends JFrame {
    public MainFrame() { this(null); }

    public MainFrame(Model.GameStateSnapshot snapCarregado) {
        super("Banco Imobiliário — Iteração 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);

        PlayerSetupDialog dlg = null;
        if (snapCarregado == null) {
            dlg = new PlayerSetupDialog(this);
            dlg.setVisible(true);
            if (!dlg.isConfirmed()) { dispose(); return; }
            snapCarregado = dlg.getSnapshotCarregado();
        }

        GameFacade game;
        UiState ui;
        if (snapCarregado != null) {
            String[] nomes = snapCarregado.players().stream().map(p -> p.nome()).toArray(String[]::new);
            Color[] cores  = snapCarregado.players().stream().map(p -> p.cor()).toArray(Color[]::new);
            int[] ordem    = snapCarregado.ordem();
            ui = new UiState(nomes.length, cores, nomes, ordem);
            game = GameFacade.initFromSnapshot(snapCarregado);
        } else {
            int     n      = dlg.getNumJogadores();
            Color[] cores  = dlg.getCoresEscolhidas();
            String[] nomes = dlg.getNomesEscolhidos();
            int[]   ordem  = dlg.getOrdemSorteada();
            ui = new UiState(n, cores, nomes, ordem);
            game = GameFacade.init(nomes, ordem);
        }

        PropertyPanel property = new PropertyPanel();
        BoardPanel board = new BoardPanel(ui);
        PlayerHudPanel hud = new PlayerHudPanel();
        DicePanel dice = new DicePanel();

        JPanel root = new JPanel(new BorderLayout());
        root.add(board,    BorderLayout.CENTER);
        root.add(property, BorderLayout.EAST);
        root.add(hud,      BorderLayout.SOUTH);
        root.add(dice,     BorderLayout.NORTH);
        setContentPane(root);

        UIController controller = new UIController(board, dice, ui, property, hud, game);

        // Sincroniza UI com o estado atual do jogo (posições e jogador da vez)
        for (int i = 0; i < game.getNumeroJogadores(); i++) {
            ui.setPos(i, game.getPosicao(i));
        }
        ui.setJogadorDaVez(game.getIndiceJogadorDaVez());
        board.repaint();

        // Ao fechar a janela, encerra a partida (apurando vencedor) antes de sair
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                controller.encerrarPartida();
            }
        });

        pack();
        Dimension max = new Dimension(1280, 800);
        Dimension cur = getSize();
        setSize(Math.min(cur.width,  max.width), Math.min(cur.height, max.height));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new MainFrame().setVisible(true);
        });
    }
}
