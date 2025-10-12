package model;

public class Terreno extends Propriedade {

    private int numCasas;

    public Terreno(String nome, int preco, int valorCasa, int aluguelBase) {
        // Chama o construtor da classe-mãe Propriedade
        super(nome, preco, aluguelBase);
        this.valorCasa = valorCasa;
        this.numCasas = 0;
    }

    // --------- CONSTRUÇÃO ---------
    @Override
    public boolean podeConstruir() {
        // Pode construir até 4 casas (sem hotel nesta iteração)
        return numCasas < 4;
    }

    @Override
    public void adicionaCasa() {
        if (podeConstruir()) {
            numCasas++;
        }
    }

    @Override
    public int getNumCasas() {
        return numCasas;
    }

    // --------- ALUGUEL ---------
    @Override
    public int calculaAluguel() {
        // Aluguel base vezes número de casas (mínimo 1)
        if (numCasas == 0) return aluguelBase;
        return aluguelBase * numCasas;
    }

    // --------- GETTERS ---------
    public int getValorCasa() {
        return valorCasa;
    }
}
