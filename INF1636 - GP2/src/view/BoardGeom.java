package view;

import java.awt.*;

/**
 * Geometria do tabuleiro: centro das 40 casas e offset de "pistas" (0..5)
 * para desenhar pinos sem sobreposição.
 */
public final class BoardGeom {
    private BoardGeom(){}

    /** Centro aproximado da casa `cell` (0..39) já considerando o retângulo do board. */
    public static Point centerOfCell(int cell, int bx, int by, int bw, int bh) {
        int margin = (int) (0.08 * bw);
        int side   = bw - 2 * margin;
        int step   = side / 10;
        int c = ((cell % 40) + 40) % 40;

        int cx, cy;
        if (c < 10) { // borda inferior: 0..9
            int k = c;
            cx = bx + margin + side - (k * step) - step/2;
            cy = by + margin + side + step/2;
        } else if (c < 20) { // borda esquerda: 10..19
            int k = c - 10;
            cx = bx + margin - step/2;
            cy = by + margin + side - (k * step) - step/2;
        } else if (c < 30) { // borda superior: 20..29
            int k = c - 20;
            cx = bx + margin + (k * step) + step/2;
            cy = by + margin - step/2;
        } else { // borda direita: 30..39
            int k = c - 30;
            cx = bx + margin + side + step/2;
            cy = by + margin + (k * step) + step/2;
        }
        return new Point(cx, cy);
    }

    /**
     * Offset da "pista" (0..5) para espalhar visualmente os pinos.
     * Retorna um pequeno deslocamento em pixels ao redor do centro da casa.
     */
    public static Point trackOffset(int pista) {
        // 6 offsets distribuídos em um círculo pequeno
        final int r = 14; // raio do deslocamento
        switch (pista % 6) {
            case 0: return new Point( 0, -r);
            case 1: return new Point( r,  0);
            case 2: return new Point( 0,  r);
            case 3: return new Point(-r,  0);
            case 4: return new Point( r,  r);
            default: return new Point(-r, r);
        }
    }
}
