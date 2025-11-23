package Model;

/**
 * Terreno construA-vel (atAc 4 casas, passando entA�o para hotel).
 */
class Terreno extends Propriedade {

    private int numCasas;
    private boolean temHotel;

    public Terreno(String nome, int preco, int valorCasa, int aluguelBase, int posicao) {
        super(nome, preco, aluguelBase, posicao);
        this.numCasas = 0;
        this.temHotel = false;
    }

    // --------- CONSTRUA�A�O ---------

    /** Pode construir enquanto houver espaA�o para casa ou hotel. */
    public boolean podeConstruir() {
        return podeConstruirCasa() || podeConstruirHotel();
    }

    /** Indica se ainda pode construir casa (mA�x. 4 casas). */
    public boolean podeConstruirCasa() { return numCasas < 4; }

    /** Indica se pode construir hotel (precisa de pelo menos 1 casa e nA�o ter hotel). */
    public boolean podeConstruirHotel() { return numCasas >= 1 && !temHotel; }

    /** Adiciona uma casa (nA�o mexe no status de hotel). */
    public void adicionaCasa() {
        if (!podeConstruirCasa()) return;
        numCasas++;
    }

    /** Marca construAA�o de hotel (mantAcm quantidade de casas). */
    public void adicionaHotel() {
        if (!podeConstruirHotel()) return;
        temHotel = true;
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
    public int getValorCasa() { return (int)(preco * 0.50); } // casa custa 50% do preAo
    public int getValorHotel() { return preco; } // hotel custa 100% do preAo
    void resetConstrucoes() { this.numCasas = 0; this.temHotel = false; }
}

