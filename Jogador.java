package model;

public class Jogador {

    private String nome;
    private ContaBancaria conta;
    private int posicao;
    private boolean preso;
    private boolean falido;

    public Jogador(String nome) {
        this.nome = nome;
        this.conta = new ContaBancaria(4000); // Cada jogador começa com $4000
        this.posicao = 0;                     // Casa inicial
        this.preso = false;
        this.falido = false;
    }

    // --------- MOVIMENTAÇÃO ---------
    public void move(int casas) {
        int novaPosicao = posicao + casas;

        // Tabuleiro padrão com 40 casas
        if (novaPosicao >= Tabuleiro.getNumCasas()) {
            novaPosicao %= Tabuleiro.getNumCasas();

            // Ao passar pela saída, recebe $200 do banco
            Banco banco = new Banco();
            banco.pagarPara(conta, 200);
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

    // --------- GETTERS E SETTERS ---------
    public ContaBancaria getConta() {
        return conta;
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
    public void receberDoBanco(int valor) {
        Banco banco = new Banco();
        banco.pagarPara(conta, valor);
    }

    public void pagarAoBanco(int valor) {
        Banco banco = new Banco();
        banco.receberPagamento(conta, valor);
    }
}
