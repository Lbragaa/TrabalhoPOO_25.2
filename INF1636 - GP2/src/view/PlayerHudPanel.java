package view;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PlayerHudPanel extends JPanel {
    private final JLabel lblPlayer = new JLabel("Jogador: —");
    private final JLabel lblBalance = new JLabel("Saldo: R$ —");
    private final JComboBox<String> comboProps = new JComboBox<>();

    public PlayerHudPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,8,4,8);
        c.gridy = 0; c.anchor = GridBagConstraints.WEST;

        lblPlayer.setFont(lblPlayer.getFont().deriveFont(Font.BOLD, 14f));
        comboProps.setPrototypeDisplayValue("Propriedade bem comprida…");
        comboProps.setFocusable(false);

        c.gridx = 0; add(lblPlayer, c);
        c.gridx = 1; add(lblBalance, c);
        c.gridx = 2; add(new JLabel("Propriedades:"), c);
        c.gridx = 3; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0;
        add(comboProps, c);
    }

    /** Atualiza nome/cor/saldo e a lista de propriedades. */
    public void updateHud(String nome, Color cor, int saldo, List<String> props) {
        lblPlayer.setText("Jogador: " + (nome != null ? nome : "—"));
        lblPlayer.setForeground(cor != null ? cor : Color.BLACK);
        lblBalance.setText("Saldo: R$ " + saldo);
        comboProps.removeAllItems();
        if (props != null && !props.isEmpty()) for (String p : props) comboProps.addItem(p);
        else comboProps.addItem("—");
    }
}
