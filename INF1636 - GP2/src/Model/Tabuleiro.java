package Model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Tabuleiro do jogo.
 * <p>Mantém: propriedades, jogadores ativos e baralho de Sorte/Revés.</p>
 * <ul>
 *   <li>Casa 10 = prisão/visita (onde o peão fica preso).</li>
 *   <li>Casa 30 = “vá para a prisão” (gatilho que envia à casa 10).</li>
 * </ul>
 */
class Tabuleiro {

    private static final int NUM_CASAS = 40;
    private static final int POSICAO_VAI_PRA_PRISAO = 30; // gatilho
    private static final int POSICAO_PRISAO = 10;         // prisão/visita

    protected List<Propriedade> propriedades;
    protected List<Jogador> jogadoresAtivos;
    protected final Queue<Carta> baralhoSorteReves;

    public Tabuleiro() {
        this.propriedades = new ArrayList<>();
        this.jogadoresAtivos = new ArrayList<>();
        this.baralhoSorteReves = new LinkedList<>();
    }

    /** Total de casas (0..39). */
    public static int getNumCasas() { return NUM_CASAS; }

    /** Posição da casa “vá para a prisão” (30). */
    public static int getPosicaoPrisao() { return POSICAO_VAI_PRA_PRISAO; }

    /** Posição da casa prisão/visita (10). */
    public static int getPosicaoVisitaPrisao() { return POSICAO_PRISAO; }

    /** Se a posição é a casa “vá para a prisão” (30). */
    public boolean isCasaPrisao(int posicao) { return posicao == POSICAO_VAI_PRA_PRISAO; }

    // ---------- Propriedades ----------
    public void addPropriedade(Propriedade p) { propriedades.add(p); }
    public List<Propriedade> getPropriedades() { return propriedades; }
    public void limparPropriedadesDe(Jogador jogador) {
        for (Propriedade p : propriedades) if (p.getProprietario() == jogador) p.setProprietario(null);
    }

    // ---------- Jogadores ----------
    public void addJogador(Jogador jogador) { jogadoresAtivos.add(jogador); }
    public void removerJogador(Jogador jogador) { jogadoresAtivos.remove(jogador); }
    public List<Jogador> getJogadoresAtivos() { return jogadoresAtivos; }
    public boolean estaNoJogo(Jogador jogador) { return jogadoresAtivos.contains(jogador); }

    /** Propriedade na posição informada (ou null). */
    public Propriedade getPropriedadeNaPosicao(int posicao) {
        for (Propriedade p : propriedades) if (p.getPosicao() == posicao) return p;
        return null;
    }

    // ---------- Cartas ----------
    /** Baralho de teste simples. */
    public void inicializarBaralhoTeste() {
        baralhoSorteReves.clear();
        baralhoSorteReves.offer(new Carta(TipoCarta.VAI_PARA_PRISAO, 0));
        baralhoSorteReves.offer(new Carta(TipoCarta.SAIDA_LIVRE,     0));
        baralhoSorteReves.offer(new Carta(TipoCarta.PAGAR,         100));
        baralhoSorteReves.offer(new Carta(TipoCarta.RECEBER,       200));
    }

    /** Compra uma carta. SAÍDA_LIVRE não retorna ao baralho agora. */
    public Carta comprarCartaSorteReves() {
        Carta c = baralhoSorteReves.poll();
        if (c == null) throw new IllegalStateException("Baralho de Sorte/Revés vazio.");
        if (c.tipo != TipoCarta.SAIDA_LIVRE) baralhoSorteReves.offer(c);
        return c;
    }

    /** Ao usar SAÍDA_LIVRE, devolve carta equivalente ao fim da fila. */
    public void devolverCartaLiberacao() {
        baralhoSorteReves.offer(new Carta(TipoCarta.SAIDA_LIVRE, 0));
    }
}
