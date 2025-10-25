package Model;

class Terreno extends Propriedade {

	protected int numCasas;
	protected int ValorCasa;

    public Terreno(String nome, int preco, int ValorCasa, int aluguelBase, int posicao) {
        super(nome, preco, aluguelBase, posicao);
        this.ValorCasa = ValorCasa;
        this.numCasas = 0;
    }

    // --------- CONSTRUÇÃO ---------
    public boolean podeConstruir() {
        // Pode construir até 4 casas (sem hotel nesta iteração)
        return numCasas < 4;
    }

   
    public void adicionaCasa() {
        if (podeConstruir()) {
            numCasas++;
        }
    }

    public int getNumCasas() {
        return numCasas;
    }

    // --------- ALUGUEL ---------
    @Override
    public int calculaAluguel() {
        if (numCasas == 0) return aluguelBase;
        return aluguelBase * numCasas;
    }

    // --------- GETTERS ---------
    public int getValorCasa() {
        return ValorCasa;
    }
}