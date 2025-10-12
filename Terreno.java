package model;

public class Terreno {

    private String nome;
    private Jogador proprietario;
    private int preco;
    private int valorCasa;
    private int numCasas;
    private int aluguelBase;

    public Terreno(String nome, int preco, int valorCasa, int aluguelBase) {
        this.nome = nome;
        this.preco = preco;
        this.valorCasa = valorCasa;
        this.aluguelBase = aluguelBase;
        this.numCasas = 0;
        this.proprietario = null;
    }

    // --------- PROPRIEDADE ---------
    public Jogador getProprietario() {
        return proprietario;
    }

    public void setProprietario(Jogador proprietario) {
        this.proprietario = proprietario;
    }

    public boolean estaDisponivel() {
        return proprietario == null;
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
    public int calculaAluguel() {
        // Regra simples: aluguel base * número de casas (mínimo 1)
        if (numCasas == 0) return aluguelBase;
        return aluguelBase * numCasas;
    }

    // --------- GETTERS ---------
    public String getNome() {
        return nome;
    }

    public int getPreco() {
        return preco;
    }

    public int getValorCasa() {
        return valorCasa;
    }
}
