package view;

import infra.ImageStore;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/** Mostra a "carta" (propriedade/companhia ou Sorte/Revés) e algumas infos visuais. */
public class PropertyPanel extends JPanel {
    private BufferedImage img;
    private String title = "Casa";
    private String detail = "—";
    private Color ownerColor;

    public PropertyPanel() {
        setPreferredSize(new Dimension(260, 0));
        setOpaque(false);
    }

    /** Exibe dados para a casa 'cell'. */
    public void showForCell(int cellIndex, boolean isChance, Color ownerColor, String title, String detail) {
        this.ownerColor = ownerColor;
        this.title = (title != null ? title : (isChance ? "Sorte/Revés" : ("Casa " + cellIndex)));
        this.detail = (detail != null ? detail : "—");

        String path;
        if (isChance) {
            path = CardResolver.randomChanceCardPath();
        } else {
            path = CardResolver.propertyCardPath(cellIndex);
        }
        img = (path != null ? ImageStore.loadCached(path) : null);

        revalidate();
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // cartão de fundo
        g2.setColor(new Color(255,255,255,235));
        g2.fillRoundRect(12, 12, w-24, h-24, 16, 16);
        g2.setColor(new Color(0,0,0,50));
        g2.drawRoundRect(12, 12, w-24, h-24, 16, 16);

        int x = 24, y = 28;

        // título
        g2.setColor(new Color(40,40,40));
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
        g2.drawString(title, x, y);
        y += 8;

        // “selo” do dono (cor) — só para propriedades
        if (ownerColor != null) {
            int r = 8;
            g2.setColor(Color.DARK_GRAY);
            g2.fillOval(w-24-2*r, 24, 2*r, 2*r);
            g2.setColor(ownerColor);
            g2.fillOval(w-24-2*r+1, 24+1, 2*r-2, 2*r-2);
        }

        // imagem da carta
        int imgTop = y + 8;
        if (img != null) {
            int maxW = w - 48;
            int maxH = h/2;
            double sx = maxW / (double) img.getWidth();
            double sy = maxH / (double) img.getHeight();
            double s = Math.min(sx, sy);
            int iw = (int) Math.round(img.getWidth() * s);
            int ih = (int) Math.round(img.getHeight() * s);
            int ix = x + (maxW - iw) / 2;
            g2.drawImage(img, ix, imgTop, iw, ih, null);
            y = imgTop + ih + 16;
        } else {
            g2.setColor(new Color(0,0,0,60));
            g2.setFont(g2.getFont().deriveFont(Font.ITALIC, 12f));
            g2.drawString("Sem carta para esta casa.", x, imgTop + 16);
            y = imgTop + 40;
        }

        // detalhes
        g2.setColor(new Color(60,60,60));
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        drawParagraph(g2, detail, x, y, w - 48, 16);
        g2.dispose();
    }

    private static void drawParagraph(Graphics2D g2, String text, int x, int y, int width, int lineH) {
        if (text == null) return;
        for (String line : text.split("\n")) {
            g2.drawString(line, x, y);
            y += lineH;
        }
    }
}
