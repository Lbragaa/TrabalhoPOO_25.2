package view;

import javax.swing.*;
import java.awt.*;

/** Janela inicial (Iteração 2): define apenas a quantidade de jogadores (3..6).
 *  OBS: o fluxo principal usa PlayerSetupDialog; este dialog é opcional/alternativo.
 */

public class StartDialog extends JDialog {
    private final JSpinner spinner;
    private boolean confirmed = false;

    public StartDialog(Window owner) {
        super(owner, "Nova Partida — Jogadores", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        root.add(new JLabel("Número de jogadores (3 a 6):"), c);

        spinner = new JSpinner(new SpinnerNumberModel(3, 3, 6, 1));
        c.gridx = 1; root.add(spinner, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancelar");
        buttons.add(ok); buttons.add(cancel);

        c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.anchor = GridBagConstraints.EAST;
        root.add(buttons, c);

        ok.addActionListener(e -> { confirmed = true; dispose(); });
        cancel.addActionListener(e -> { dispose(); });

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    public boolean isConfirmed() { return confirmed; }
    public int getNumJogadores() { return (Integer) spinner.getValue(); }
}
