package model;

public abstract class Propriedade {

    protected String nome;
    protected Jogador proprietario;
    protected int preco;
    protected int valorCasa;   // Some subclasses might not use this
    protected int aluguelBase;

    public Propriedade(String nome, int preco, int aluguelBase) {
        this.nome = nome;
        this.preco = preco;
        this.aluguelBase = aluguelBase;
        this.proprietario = null;
    }

    // --------- GETTERS E SETTERS ---------
    public String getNome() {
        return nome;
    }

    public Jogador getProprietario() {
        return proprietario;
    }

    public void setProprietario(Jogador proprietario) {
        this.proprietario = proprietario;
    }

    public int getPreco() {
        return preco;
    }

    public int getAluguelBase() {
        return aluguelBase;
    }

    public boolean estaDisponivel() {
        return proprietario == null;
    }

    // --------- REGRAS GERAIS ---------
    public abstract int calculaAluguel();

    public abstract boolean podeConstruir();

    public abstract void adicionaCasa();

    public abstract int getNumCasas();
}
