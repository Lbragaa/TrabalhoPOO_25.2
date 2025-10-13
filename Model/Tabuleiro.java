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

    // Número padrão de casas no Banco Imobiliário
    private static final int NUM_CASAS = 40;
    // Posição da prisão (geralmente casa 10)
    private static final int POSICAO_VAI_PRA_PRISAO = 26;
    private static final int POSICAO_PRISAO = 10;

    // Lista de propriedades existentes no tabuleiro
    protected List<Propriedade> propriedades;

    // Lista de jogadores ativos na partida
    protected List<Jogador> jogadoresAtivos;

    // Lista de baralho usando FILA, sempre compra no início e coloca no fim do baralhp
    protected final Queue<Carta> baralhoSorteReves;
    
    // ---------- CONSTRUTOR ----------
    public Tabuleiro() {
        this.propriedades = new ArrayList<>();
        this.jogadoresAtivos = new ArrayList<>();
        this.baralhoSorteReves = new LinkedList<>();
    }

    // ---------- MÉTODOS ESTÁTICOS ----------
    /**
     * Retorna o número total de casas no tabuleiro.
     * @return Número de casas.
     */
    public static int getNumCasas() {
        return NUM_CASAS;
    }

    /**
     * Retorna a posição da casa que envia o jogador para a prisão.
     * @return Posição "Vá para a Prisão".
     */
    public static int getPosicaoPrisao() { //posicao quando o jogador tem que ir para prisao
        return POSICAO_VAI_PRA_PRISAO;
    }

    /**
     * Retorna a posição da prisão onde o jogador ficará preso.
     * @return Posição da prisão.
     */
    public static int getPosicaoVisitaPrisao() {
    	return POSICAO_PRISAO;      //posicao de destino do jogador quando ele é preso
    }

    /**
     * Verifica se a posição fornecida corresponde à casa "Vá para a Prisão".
     * @param posicao Posição a ser verificada.
     * @return true se for a casa de prisão, false caso contrário.
     */
    public boolean isCasaPrisao(int posicao) { // verifica se é a casa que ele é mandado pra prisao
        return posicao == POSICAO_VAI_PRA_PRISAO;
    }
    
    //---------------------MÉTODOS AUXILIARES PRA TESTE--------------------------

    /**
     * Inicializa o baralho de Sorte/Revés com cartas de teste simples.
     */
    public void inicializarBaralhoTeste() {
        if (baralhoSorteReves == null) return;

        baralhoSorteReves.clear();

        // Cartas simples apenas para testar comportamentos básicos
        baralhoSorteReves.offer(new Carta(TipoCarta.VAI_PARA_PRISAO, 0));
        baralhoSorteReves.offer(new Carta(TipoCarta.SAIDA_LIVRE, 0));
        baralhoSorteReves.offer(new Carta(TipoCarta.PAGAR, 100));
        baralhoSorteReves.offer(new Carta(TipoCarta.RECEBER, 200));
    }

    // ---------- PROPRIEDADES ----------
    /**
     * Adiciona uma propriedade ao tabuleiro.
     * @param p Propriedade a ser adicionada.
     */
    public void addPropriedade(Propriedade p) {
        propriedades.add(p);
    }

    /**
     * Retorna a lista de propriedades do tabuleiro.
     * @return Lista de propriedades.
     */
    public List<Propriedade> getPropriedades() {
        return propriedades;
    }

    /**
     * Libera todas as propriedades pertencentes a um jogador (usado em caso de falência).
     * @param jogador Jogador cujas propriedades serão liberadas.
     */
    public void limparPropriedadesDe(Jogador jogador) {
        for (Propriedade p : propriedades) {
            if (p.getProprietario() == jogador) {
                p.setProprietario(null);
            }
        }
    }

    // ---------- JOGADORES ----------
    /**
     * Adiciona um jogador ativo ao tabuleiro.
     * @param jogador Jogador a ser adicionado.
     */
    public void addJogador(Jogador jogador) {
        jogadoresAtivos.add(jogador);
    }

    /**
     * Remove um jogador do tabuleiro.
     * @param jogador Jogador a ser removido.
     */
    public void removerJogador(Jogador jogador) {
        jogadoresAtivos.remove(jogador);
    }

    /**
     * Retorna a lista de jogadores ativos no tabuleiro.
     * @return Lista de jogadores.
     */
    public List<Jogador> getJogadoresAtivos() {
        return jogadoresAtivos;
    }

    /**
     * Retorna a propriedade que ocupa uma determinada posição no tabuleiro.
     * @param posicao Posição a ser consultada.
     * @return Propriedade na posição, ou null se não houver.
     */
    public Propriedade getPropriedadeNaPosicao(int posicao) {
        for (Propriedade p : propriedades) {
            if (p.getPosicao() == posicao) {
                return p;
            }
        }
        return null;
    }

    /**
     * Verifica se um jogador está ativo no jogo.
     * @param jogador Jogador a ser verificado.
     * @return true se o jogador estiver no jogo, false caso contrário.
     */
    public boolean estaNoJogo(Jogador jogador) {
        return jogadoresAtivos.contains(jogador);
    }
    //--------------- CARTAS -------------

    /**
     * Compra uma carta do baralho de Sorte/Revés.
     * As cartas de liberação ("SAIDA_LIVRE") não retornam ao baralho imediatamente.
     * @return Carta comprada.
     * @throws IllegalStateException se o baralho estiver vazio.
     */
    public Carta comprarCartaSorteReves() {
        Carta c = baralhoSorteReves.poll();
        if (c == null) throw new IllegalStateException("Baralho de Sorte/Revés vazio.");

        if (c.tipo == TipoCarta.SAIDA_LIVRE) {
            // jogador guarda a carta (contador +1) e ELA NÃO VOLTA ao deck agora
            // não faz offer(c)
        } else {
            // cartas normais continuam no ciclo
            baralhoSorteReves.offer(c);
        }
        return c;
    }

    /**
     * Devolve uma carta de liberação da prisão ao final do baralho.
     * Usado quando o jogador utiliza a carta para sair da prisão.
     */
    public void devolverCartaLiberacao() {
    baralhoSorteReves.offer(new Carta(TipoCarta.SAIDA_LIVRE, 0));
    }
}













