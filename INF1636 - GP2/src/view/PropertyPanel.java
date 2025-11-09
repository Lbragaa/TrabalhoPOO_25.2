package view;

import infra.ImageStore;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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

        String path = isChance ? CardResolver.randomChanceCardPath()
                               : CardResolver.propertyCardPath(cellIndex);
        img = (path != null ? ImageStore.loadCached(path) : null);

        revalidate();
        repaint();
    }

    /** Exibe explicitamente a carta de Sorte/Revés sorteada (chance{cardNumber}.png). */
    public void showChanceCard(int cardNumber, String title, String detail) {
        this.ownerColor = null;
        this.title  = (title  != null ? title  : "Sorte/Revés");
        this.detail = (detail != null ? detail : "—");
        String path = "/sorteReves/chance" + Math.max(1, Math.min(30, cardNumber)) + ".png";
        this.img = ImageStore.loadCached(path);
        revalidate();
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // cartão de fundo
        g2.setColor(new Color(255,255,255,235));
        g2.fillRoundRect(12, 12, w-24, h-24, 16, 16);
        g2.setColor(new Color(0,0,0,50));
        g2.drawRoundRect(12, 12, w-24, h-24, 16, 16);

        int x = 24, y = 30;

        // título
        g2.setColor(new Color(40,40,40));
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
        g2.drawString(title, x, y);

        // “selo” do dono (cor) — só para propriedades
        if (ownerColor != null) {
            int r = 8;
            int cx = w - 24 - 2*r;
            int cy = 24;
            g2.setColor(new Color(0,0,0,100));
            g2.fillOval(cx, cy, 2*r, 2*r);
            g2.setColor(ownerColor);
            g2.fillOval(cx+1, cy+1, 2*r-2, 2*r-2);
        }

        // imagem da carta
        y += 10;
        int imgTop = y;
        if (img != null) {
            int maxW = w - 48;
            int maxH = (int)Math.round(h * 0.45); // dá mais espaço ao parágrafo
            double sx = maxW / (double) img.getWidth();
            double sy = maxH / (double) img.getHeight();
            double s = Math.min(sx, sy);
            int iw = (int)Math.round(img.getWidth() * s);
            int ih = (int)Math.round(img.getHeight() * s);
            int ix = x + (maxW - iw) / 2;
            g2.drawImage(img, ix, imgTop, iw, ih, null);
            y = imgTop + ih + 16;
        } else {
            g2.setColor(new Color(0,0,0,60));
            g2.setFont(g2.getFont().deriveFont(Font.ITALIC, 12f));
            g2.drawString("Sem carta para esta casa.", x, imgTop + 16);
            y = imgTop + 40;
        }

        // detalhes (com suporte a \n e quebra de linha por largura)
        g2.setColor(new Color(60,60,60));
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        drawParagraphWrapped(g2, detail, x, y, w - 48, 16);

        g2.dispose();
    }

    /** Desenha texto com quebras explícitas (\n) e word-wrap por largura. */
    private static void drawParagraphWrapped(Graphics2D g2, String text, int x, int y, int width, int lineH) {
        if (text == null || text.isEmpty()) return;
        FontMetrics fm = g2.getFontMetrics();
        for (String block : text.split("\n")) {
            List<String> lines = wrapLine(block, fm, width);
            for (String ln : lines) {
                g2.drawString(ln, x, y);
                y += lineH;
            }
        }
    }

    /** Word-wrap simples baseado em largura. */
    private static List<String> wrapLine(String s, FontMetrics fm, int maxW) {
        List<String> out = new ArrayList<>();
        if (s == null) { out.add(""); return out; }
        String[] words = s.split("\\s+");
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            String tryLine = cur.length() == 0 ? w : cur + " " + w;
            if (fm.stringWidth(tryLine) <= maxW) {
                cur.setLength(0);
                cur.append(tryLine);
            } else {
                if (cur.length() > 0) out.add(cur.toString());
                // se palavra isolada for maior que maxW, força quebra bruta
                if (fm.stringWidth(w) > maxW) {
                    String part = "";
                    for (char ch : w.toCharArray()) {
                        String next = part + ch;
                        if (fm.stringWidth(next) > maxW) {
                            out.add(part);
                            part = String.valueOf(ch);
                        } else {
                            part = next;
                        }
                    }
                    cur.setLength(0);
                    cur.append(part);
                } else {
                    cur.setLength(0);
                    cur.append(w);
                }
            }
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }
}
