package Model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Representa o tabuleiro do jogo Banco Imobiliário.
 * Mantém o controle das propriedades, jogadores ativos e do baralho de cartas Sorte/Revés.
 */
class Tabuleiro {

    // Número padrão de casas
    private static final int NUM_CASAS = 40;
    // Posições de prisão
    private static final int POSICAO_VAI_PRA_PRISAO = 30;
    private static final int POSICAO_PRISAO = 10;

    protected List<Propriedade> propriedades;
    protected List<Jogador> jogadoresAtivos;
    protected final Queue<Carta> baralhoSorteReves;

    public Tabuleiro() {
        this.propriedades = new ArrayList<>();
        this.jogadoresAtivos = new ArrayList<>();
        this.baralhoSorteReves = new LinkedList<>();
    }

    public static int getNumCasas() { return NUM_CASAS; }
    public static int getPosicaoPrisao() { return POSICAO_VAI_PRA_PRISAO; }
    public static int getPosicaoVisitaPrisao() { return POSICAO_PRISAO; }
    public boolean isCasaPrisao(int posicao) { return posicao == POSICAO_VAI_PRA_PRISAO; }

    // ---------- Propriedades ----------
    public void addPropriedade(Propriedade p) { propriedades.add(p); }
    public List<Propriedade> getPropriedades() { return propriedades; }
    public void limparPropriedadesDe(Jogador jogador) {
        for (Propriedade p : propriedades) {
            if (p.getProprietario() == jogador) p.setProprietario(null);
        }
    }

    // ---------- Jogadores ----------
    public void addJogador(Jogador jogador) { jogadoresAtivos.add(jogador); }
    public void removerJogador(Jogador jogador) { jogadoresAtivos.remove(jogador); }
    public List<Jogador> getJogadoresAtivos() { return jogadoresAtivos; }
    public boolean estaNoJogo(Jogador jogador) { return jogadoresAtivos.contains(jogador); }

    public Propriedade getPropriedadeNaPosicao(int posicao) {
        for (Propriedade p : propriedades) {
            if (p.getPosicao() == posicao) return p;
        }
        return null;
    }

    // ---------- Cartas ----------
    /** Baralho de teste simples. */
    public void inicializarBaralhoTeste() {
        if (baralhoSorteReves == null) return;
        baralhoSorteReves.clear();
        baralhoSorteReves.offer(new Carta(TipoCarta.VAI_PARA_PRISAO, 0));
        baralhoSorteReves.offer(new Carta(TipoCarta.SAIDA_LIVRE,     0));
        baralhoSorteReves.offer(new Carta(TipoCarta.PAGAR,         100));
        baralhoSorteReves.offer(new Carta(TipoCarta.RECEBER,       200));
    }

    /**
     * Compra uma carta. SAÍDA_LIVRE não retorna ao baralho agora.
     */
    public Carta comprarCartaSorteReves() {
        Carta c = baralhoSorteReves.poll();
        if (c == null) throw new IllegalStateException("Baralho de Sorte/Revés vazio.");
        if (c.tipo == TipoCarta.SAIDA_LIVRE) {
            // jogador guarda; não volta já
        } else {
            baralhoSorteReves.offer(c);
        }
        return c;
    }

    /** Ao usar a SAÍDA_LIVRE, devolve carta equivalente ao fim da fila. */
    public void devolverCartaLiberacao() {
        baralhoSorteReves.offer(new Carta(TipoCarta.SAIDA_LIVRE, 0));
    }
}
