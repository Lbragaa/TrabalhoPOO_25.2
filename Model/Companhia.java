package Model;

class Companhia extends Propriedade {

    protected int multiplicadorAluguel; // Valor usado para calcular o aluguel

    public Companhia(String nome, int preco, int multiplicadorAluguel, int aluguelBase, int posicao) {
        super(nome, preco, aluguelBase, posicao);
        this.multiplicadorAluguel = multiplicadorAluguel;
    }


    // --------- ALUGUEL ---------
    @Override
    public int calculaAluguel() {
        // Aluguel simples baseado em um multiplicador do aluguel base
        return aluguelBase * multiplicadorAluguel;
    }

    public int getMultiplicadorAluguel() {
        return multiplicadorAluguel;
    }

    public void setMultiplicadorAluguel(int multiplicadorAluguel) {
        this.multiplicadorAluguel = multiplicadorAluguel;
    }
}
