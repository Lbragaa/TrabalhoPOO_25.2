package Model;

/**
 * Terreno construível (até 4 casas nesta iteração).
 * <p>Regra de aluguel: com ≥1 casa, cresce em função do nº de casas; sem casa, usa aluguel base.
 * O Motor só cobra terreno com ≥1 casa, então este retorno base é redundante porém inofensivo.</p>
 */
class Terreno extends Propriedade {

    protected int numCasas;
    protected int valorCasa;

    public Terreno(String nome, int preco, int valorCasa, int aluguelBase, int posicao) {
        super(nome, preco, aluguelBase, posicao);
        this.valorCasa = valorCasa;
        this.numCasas = 0;
    }

    // --------- CONSTRUÇÃO ---------
    /** Pode construir até 4 casas (sem hotel nesta iteração). */
    public boolean podeConstruir() { return numCasas < 4; }

    /** Incrementa 1 casa se permitido. */
    public void adicionaCasa() { if (podeConstruir()) numCasas++; }

    public int getNumCasas() { return numCasas; }

    // --------- ALUGUEL ---------
    @Override
    public int calculaAluguel() {
        if (numCasas == 0) return aluguelBase;
        return aluguelBase * numCasas;
    }

    // --------- GETTERS ---------
    public int getValorCasa() { return valorCasa; }
}