package model;

class Companhia extends Propriedade {

    private int multiplicadorAluguel; // Valor usado para calcular o aluguel

    public Companhia(String nome, int preco, int aluguelBase, int multiplicadorAluguel) {
        super(nome, preco, aluguelBase);
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

