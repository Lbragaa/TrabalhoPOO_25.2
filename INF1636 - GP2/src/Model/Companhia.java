package Model;

/**
 * Propriedade do tipo Companhia.
 * <p>
 * Regra simplificada de aluguel: {@code aluguel = aluguelBase * multiplicadorAluguel}.
 * </p>
 */
class Companhia extends Propriedade {

    /** Fator multiplicador aplicado sobre o aluguel base. */
    protected int multiplicadorAluguel;

    /**
     * Constrói uma Companhia.
     * @param nome nome da propriedade
     * @param preco preço de compra
     * @param multiplicadorAluguel fator usado no cálculo do aluguel
     * @param aluguelBase valor base de aluguel
     * @param posicao posição no tabuleiro (0..39)
     */
    public Companhia(String nome, int preco, int multiplicadorAluguel, int aluguelBase, int posicao) {
        super(nome, preco, aluguelBase, posicao);
        this.multiplicadorAluguel = multiplicadorAluguel;
    }

    // --------- ALUGUEL ---------

    /**
     * Calcula o aluguel da companhia.
     * @return aluguel = aluguelBase * multiplicadorAluguel
     */
    @Override
    public int calculaAluguel() {
        return aluguelBase * multiplicadorAluguel;
    }

    /** Retorna o multiplicador de aluguel. */
    public int getMultiplicadorAluguel() {
        return multiplicadorAluguel;
    }

    /**
     * Define o multiplicador de aluguel.
     * @param multiplicadorAluguel novo multiplicador (idealmente &ge; 1)
     */
    public void setMultiplicadorAluguel(int multiplicadorAluguel) {
        this.multiplicadorAluguel = multiplicadorAluguel;
    }
}