package infra;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estado visual mantido pela View:
 * - posições dos peões
 * - pista (0..5) para evitar sobreposição
 * - cores e nomes escolhidos na UI
 * - ordem sorteada (índices de jogadores) e apontador do turno
 * - índice do PINO (0..5) por jogador, para desenhar o PNG correspondente
 */
public class UiState {

    private final int numJogadores;
    private final int[] pos;        // casa 0..39 por jogador (índice do jogador)
    private final int[] pista;      // 0..5 por jogador
    private final Color[] cores;    // cor por jogador (decisão da UI)
    private final String[] nomes;   // nome por jogador (decisão da UI)
    private final int[] pinoIndex;  // 0..5 por jogador (pin0..pin5)

    /** Ordem sorteada: lista de índices de jogadores. Ex.: [2,0,1] */
    private final List<Integer> ordem;

    /** Posição atual do ponteiro de turno dentro da ordem (0..n-1). */
    private int turnoIndex = 0;

    // Paleta oficial de pinos (para PlayerSetupDialog usar a mesma)
    public static final Color[] PIN_PALETTE = new Color[] {
            Color.RED, Color.BLUE, Color.ORANGE, Color.YELLOW, Color.PINK, Color.GRAY
    };

    // ================== CONSTRUTORES ==================

    /** Construtor completo com nomes/cores/ordem. */
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

        Color[] defaults = defaultColors(numJogadores);

        for (int i = 0; i < numJogadores; i++) {
            pos[i] = 0;
            pista[i] = i; // espalha os primeiros peões em pistas diferentes

            Color cor = (cores != null && i < cores.length && cores[i] != null)
                    ? cores[i] : defaults[i];
            this.cores[i] = cor;

            this.nomes[i] = (nomes != null && i < nomes.length && nomes[i] != null && !nomes[i].isBlank())
                    ? nomes[i] : ("J" + (i+1));

            this.pinoIndex[i] = mapColorToPinIndex(cor); // 0..5 (pin0..pin5)
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
        Color[] base = PIN_PALETTE; // usa exatamente: Red, Blue, Orange, Yellow, Pink, Gray
        Color[] out = new Color[n];
        System.arraycopy(base, 0, out, 0, n);
        return out;
    }

    private static int norm40(int v) {
        return ((v % 40) + 40) % 40;
    }

    /** Mapeia cor -> índice de pino (0..5). Tolerante a variações próximas. */
    private static int mapColorToPinIndex(Color c) {
        if (c == null) return 0;
        // Igualdade direta com a paleta oficial
        for (int i = 0; i < PIN_PALETTE.length; i++) {
            if (c.equals(PIN_PALETTE[i])) return i;
        }
        // Aproximações por faixa
        int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
        if (r > 200 && g < 80  && b < 80)  return 0; // Red
        if (r < 80  && g < 170 && b > 160) return 1; // Blue
        if (r > 230 && g > 130 && b < 60)  return 2; // Orange
        if (r > 220 && g > 220 && b < 120) return 3; // Yellow
        if (r > 230 && g < 160 && b > 200) return 4; // Pink
        if (Math.abs(r-g) < 15 && Math.abs(g-b) < 15 && r > 90 && r < 180) return 5; // Gray
        return 0;
    }

    // ================== MÉTODOS USADOS PELA VIEW ==================

    /** Índice do jogador da vez (no domínio dos jogadores, não na posição da lista). */
    public int jogadorAtual() { return ordem.get(turnoIndex); }

    /** Avança o ponteiro de turno (uso eventual em protótipos; o oficial vem do Model). */
    public void proximoTurno() { turnoIndex = (turnoIndex + 1) % numJogadores; }

    /** Movimento local (útil para mock/testes da UI). O oficial vem do Model. */
    public void moverJogadorAtual(int passos) {
        int j = jogadorAtual();
        pos[j] = norm40(pos[j] + passos);
    }

    // ======= Sincronização com o Model (chamadas feitas pelo Controller) =======

    /** Ajusta a posição de um jogador para refletir o estado do Model. */
    public void setPos(int jogador, int posicao) {
        if (jogador < 0 || jogador >= numJogadores) return;
        pos[jogador] = norm40(posicao);
    }

    /**
     * Ajusta o "jogador da vez" para refletir o turno vindo do Model.
     * Recebe o índice do jogador (no domínio 0..n-1) e move o ponteiro de ordem
     * para a posição onde esse jogador aparece.
     */
    public void setJogadorAtualByIndiceJogador(int jogadorIndex) {
        for (int k = 0; k < ordem.size(); k++) {
            if (ordem.get(k) == jogadorIndex) {
                turnoIndex = k;
                return;
            }
        }
    }
    public void setJogadorDaVez(int jogadorIndex) {
        for (int i = 0; i < ordem.size(); i++) {
            if (ordem.get(i) == jogadorIndex) { turnoIndex = i; break; }
        }
    }

    // ================== GETTERS PARA A VIEW DESENHAR ==================

    public int getPos(int jogador)        { return pos[jogador]; }
    public int getPista(int jogador)      { return pista[jogador]; }
    public Color getCor(int jogador)      { return cores[jogador]; }
    public String getNome(int jogador)    { return nomes[jogador]; }
    public String getNomeAtual()          { return nomes[jogadorAtual()]; }
    public int getPinoIndex(int jogador)  { return pinoIndex[jogador]; }

    public int getNumJogadores()          { return numJogadores; }
    public List<Integer> getOrdem()       { return Collections.unmodifiableList(ordem); }
}
