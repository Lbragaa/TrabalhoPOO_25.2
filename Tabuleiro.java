package model;

import java.util.ArrayList;
import java.util.List;

class Tabuleiro {

    // Número padrão de casas no Banco Imobiliário
    private static final int NUM_CASAS = 40;
    // Posição da prisão (geralmente casa 10)
    private static final int POSICAO_PRISAO = 10;

    // Lista de propriedades existentes no tabuleiro
    private List<Propriedade> propriedades;

    // Lista de jogadores ativos na partida
    private List<Jogador> jogadoresAtivos;

    // Lista de baralho usando FILA, sempre compra no início e coloca no fim do baralhp
    private final Queue<Carta> baralhoSorteReves = new LinkedList<>();
    
    // ---------- CONSTRUTOR ----------
    public Tabuleiro() {
        this.propriedades = new ArrayList<>();
        this.jogadoresAtivos = new ArrayList<>();
    }

    // ---------- MÉTODOS ESTÁTICOS ----------
    public static int getNumCasas() {
        return NUM_CASAS;
    }

    public static int getPosicaoPrisao() {
        return POSICAO_PRISAO;
    }

    public boolean isCasaPrisao(int posicao) {
        return posicao == POSICAO_PRISAO;
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
    public Propriedade getPropriedadeNaPosicao(int pos) {
        for (Propriedade p : propriedades) {
            if (p != null && p.getPosicao() == pos) {
                return p;
            }
        }
        return null;
    }
    public boolean estaNoJogo(Jogador jogador) {
        return jogadoresAtivos.contains(jogador);
    }
}


