package model;

class Jogador {

    private String nome;
    private ContaBancaria conta;
    private int posicao;
    private boolean preso;
    private boolean falido;
    private int cartasLiberacao;
    public Jogador(String nome) {
        this.nome = nome;
        this.conta = new ContaBancaria(4000); // Cada jogador começa com $4000
        this.posicao = 0;                     // Casa inicial
        this.preso = false;
        this.falido = false;
        this.cartasLiberacao = 0;
    }

    // --------- MOVIMENTAÇÃO ---------
public void move(int casas, Banco banco) {
    int novaPosicao = posicao + casas;

    if (novaPosicao >= Tabuleiro.getNumCasas()) {
        novaPosicao %= Tabuleiro.getNumCasas();
        banco.pagarPara(conta, 200); // shared banco, nao fica criando toda hora um banco novo
    }

    this.posicao = novaPosicao;
}

    // --------- PRISÃO ---------
    public void prende() {
        this.preso = true;
        this.posicao = Tabuleiro.getPosicaoPrisao();
    }

    public void solta() {
        this.preso = false;
    }
    void adicionarCartaLiberacao() {
        this.cartasLiberacao++;
    }
    
    // --------- GETTERS E SETTERS ---------
    public ContaBancaria getConta() {
        return conta;
    }
    public int getCartasLiberacao() {
        return this.cartasLiberacao;
    }
    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
    }

    public boolean estaPreso() {
        return preso;
    }

    public void setFalido(boolean falido) {
        this.falido = falido;
    }

    public boolean isFalido() {
        return falido;
    }

    public String getNome() {
        return nome;
    }

    // --------- OUTROS ---------

    // Antes tava toda hora criando um banco novo. Desse jeito, o metodo de jogador espera que exista algum banco que ira pagar ou receber.
    // Tem que considerar tambem que esses metodos nao estao sendo usados. A maioria das coisas esta sendo feita com os metodos de conta, ou de banco, tudo controlado pelo Acoes.
    public void pagarAoBanco(Banco banco, int valor) {
    banco.receberPagamento(this, valor);
}

public void receberDoBanco(Banco banco, int valor) {
    banco.pagarPara(conta, valor);
}

     /**
     * Consome 1 carta de liberação, se houver.
     * @return true se consumiu; false se não tinha.
     */
    boolean consumirCartaLiberacao() {
        if (this.cartasLiberacao > 0) {
            this.cartasLiberacao--;
            return true;
        }
        return false;
    }
}

