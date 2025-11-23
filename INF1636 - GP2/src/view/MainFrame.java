package view;

import Model.GameFacade;
import controller.UIController;
import infra.UiState;

import javax.swing.*;
import java.awt.*;

/** Janela principal: monta a UI, inicializa GameFacade e conecta o UIController. */
public class MainFrame extends JFrame {
    public MainFrame() {
        super("Banco Imobiliário — Iteração 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);

        PlayerSetupDialog dlg = new PlayerSetupDialog(this);
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) { dispose(); return; }

        int     n      = dlg.getNumJogadores();
        Color[] cores  = dlg.getCoresEscolhidas();
        String[] nomes = dlg.getNomesEscolhidos();
        int[]   ordem  = dlg.getOrdemSorteada();

        UiState ui = new UiState(n, cores, nomes, ordem);

        PropertyPanel property = new PropertyPanel();
        BoardPanel board = new BoardPanel(ui);
        PlayerHudPanel hud = new PlayerHudPanel();
        DicePanel dice = new DicePanel();

        // Façade (Singleton)
        GameFacade game = GameFacade.init(nomes, ordem);

        JPanel root = new JPanel(new BorderLayout());
        root.add(board,    BorderLayout.CENTER);
        root.add(property, BorderLayout.EAST);
        root.add(hud,      BorderLayout.SOUTH);
        root.add(dice,     BorderLayout.NORTH);
        setContentPane(root);

        new UIController(board, dice, ui, property, hud, game);

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