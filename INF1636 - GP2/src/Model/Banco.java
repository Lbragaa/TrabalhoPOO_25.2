package Model;

/**
 * Representa o banco central do jogo.
 * <p>
 * O banco mantém sua própria {@link ContaBancaria} e realiza transferências
 * para/de jogadores conforme as regras do jogo.
 * </p>
 * <strong>Atenção:</strong> o método {@link #abastece()} recria a conta. Se houver
 * código que guarde a referência retornada por {@link #getConta()}, essa referência
 * ficará desatualizada. Preferir (se existir) repor o saldo na mesma instância.
 */
class Banco {

    /** Conta interna do banco (saldo inicial de $200.000). */
    protected ContaBancaria conta;

    /** Cria o banco com saldo inicial de $200.000. */
    public Banco() {
        this.conta = new ContaBancaria(200000);
    }

    /**
     * Retorna a conta bancária do banco.
     * @return conta do banco
     */
    public ContaBancaria getConta() {
        return conta;
    }

    /**
     * Reabastece o banco para $200.000.
     * <p>Implementação atual recria a conta (ver observação na classe).</p>
     */
    public void abastece() {
        this.conta = new ContaBancaria(200000);
    }

    /**
     * Recebe pagamento de um jogador para o banco.
     * @param origem conta do jogador pagante
     * @param valor valor a transferir
     * @return {@code true} se transferiu; {@code false} caso contrário
     */
    public boolean receberPagamento(ContaBancaria origem, int valor) {
        return origem.paga(this.conta, valor);
    }

    /**
     * Paga um valor do banco para uma conta de destino.
     * @param destino conta que receberá
     * @param valor valor a transferir (deve ser &gt; 0)
     * @return {@code true} se transferiu; {@code false} se valor inválido/insuficiente
     */
    public boolean pagarPara(ContaBancaria destino, int valor) {
        if (valor <= 0) return false;
        return this.conta.paga(destino, valor);
    }

    /**
     * Retorna o saldo atual do banco.
     * @return saldo do banco
     */
    public int getSaldo() {
        return conta.getSaldo();
    }
}