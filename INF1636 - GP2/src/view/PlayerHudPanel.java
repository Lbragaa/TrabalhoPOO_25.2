package view;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** HUD do jogador: nome, saldo e acesso rápido às propriedades. */
public class PlayerHudPanel extends JPanel {
    private final JLabel lblPlayer     = new JLabel("Jogador: —");
    private final JLabel lblBalance    = new JLabel("Saldo: R$ —");
    private final JLabel lblAllBalances = new JLabel("Saldos: —"); // NOVO
    private final JButton btnVerProps  = new JButton("Ver propriedades");
    private final JButton btnEncerrar  = new JButton("Encerrar partida");

    private static final NumberFormat BRL_INT =
            NumberFormat.getIntegerInstance(new Locale("pt","BR"));

    public PlayerHudPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,8,4,8);
        c.anchor = GridBagConstraints.WEST;

        lblPlayer.setFont(lblPlayer.getFont().deriveFont(Font.BOLD, 14f));
        btnVerProps.setToolTipText("Abrir lista de propriedades do jogador da vez");

        // Linha 0: jogador da vez, saldo e botão
        c.gridy = 0;
        c.gridx = 0; add(lblPlayer, c);
        c.gridx = 1; add(lblBalance, c);
        c.gridx = 2; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        add(Box.createHorizontalStrut(1), c);
        c.gridx = 3; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        add(btnVerProps, c);

        // Linha 0: botão de encerrar (lado direito)
        c.gridx = 4;
        add(btnEncerrar, c);

        // Linha 1: saldos de todos os jogadores
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 4;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(lblAllBalances, c);
    }

    /** Mantém a mesma assinatura para não quebrar o controller. */
    public void updateHud(String nome, Color cor, int saldo, List<String> props) {
        lblPlayer.setText("Jogador: " + (nome != null ? nome : "—"));
        lblPlayer.setForeground(cor != null ? cor : Color.BLACK);
        lblBalance.setText("Saldo: R$ " + BRL_INT.format(saldo));
        int n = (props != null ? props.size() : 0);
        btnVerProps.setText(n > 0 ? "Ver propriedades (" + n + ")" : "Ver propriedades");
        btnVerProps.setEnabled(n > 0);
    }

    /** NOVO: texto com os saldos de todos os jogadores. */
    public void updateAllBalances(String texto) {
        lblAllBalances.setText(texto != null ? texto : "Saldos: —");
    }

    /** Exposto para o controller ouvir o clique. */
    public JButton viewPropsButton() { return btnVerProps; }

    /** Exposto para o controller ouvir o clique de encerrar. */
    public JButton endGameButton() { return btnEncerrar; }
}
