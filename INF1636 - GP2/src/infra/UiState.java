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
 *
 * Observação: o avanço "oficial" do jogo vem do Model. A UI apenas
 * sincroniza este estado chamando setPos(...) e setJogadorAtualByIndiceJogador(...).
 */
public class UiState {

    private final int numJogadores;
    private final int[] pos;       // casa 0..39 por jogador (índice do jogador)
    private final int[] pista;     // 0..5 por jogador
    private final Color[] cores;   // cor por jogador (decisão da UI)
    private final String[] nomes;  // nome por jogador (decisão da UI)

    /** Ordem sorteada: lista de índices de jogadores. Ex.: [2,0,1] */
    private final List<Integer> ordem;

    /** Posição atual do ponteiro de turno dentro da ordem (0..n-1). */
    private int turnoIndex = 0;

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

        for (int i = 0; i < numJogadores; i++) {
            pos[i] = 0;
            pista[i] = i; // espalha os primeiros peões em pistas diferentes
            this.cores[i] = (cores != null && i < cores.length && cores[i] != null)
                    ? cores[i] : defaultColors(numJogadores)[i];
            this.nomes[i] = (nomes != null && i < nomes.length && nomes[i] != null && !nomes[i].isBlank())
                    ? nomes[i] : ("J" + (i+1));
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
        Color[] base = new Color[] {
            new Color(220,20,60),  // vermelho
            new Color(25,130,196), // azul
            new Color(34,139,34),  // verde
            new Color(255,140,0),  // laranja
            new Color(148,0,211),  // roxo
            new Color(240,200,0)   // amarelo
        };
        Color[] out = new Color[n];
        System.arraycopy(base, 0, out, 0, n);
        return out;
    }

    private static int norm40(int v) {
        return ((v % 40) + 40) % 40;
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

    public int getNumJogadores()          { return numJogadores; }
    public List<Integer> getOrdem()       { return Collections.unmodifiableList(ordem); }
}
