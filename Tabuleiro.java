package model;

public class Tabuleiro {

    // Número padrão de casas no Banco Imobiliário
    private static final int NUM_CASAS = 40;

    // Posição da prisão (geralmente casa 10)
    private static final int POSICAO_PRISAO = 10;

    public Tabuleiro() {
        // Nenhum atributo complexo ainda — sem necessidade de casas detalhadas nesta iteração
    }

    public static int getNumCasas() {
        return NUM_CASAS;
    }

    public static int getPosicaoPrisao() {
        return POSICAO_PRISAO;
    }

    // Verifica se uma posição é a prisão
    public boolean isCasaPrisao(int posicao) {
        return posicao == POSICAO_PRISAO;
    }
}
