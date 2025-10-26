package view;

import infra.ImageStore;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

/** Barra dos dados: forçar valores, lançar e rolar aleatoriamente, com faces desenhadas em um canvas próprio. */
public class DicePanel extends JPanel {
    private final JComboBox<Integer> d1, d2;
    private final JButton rollBtn;        // usa os combos (forçar)
    private final JButton randomBtn;      // rolagem aleatória

    // imagens das faces 1..6
    private final BufferedImage[] faces = new BufferedImage[7];

    // último resultado mostrado
    private int faceLeft = 1, faceRight = 1;

    // canvas dedicado onde os dados são desenhados (evita sobreposição/corte)
    private final JComponent diceCanvas = new JComponent() {
        @Override public Dimension getPreferredSize() { return new Dimension(120, 44); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getHeight() - 8, 36); // lado do dado
            int pad  = 6;
            int totalW = size * 2 + pad;
            int x = (getWidth() - totalW) / 2;
            int y = (getHeight() - size) / 2;

            drawFace(g2, faceLeft,  x,            y, size);
            drawFace(g2, faceRight, x + size+pad, y, size);
            g2.dispose();
        }
    };

    public DicePanel() {
        setOpaque(false);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.gridy = 0;

        d1 = new JComboBox<>(IntStream.rangeClosed(1, 6).boxed().toArray(Integer[]::new));
        d2 = new JComboBox<>(IntStream.rangeClosed(1, 6).boxed().toArray(Integer[]::new));
        d1.setFocusable(false); d2.setFocusable(false);

        rollBtn   = new JButton("Lançar");
        randomBtn = new JButton("Rolar aleatório");
        rollBtn.setFocusable(false); randomBtn.setFocusable(false);

        // carrega faces 1..6
        for (int i = 1; i <= 6; i++) faces[i] = ImageStore.load("/dados/die_face_" + i + ".png");

        // linha de controles (GridBag) + canvas na direita
        c.gridx = 0; add(new JLabel("Forçar:"), c);
        c.gridx = 1; add(d1, c);
        c.gridx = 2; add(d2, c);
        c.gridx = 3; add(rollBtn, c);
        c.gridx = 4; add(randomBtn, c);

        c.gridx = 5;
    c.weightx = 0;                       // não empurra para a borda
    c.fill = GridBagConstraints.NONE;    // mantém tamanho preferido
    c.anchor = GridBagConstraints.WEST;  // encosta no botão "Rolar aleatório"
    c.insets = new Insets(2, 8, 2, 2);   // pequeno espaço à esquerda
    add(diceCanvas, c);
    }

    // --- API usada pelo controller ---
    public JButton rollButton()   { return rollBtn; }
    public JButton randomButton() { return randomBtn; }

    public int forcedD1() { return (Integer) d1.getSelectedItem(); }
    public int forcedD2() { return (Integer) d2.getSelectedItem(); }

    /** Sincroniza os combos quando rolar aleatório. */
    public void setForced(int v1, int v2) {
        d1.setSelectedItem(v1);
        d2.setSelectedItem(v2);
    }

    /** Define as faces exibidas e repinta o canvas. */
    public void setFaces(int f1, int f2) {
        faceLeft  = Math.max(1, Math.min(6, f1));
        faceRight = Math.max(1, Math.min(6, f2));
        diceCanvas.repaint();
    }

    private void drawFace(Graphics2D g2, int face, int x, int y, int size) {
        BufferedImage img = (face >= 1 && face <= 6) ? faces[face] : null;
        if (img != null) {
            g2.drawImage(img, x, y, size, size, null); // drawImage (exigência do enunciado)
        } else {
            g2.setColor(Color.WHITE); g2.fillRoundRect(x, y, size, size, 6, 6);
            g2.setColor(Color.DARK_GRAY); g2.drawRoundRect(x, y, size, size, 6, 6);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
            String s = String.valueOf(face);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s, x + (size - fm.stringWidth(s))/2, y + (size + fm.getAscent())/2 - 4);
        }
    }
}
