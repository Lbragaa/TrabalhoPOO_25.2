package Model;

/**
 * Terreno construível (até 4 casas, passando então para hotel).
 */
class Terreno extends Propriedade {

    private int numCasas;
    private boolean temHotel;

    public Terreno(String nome, int preco, int valorCasa, int aluguelBase, int posicao) {
        super(nome, preco, aluguelBase, posicao);
        this.numCasas = 0;
        this.temHotel = false;
    }

    // --------- CONSTRUÇÃO ---------

    /** Pode construir enquanto ainda não houver hotel (máx. 4 casas + 1 hotel). */
    public boolean podeConstruir() {
        return !temHotel && numCasas <= 4;
    }

    /** Adiciona construção: até 4 casas; após isso, vira hotel sem zerar casas. */
    public void adicionaCasa() {
        if (!podeConstruir()) return;

        if (numCasas < 4) {
            // constrói mais uma casa
            numCasas++;
        } else if (numCasas == 4) {
            // já tem 4 casas, agora adiciona hotel (continua com 4 casas)
            temHotel = true;
        }
    }

    public int getNumCasas()   { return numCasas; }
    public boolean temHotel()  { return temHotel; }

    // --------- ALUGUEL (segundo regra do professor) ---------

    @Override
    public int calculaAluguel() {
        double preco = this.preco;

        double Vb = preco * 0.10; // 10%
        double Vc = preco * 0.15; // 15% por casa
        double Vh = temHotel ? preco * 0.30 : 0; // 30% se tiver hotel

        return (int)(Vb + Vc * numCasas + Vh);
    }

    // --------- GETTERS ---------
    public int getValorCasa() { return (int)(preco * 0.50); } // casa custa 50% do preço
    public int getValorHotel() { return preco; } // hotel custa 100% do preço
    void resetConstrucoes() { this.numCasas = 0; this.temHotel = false; }
}
