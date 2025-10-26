package Model;

/**
 * Representa um jogador: nome, conta, posição, status de prisão/falência e cartas de liberação.
 * <p>Regra: ao passar pela saída recebe $200 do banco (tratado em {@link #move}).</p>
 */
class Jogador {

    private String nome;
    private ContaBancaria conta;
    private int posicao;              // 0..39 (o chamador deve normalizar, se necessário)
    private boolean preso;
    private boolean falido;
    private int cartasLiberacao;

    /** Cria jogador com $4000, posição 0, livre e sem cartas. */
    public Jogador(String nome) {
        this.nome = nome;
        this.conta = new ContaBancaria(4000);
        this.posicao = 0;
        this.preso = false;
        this.falido = false;
        this.cartasLiberacao = 0;
    }

    // --------- MOVIMENTAÇÃO ---------

    /**
     * Move o jogador somando {@code casas}. Se ultrapassar o final do tabuleiro,
     * normaliza e recebe $200 do banco.
     * @param casas número de casas a andar (>=0)
     * @param banco banco compartilhado do jogo
     */
    public void move(int casas, Banco banco) {
        int novaPosicao = posicao + casas;
        if (novaPosicao >= Tabuleiro.getNumCasas()) {
            novaPosicao %= Tabuleiro.getNumCasas();
            banco.pagarPara(conta, 200);
        }
        this.posicao = novaPosicao;
    }

    // --------- PRISÃO ---------

    /** Coloca o jogador na prisão e move para a casa de prisão/visita. */
    public void prende() {
        this.preso = true;
        this.posicao = Tabuleiro.getPosicaoVisitaPrisao();
    }

    /** Libera o jogador da prisão (não move). */
    public void solta() { this.preso = false; }

    /** Adiciona uma carta de liberação. */
    void adicionarCartaLiberacao() { this.cartasLiberacao++; }

    // --------- GETTERS E SETTERS ---------

    public ContaBancaria getConta() { return conta; }
    public int getCartasLiberacao() { return this.cartasLiberacao; }
    public int getPosicao() { return posicao; }
    public void setPosicao(int posicao) { this.posicao = posicao; } // assumimos normalização externa
    public boolean estaPreso() { return preso; }
    public void setFalido(boolean falido) { this.falido = falido; }
    public boolean isFalido() { return falido; }
    public String getNome() { return nome; }

    // --------- OUTROS ---------

    /** Paga valor ao banco. */
    public void pagarAoBanco(Banco banco, int valor) { banco.receberPagamento(this.conta, valor); }

    /** Recebe valor do banco. */
    public void receberDoBanco(Banco banco, int valor) { banco.pagarPara(conta, valor); }

    /**
     * Consome carta de liberação, se houver.
     * @return {@code true} se consumiu; {@code false} caso contrário
     */
    boolean consumirCartaLiberacao() {
        if (this.cartasLiberacao > 0) { this.cartasLiberacao--; return true; }
        return false;
    }
}
