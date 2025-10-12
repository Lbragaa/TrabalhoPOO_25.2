package Model;

class Propriedade {

	protected String nome;
	protected Jogador proprietario;
    protected int preco;
    protected int aluguelBase;
    protected int posicao;

    public Propriedade(String nome, int preco, int aluguelBase, int posicao) {
        this.nome = nome;
        this.preco = preco;
        this.aluguelBase = aluguelBase;
        this.proprietario = null;
        this.posicao = posicao;
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

    public int getPosicao() {
    return posicao;
    }

    public void setPosicao(int posicao) {
    this.posicao = posicao;
    }

	public int calculaAluguel() {
		return aluguelBase;
	}
}