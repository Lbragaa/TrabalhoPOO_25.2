package view;

import java.awt.*;

/**
 * Geometria do tabuleiro: centro das 40 casas e offset de "pistas" (0..5)
 * para desenhar pinos sem sobreposição.
 *
 * Corrigido: usa 11 passos por lado (canto + 9 + canto) e separa X/Y.
 */
public final class BoardGeom {
    private BoardGeom(){}

    /** Centro aproximado da casa `cell` (0..39) já considerando o retângulo do board. */
    public static Point centerOfCell(int cell, int bx, int by, int bw, int bh) {
        // Margens proporcionais (ajuste fino se necessário)
        double marginX = 0.08 * bw;
        double marginY = 0.08 * bh;

        // Comprimento útil (entre os cantos) em cada eixo
        double sideX = bw - 2.0 * marginX;
        double sideY = bh - 2.0 * marginY;

        // Espaçamento entre centros ao longo do lado: 11 centros por lado
        double stepX = sideX / 11.0;
        double stepY = sideY / 11.0;

        int c = ((cell % 40) + 40) % 40;

        double cx, cy;
        if (c <= 10) { // borda inferior: 0..10 (da direita p/ esquerda)
            int k = c; // 0..10
            cx = bx + marginX + sideX - (k + 0.5) * stepX;
            cy = by + marginY + sideY + stepY / 2.0; // pista fora do tabuleiro
        } else if (c <= 20) { // borda esquerda: 10..20 (de baixo p/ cima)
            int k = c - 10; // 0..10
            cx = bx + marginX - stepX / 2.0; // pista fora do tabuleiro
            cy = by + marginY + sideY - (k + 0.5) * stepY;
        } else if (c <= 30) { // borda superior: 20..30 (da esquerda p/ direita)
            int k = c - 20; // 0..10
            cx = bx + marginX + (k + 0.5) * stepX;
            cy = by + marginY - stepY / 2.0; // pista fora do tabuleiro
        } else { // borda direita: 30..39 (de cima p/ baixo)
            int k = c - 30; // 0..9
            cx = bx + marginX + sideX + stepX / 2.0; // pista fora do tabuleiro
            cy = by + marginY + (k + 0.5) * stepY;
        }

        return new Point((int)Math.round(cx), (int)Math.round(cy));
    }

    /**
     * Offset da "pista" (0..5) para espalhar visualmente os pinos.
     * Retorna um pequeno deslocamento em pixels ao redor do centro da casa.
     */
    public static Point trackOffset(int pista) {
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
