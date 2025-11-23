package infra;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estado visual mantido pela camada de View/Controller (sem regras de jogo).
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Posições dos peões (0..39), “pista” para evitar sobreposição visual, cores/nomes exibidos.</li>
 *   <li>Ordem sorteada de exibição e ponteiro do turno para destacar o jogador da vez.</li>
 *   <li>Índice do pino (0..5) mapeado a partir da cor escolhida.</li>
 * </ul>
 * <p>Observação: a verdade do jogo vem do Model; este estado serve para refletir/desenhar a UI.</p>
 */
public class UiState {

    private final int numJogadores;
    private final int[] pos;        // casa 0..39 por jogador
    private final int[] pista;      // 0..5 por jogador (offset visual)
    private final Color[] cores;    // cor exibida
    private final String[] nomes;   // nome exibido
    private final int[] pinoIndex;  // 0..5 (seleção de sprite)
    private final boolean[] ativo;  // true se jogador ainda está no jogo

    /** Ordem sorteada de jogadores (ex.: [2,0,1]). */
    private final List<Integer> ordem;

    /** Posição atual do ponteiro de turno dentro de {@link #ordem}. */
    private int turnoIndex = 0;

    /** Paleta fixa de pinos (UI e sprites devem estar alinhados a esta paleta). */
    public static final Color[] PIN_PALETTE = new Color[] {
            Color.RED, Color.BLUE, Color.ORANGE, Color.YELLOW, Color.PINK, Color.GRAY
    };

    // ================== CONSTRUTORES ==================

    /**
     * Construtor completo.
     * @param numJogadores 3..6
     * @param cores cores preferidas (pode conter nulos; usa padrão se faltar)
     * @param nomes nomes preferidos (pode conter nulos/vazios; usa "J1..Jn")
     * @param ordemSorteada vetor com os índices (0..n-1) ou {@code null} para embaralhar
     */
    public UiState(int numJogadores, Color[] cores, String[] nomes, int[] ordemSorteada) {
        if (numJogadores < 3 || numJogadores > 6) {
            throw new IllegalArgumentException("numJogadores deve ser 3..6");
        }
        this.numJogadores = numJogadores;
        this.pos   = new int[numJogadores];
        this.pista = new int[numJogadores];
        this.cores = new Color[numJogadores];
        this.nomes = new String[numJogadores];
        this.pinoIndex = new int[numJogadores];
        this.ativo = new boolean[numJogadores];

        Color[] defaults = defaultColors(numJogadores);

        for (int i = 0; i < numJogadores; i++) {
            pos[i] = 0;
            pista[i] = i; // distribui visualmente os peões de saída
            ativo[i] = true;

            Color cor = (cores != null && i < cores.length && cores[i] != null)
                    ? cores[i] : defaults[i];
            this.cores[i] = cor;

            this.nomes[i] = (nomes != null && i < nomes.length && nomes[i] != null && !nomes[i].isBlank())
                    ? nomes[i] : ("J" + (i+1));

            this.pinoIndex[i] = mapColorToPinIndex(cor);
        }

        this.ordem = new ArrayList<>();
        if (ordemSorteada != null && ordemSorteada.length == numJogadores) {
            for (int v : ordemSorteada) ordem.add(v);
        } else {
            for (int i = 0; i < numJogadores; i++) ordem.add(i);
            Collections.shuffle(this.ordem);
        }
    }

    /** Compatibilidade: sem nomes/ordem. */
    public UiState(int numJogadores, Color[] cores) { this(numJogadores, cores, null, null); }
    public UiState(int numJogadores) { this(numJogadores, null, null, null); }

    // ================== HELPERS ==================

    private static Color[] defaultColors(int n) {
        Color[] base = PIN_PALETTE;
        Color[] out = new Color[n];
        System.arraycopy(base, 0, out, 0, n); // n <= 6 garantido pela validação
        return out;
    }

    private static int norm40(int v) {
        return ((v % 40) + 40) % 40;
    }

    /** Mapeia cor -> índice de pino (0..5). Tolerante a variações próximas. */
    private static int mapColorToPinIndex(Color c) {
        if (c == null) return 0;
        for (int i = 0; i < PIN_PALETTE.length; i++) if (c.equals(PIN_PALETTE[i])) return i;
        int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
        if (r > 200 && g <  80 && b <  80) return 0; // Red
        if (r <  80 && g < 170 && b > 160) return 1; // Blue
        if (r > 230 && g > 130 && b <  60) return 2; // Orange
        if (r > 220 && g > 220 && b < 120) return 3; // Yellow
        if (r > 230 && g < 160 && b > 200) return 4; // Pink
        if (Math.abs(r-g) < 15 && Math.abs(g-b) < 15 && r > 90 && r < 180) return 5; // Gray
        return 0;
    }

    // ================== MÉTODOS USADOS PELA VIEW ==================

    /** Índice do jogador da vez (no domínio 0..n-1). */
    public int jogadorAtual() { return ordem.get(turnoIndex); }

    // ======= Sincronização com o Model (chamadas feitas pelo Controller) =======

    /** Ajusta a posição de um jogador para refletir o estado do Model. */
    public void setPos(int jogador, int posicao) {
        if (jogador < 0 || jogador >= numJogadores) return;
        pos[jogador] = norm40(posicao);
    }

    /**
     * Ajusta o "jogador da vez" refletindo o turno vindo do Model.
     * @param jogadorIndex índice do jogador (0..n-1)
     */
    public void setJogadorDaVez(int jogadorIndex) {
        for (int i = 0; i < ordem.size(); i++) {
            if (ordem.get(i) == jogadorIndex) { turnoIndex = i; break; }
        }
    }

    // ================== GETTERS PARA A VIEW DESENHAR ==================

    public int getPos(int jogador)       { return pos[jogador]; }
    public int getPista(int jogador)     { return pista[jogador]; }
    public Color getCor(int jogador)     { return cores[jogador]; }
    public String getNome(int jogador)   { return nomes[jogador]; }
    public String getNomeAtual()         { return nomes[jogadorAtual()]; }
    public int getPinoIndex(int jogador) { return pinoIndex[jogador]; }
    public boolean isAtivo(int jogador)  { return jogador >=0 && jogador < ativo.length && ativo[jogador]; }
    /** Marca jogador como ativo/inativo para refletir falência. */
    public void setAtivo(int jogador, boolean status) {
        if (jogador < 0 || jogador >= ativo.length) return;
        ativo[jogador] = status;
    }

    public int getNumJogadores()         { return numJogadores; }
    public List<Integer> getOrdem()      { return Collections.unmodifiableList(ordem); }
}
