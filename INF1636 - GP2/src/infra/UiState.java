package infra;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estado visual mantido pela camada de View/Controller (sem regras de jogo).
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>PosiAAes dos peAes (0..39), ?pista?? para evitar sobreposiAAo visual, cores/nomes exibidos.</li>
 *   <li>Ordem sorteada de exibiAAo e ponteiro do turno para destacar o jogador da vez.</li>
 *   <li>A?ndice do pino (0..5) mapeado a partir da cor escolhida.</li>
 * </ul>
 * <p>ObservaAAo: a verdade do jogo vem do Model; este estado serve para refletir/desenhar a UI.</p>
 */
public class UiState {

    private final int numJogadores;
    private final List<Integer> pos;       // casa 0..39 por jogador
    private final List<Integer> pista;     // 0..5 por jogador (offset visual)
    private final List<Color> cores;       // cor exibida
    private final List<String> nomes;      // nome exibido
    private final List<Integer> pinoIndex; // 0..5 (seleAAo de sprite)
    private final List<Boolean> ativo;     // true se jogador ainda estA no jogo

    /** Ordem sorteada de jogadores (ex.: [2,0,1]). */
    private final List<Integer> ordem;

    /** PosiAAo atual do ponteiro de turno dentro de {@link #ordem}. */
    private int turnoIndex = 0;

    /** Paleta fixa de pinos (UI e sprites devem estar alinhados a esta paleta). */
    public static final List<Color> PIN_PALETTE = List.of(
            Color.RED, Color.BLUE, Color.ORANGE, Color.YELLOW, Color.PINK, Color.GRAY
    );

    // ================== CONSTRUTORES ==================

    /**
     * Construtor completo.
     * @param numJogadores 3..6
     * @param cores cores preferidas (pode conter nulos; usa padrAo se faltar)
     * @param nomes nomes preferidos (pode conter nulos/vazios; usa "J1..Jn")
     * @param ordemSorteada vetor com os A-ndices (0..n-1) ou {@code null} para embaralhar
     */
    public UiState(int numJogadores, List<Color> cores, List<String> nomes, List<Integer> ordemSorteada) {
        if (numJogadores < 3 || numJogadores > 6) {
            throw new IllegalArgumentException("numJogadores deve ser 3..6");
        }
        this.numJogadores = numJogadores;
        this.pos   = new ArrayList<>(Collections.nCopies(numJogadores, 0));
        this.pista = new ArrayList<>(numJogadores);
        this.cores = new ArrayList<>(numJogadores);
        this.nomes = new ArrayList<>(numJogadores);
        this.pinoIndex = new ArrayList<>(numJogadores);
        this.ativo = new ArrayList<>(numJogadores);

        List<Color> defaults = defaultColors(numJogadores);

        for (int i = 0; i < numJogadores; i++) {
            pos.set(i, 0);
            pista.add(i); // distribui visualmente os peAes de saA-da
            ativo.add(true);

            Color cor = (cores != null && i < cores.size() && cores.get(i) != null)
                    ? cores.get(i) : defaults.get(i);
            this.cores.add(cor);

            this.nomes.add((nomes != null && i < nomes.size() && nomes.get(i) != null && !nomes.get(i).isBlank())
                    ? nomes.get(i) : ("J" + (i+1)));

            this.pinoIndex.add(mapColorToPinIndex(cor));
        }

        this.ordem = new ArrayList<>();
        if (ordemSorteada != null && ordemSorteada.size() == numJogadores) {
            ordem.addAll(ordemSorteada);
        } else {
            for (int i = 0; i < numJogadores; i++) ordem.add(i);
            Collections.shuffle(this.ordem);
        }
    }

    /** Compatibilidade: sem nomes/ordem. */
    public UiState(int numJogadores, List<Color> cores) { this(numJogadores, cores, null, null); }
    public UiState(int numJogadores) { this(numJogadores, null, null, null); }

    // ================== HELPERS ==================

    private static List<Color> defaultColors(int n) {
        return new ArrayList<>(PIN_PALETTE.subList(0, n)); // n <= 6 garantido pela validaAAo
    }

    private static int norm40(int v) {
        return ((v % 40) + 40) % 40;
    }

    /** Mapeia cor -> A-ndice de pino (0..5). Tolerante a variaAAes prA3ximas. */
    private static int mapColorToPinIndex(Color c) {
        if (c == null) return 0;
        for (int i = 0; i < PIN_PALETTE.size(); i++) {
            if (c.equals(PIN_PALETTE.get(i))) return i;
        }
        int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
        if (r > 200 && g <  80 && b <  80) return 0; // Red
        if (r <  80 && g < 170 && b > 160) return 1; // Blue
        if (r > 230 && g > 130 && b <  60) return 2; // Orange
        if (r > 220 && g > 220 && b < 120) return 3; // Yellow
        if (r > 230 && g < 160 && b > 200) return 4; // Pink
        if (Math.abs(r-g) < 15 && Math.abs(g-b) < 15 && r > 90 && r < 180) return 5; // Gray
        return 0;
    }

    // ================== MA%TODOS USADOS PELA VIEW ==================

    /** A?ndice do jogador da vez (no domA-nio 0..n-1). */
    public int jogadorAtual() { return ordem.get(turnoIndex); }

    // ======= SincronizaAAo com o Model (chamadas feitas pelo Controller) =======

    /** Ajusta a posiAAo de um jogador para refletir o estado do Model. */
    public void setPos(int jogador, int posicao) {
        if (jogador < 0 || jogador >= numJogadores) return;
        pos.set(jogador, norm40(posicao));
    }

    /**
     * Ajusta o "jogador da vez" refletindo o turno vindo do Model.
     * @param jogadorIndex A-ndice do jogador (0..n-1)
     */
    public void setJogadorDaVez(int jogadorIndex) {
        for (int i = 0; i < ordem.size(); i++) {
            if (ordem.get(i) == jogadorIndex) { turnoIndex = i; break; }
        }
    }

    // ================== GETTERS PARA A VIEW DESENHAR ==================

    public int getPos(int jogador)       { return pos.get(jogador); }
    public int getPista(int jogador)     { return pista.get(jogador); }
    public Color getCor(int jogador)     { return cores.get(jogador); }
    public String getNome(int jogador)   { return nomes.get(jogador); }
    public String getNomeAtual()         { return nomes.get(jogadorAtual()); }
    public int getPinoIndex(int jogador) { return pinoIndex.get(jogador); }
    public boolean isAtivo(int jogador)  { return jogador >=0 && jogador < ativo.size() && ativo.get(jogador); }
    /** Marca jogador como ativo/inativo para refletir falAncia. */
    public void setAtivo(int jogador, boolean status) {
        if (jogador < 0 || jogador >= ativo.size()) return;
        ativo.set(jogador, status);
    }

    public int getNumJogadores()         { return numJogadores; }
    public List<Integer> getOrdem()      { return Collections.unmodifiableList(ordem); }
}
