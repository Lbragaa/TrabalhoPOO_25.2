package view;

import infra.ImageStore;
import infra.UiState;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BoardPanel extends JPanel {
    private final BufferedImage boardImg;
    private final UiState ui;

    // pin images 0..5 (Red, Blue, Orange, Yellow, Pink, Gray)
    private final BufferedImage[] pinImgs = new BufferedImage[6];
    private static final int PIN_W = 25, PIN_H = 38;
    private static final int PIN_Y_BIAS = 6; // raise a bit so the tip sits inside the cell ring

    public BoardPanel(UiState ui) {
        this.ui = ui;
        this.boardImg = ImageStore.load("/tabuleiro.png");
        int w = (boardImg != null ? boardImg.getWidth()  : 1000);
        int h = (boardImg != null ? boardImg.getHeight() : 700);
        setPreferredSize(new Dimension(w, h));
        setOpaque(true);

        // load pins
        for (int i = 0; i < pinImgs.length; i++) {
            pinImgs[i] = ImageStore.load("/pinos/pin" + i + ".png");
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 0, y = 0, bw = (boardImg!=null?boardImg.getWidth():getWidth()), bh = (boardImg!=null?boardImg.getHeight():getHeight());
        if (boardImg != null) {
            x = (getWidth()-bw)/2;
            y = (getHeight()-bh)/2;
            g2.drawImage(boardImg, x, y, null);
        } else {
            g2.setColor(Color.LIGHT_GRAY); g2.fillRect(0,0,getWidth(),getHeight());
            g2.setColor(Color.DARK_GRAY);  g2.drawString("tabuleiro.png não encontrado.", 20, 20);
        }

        if (ui != null) {
            // turn badge
            int barraW = 280, barraH = 28;
            int bx = getWidth() - (barraW + 16), by = 12;
            g2.setColor(new Color(0,0,0,80));
            g2.fillRoundRect(bx-2, by-2, barraW+4, barraH+4, 10, 10);
            g2.setColor(ui.getCor(ui.jogadorAtual()));
            g2.fillRoundRect(bx, by, barraW, barraH, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString("Jogador da vez: " + ui.getNomeAtual(), bx + 10, by + 18);

            // draw pins
            for (int j = 0; j < ui.getNumJogadores(); j++) {
                int cell = ui.getPos(j);
                Point c = BoardGeom.centerOfCell(cell, x, y, bw, bh);
                Point off = BoardGeom.trackOffset(ui.getPista(j));
                int px = c.x + off.x;
                int py = c.y + off.y;

                // choose pin index (prefer UiState if it has it; else map by color)
                int pinIdx = pinIndexForPlayer(j);

                BufferedImage pin = (pinIdx >=0 && pinIdx < pinImgs.length) ? pinImgs[pinIdx] : null;
                if (pin != null) {
                    int drawX = px - PIN_W/2;
                    int drawY = py - PIN_H + PIN_Y_BIAS;
                    g2.drawImage(pin, drawX, drawY, PIN_W, PIN_H, null);

                    // name label centered above the pin
                    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 10f));
                    FontMetrics fm = g2.getFontMetrics();
                    String name = ui.getNome(j);
                    int textW = fm.stringWidth(name);
                    int tx = px - textW/2;
                    int ty = drawY - 4;
                    g2.setColor(Color.BLACK); g2.drawString(name, tx+1, ty+1);
                    g2.setColor(Color.WHITE); g2.drawString(name, tx, ty);
                } else {
                    // fallback: draw circle as before
                    int r = 10;
                    g2.setColor(Color.BLACK); g2.fillOval(px-r-1, py-r-1, 2*r+2, 2*r+2);
                    g2.setColor(ui.getCor(j)); g2.fillOval(px-r, py-r, 2*r, 2*r);
                    g2.setColor(Color.WHITE);
                    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 10f));
                    g2.drawString(ui.getNome(j), px - 12, py - r - 4);
                }
            }
        }
        g2.dispose();
    }

    // Maps to pin index if UiState already provides it; otherwise color-based fallback.
    private int pinIndexForPlayer(int j) {
        try {
            // if UiState has getPinoIndex(int), use it
            return (int) UiState.class.getMethod("getPinoIndex", int.class).invoke(ui, j);
        } catch (Exception ignore) {
            // fallback by color (must match PlayerSetupDialog palette)
            Color c = ui.getCor(j);
            if (c == null) return 0;
            if (c.equals(Color.RED))    return 0;
            if (c.equals(Color.BLUE))   return 1;
            // Orange (not a constant) — common RGB(255,165,0)
            if (c.getRed() > 240 && c.getGreen() > 140 && c.getBlue() < 20) return 2;
            if (c.equals(Color.YELLOW)) return 3;
            // Pink — approximate
            if (c.getRed() > 240 && c.getGreen() < 120 && c.getBlue() > 180) return 4;
            if (c.equals(Color.GRAY))   return 5;
            return 0;
        }
    }
}
