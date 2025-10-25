package view;

import infra.ImageStore;
import infra.UiState;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BoardPanel extends JPanel {
    private final BufferedImage boardImg;
    private final UiState ui;

    public BoardPanel(UiState ui) {
        this.ui = ui;
        this.boardImg = ImageStore.load("/tabuleiro.png");
        int w = (boardImg != null ? boardImg.getWidth()  : 1000);
        int h = (boardImg != null ? boardImg.getHeight() : 700);
        setPreferredSize(new Dimension(w, h));
        setOpaque(true);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x=0,y=0;
        if (boardImg != null) {
            x = (getWidth()-boardImg.getWidth())/2;
            y = (getHeight()-boardImg.getHeight())/2;
            g2.drawImage(boardImg, x, y, null);
        } else {
            g2.setColor(Color.LIGHT_GRAY); g2.fillRect(0,0,getWidth(),getHeight());
            g2.setColor(Color.DARK_GRAY);  g2.drawString("tabuleiro.png não encontrado.", 20, 20);
        }

        if (ui != null) {
            int barraW = 280, barraH = 28;
            int bx = getWidth() - (barraW + 16), by = 12;
            g2.setColor(new Color(0,0,0,80));
            g2.fillRoundRect(bx-2, by-2, barraW+4, barraH+4, 10, 10);
            g2.setColor(ui.getCor(ui.jogadorAtual()));
            g2.fillRoundRect(bx, by, barraW, barraH, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString("Jogador da vez: " + ui.getNomeAtual(), bx + 10, by + 18);

            for (int j = 0; j < ui.getNumJogadores(); j++) {
                int cell = ui.getPos(j);
                Point c = BoardGeom.centerOfCell(cell, x, y,
                        (boardImg!=null?boardImg.getWidth():getWidth()),
                        (boardImg!=null?boardImg.getHeight():getHeight()));
                Point off = BoardGeom.trackOffset(ui.getPista(j));
                int px = c.x + off.x, py = c.y + off.y;

                int r = 10;
                g2.setColor(Color.BLACK); g2.fillOval(px-r-1, py-r-1, 2*r+2, 2*r+2);
                g2.setColor(ui.getCor(j)); g2.fillOval(px-r, py-r, 2*r, 2*r);

                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 10f));
                g2.drawString(ui.getNome(j), px - 12, py - r - 4);
            }
        }
        g2.dispose();
    }
}
