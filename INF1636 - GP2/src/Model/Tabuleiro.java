package Model;

import java.util.*;

/**
 * Tabuleiro do jogo.
 * <p>Mantém: propriedades, jogadores ativos e baralho de Sorte/Revés.</p>
 * <ul>
 *   <li>Casa 10 = prisão/visita (onde o peão fica preso).</li>
 *   <li>Casa 30 = “vá para a prisão” (gatilho que envia à casa 10).</li>
 *   <li>Casas de Sorte/Revés: 2, 12, 16, 22, 27, 37.</li>
 * </ul>
 */
class Tabuleiro {

    private static final int NUM_CASAS = 40;
    private static final int POSICAO_VAI_PRA_PRISAO = 30; // gatilho
    private static final int POSICAO_PRISAO = 10;         // prisão/visita
    private static final Set<Integer> CHANCE_CELLS = Set.of(2, 12, 16, 22, 27, 37);
    private static final int POSICAO_LUCROS_DIVIDENDOS = 18;
    private static final int POSICAO_IMPOSTO_RENDA = 24;
    protected List<Propriedade> propriedades;
    protected List<Jogador> jogadoresAtivos;
    protected final Queue<Carta> baralhoSorteReves;

    public Tabuleiro() {
        this.propriedades = new ArrayList<>();
        this.jogadoresAtivos = new ArrayList<>();
        this.baralhoSorteReves = new LinkedList<>();
        inicializarBaralhoTeste(); // baralho real (chance1..chance30) embaralhado
    }

    /** Total de casas (0..39). */
    public static int getNumCasas() { return NUM_CASAS; }

    /** Posição da casa “vá para a prisão” (30). */
    public static int getPosicaoPrisao() { return POSICAO_VAI_PRA_PRISAO; }

    /** Posição da casa prisão/visita (10). */
    public static int getPosicaoVisitaPrisao() { return POSICAO_PRISAO; }

    /** Se a posição é a casa “vá para a prisão” (30). */
    public boolean isCasaPrisao(int posicao) { return posicao == POSICAO_VAI_PRA_PRISAO; }

    /** Se a posição é uma casa de Sorte/Revés. */
    public boolean isChanceCell(int posicao) { return CHANCE_CELLS.contains(posicao); }

        /** Se a posição é a casa de lucros/dividendos (18). */
    public boolean isCasaLucrosDividendos(int posicao) { return posicao == POSICAO_LUCROS_DIVIDENDOS; }

    /** Se a posição é a casa de imposto de renda (24). */
    public boolean isCasaImpostoRenda(int posicao) { return posicao == POSICAO_IMPOSTO_RENDA; }

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
    /** Baralho de teste simples (mantido para compatibilidade de testes). */
    public void inicializarBaralhoTeste() {
        baralhoSorteReves.clear();
        baralhoSorteReves.offer(new Carta(TipoCarta.VAI_PARA_PRISAO, 0, 23));
        baralhoSorteReves.offer(new Carta(TipoCarta.SAIDA_LIVRE,     0,  9));
        baralhoSorteReves.offer(new Carta(TipoCarta.PAGAR,         100, 25));
        baralhoSorteReves.offer(new Carta(TipoCarta.RECEBER,       200, 10));
    }

    /** Baralho real: chance1.png .. chance30.png mapeados para tipo/valor, embaralhado. */
    public void inicializarBaralhoSorteRevesDefault() {
        baralhoSorteReves.clear();
        List<Carta> cartas = new ArrayList<>();

        // SAÍDA_LIVRE
        cartas.add(new Carta(TipoCarta.SAIDA_LIVRE,      0,  9));

        // PAGAR
        cartas.add(new Carta(TipoCarta.PAGAR,           30, 24));
        cartas.add(new Carta(TipoCarta.PAGAR,           50, 25));
        cartas.add(new Carta(TipoCarta.PAGAR,           25, 26));
        cartas.add(new Carta(TipoCarta.PAGAR,           30, 27));
        cartas.add(new Carta(TipoCarta.PAGAR,           45, 28));
        cartas.add(new Carta(TipoCarta.PAGAR,           50, 29));
        cartas.add(new Carta(TipoCarta.PAGAR,           50, 30));

        // RECEBER
        cartas.add(new Carta(TipoCarta.RECEBER,         25,  1));
        cartas.add(new Carta(TipoCarta.RECEBER,        150,  2));
        cartas.add(new Carta(TipoCarta.RECEBER,         80,  3));
        cartas.add(new Carta(TipoCarta.RECEBER,        200,  4));
        cartas.add(new Carta(TipoCarta.RECEBER,         50,  5));
        cartas.add(new Carta(TipoCarta.RECEBER,         50,  6));
        cartas.add(new Carta(TipoCarta.RECEBER,        100,  7));
        cartas.add(new Carta(TipoCarta.RECEBER,        100,  8));

        // RECEBER
        cartas.add(new Carta(TipoCarta.RECEBER,        200, 10));

        // RECEBER_DE_CADA
        cartas.add(new Carta(TipoCarta.RECEBER_DE_CADA, 50, 11));

        // RECEBER
        cartas.add(new Carta(TipoCarta.RECEBER,         45, 12));
        cartas.add(new Carta(TipoCarta.RECEBER,        100, 13));
        cartas.add(new Carta(TipoCarta.RECEBER,        100, 14));
        cartas.add(new Carta(TipoCarta.RECEBER,         20, 15));

        // PAGAR
        cartas.add(new Carta(TipoCarta.PAGAR,           15, 16));
        cartas.add(new Carta(TipoCarta.PAGAR,           25, 17));
        cartas.add(new Carta(TipoCarta.PAGAR,           45, 18));
        cartas.add(new Carta(TipoCarta.PAGAR,           30, 19));
        cartas.add(new Carta(TipoCarta.PAGAR,          100, 20));
        cartas.add(new Carta(TipoCarta.PAGAR,          100, 21));
        cartas.add(new Carta(TipoCarta.PAGAR,           40, 22));

        // VAI PARA PRISÃO
        cartas.add(new Carta(TipoCarta.VAI_PARA_PRISAO,  0, 23));

        // Embaralha a lista inteira e cria a fila nessa ordem
        Collections.shuffle(cartas);
        for (Carta c : cartas) {
            baralhoSorteReves.offer(c);
        }
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
        baralhoSorteReves.offer(new Carta(TipoCarta.SAIDA_LIVRE, 0, 9));
    }
}