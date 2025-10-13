package Model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
    public static int getNumCasas() {
        return NUM_CASAS;
    }

    public static int getPosicaoPrisao() { //posicao quando o jogador tem que ir para prisao
        return POSICAO_VAI_PRA_PRISAO;
    }
    
    public static int getPosicaoVisitaPrisao() {
    	return POSICAO_PRISAO;      //posicao de destino do jogador quando ele é preso
    }
    public boolean isCasaPrisao(int posicao) { // verifica se é a casa que ele é mandado pra prisao
        return posicao == POSICAO_VAI_PRA_PRISAO;
    }
    
    //---------------------MÉTODOS AUXILIARES PRA TESTE--------------------------
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
    public void addPropriedade(Propriedade p) {
        propriedades.add(p);
    }

    public List<Propriedade> getPropriedades() {
        return propriedades;
    }

    public void limparPropriedadesDe(Jogador jogador) {
        for (Propriedade p : propriedades) {
            if (p.getProprietario() == jogador) {
                p.setProprietario(null);
            }
        }
    }

    // ---------- JOGADORES ----------
    public void addJogador(Jogador jogador) {
        jogadoresAtivos.add(jogador);
    }

    public void removerJogador(Jogador jogador) {
        jogadoresAtivos.remove(jogador);
    }

    public List<Jogador> getJogadoresAtivos() {
        return jogadoresAtivos;
    }
    
    public Propriedade getPropriedadeNaPosicao(int posicao) {
        for (Propriedade p : propriedades) {
            if (p.getPosicao() == posicao) {
                return p;
            }
        }
        return null;
    }

    public boolean estaNoJogo(Jogador jogador) {
        return jogadoresAtivos.contains(jogador);
    }
    //--------------- CARTAS -------------
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
    //Quando o jogador usa a carta ele tem que devolver pro final da fila, isso acontece la na ação quando ele ta preso e usa a carta pra sair
    public void devolverCartaLiberacao() {
    baralhoSorteReves.offer(new Carta(TipoCarta.SAIDA_LIVRE, 0));
    }
}













