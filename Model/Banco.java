package Model;

/**
 * Representa o banco central do jogo Banco Imobiliário.
 * Mantém a conta do banco e facilita transações entre jogadores e o banco.
 */
class Banco {

    protected ContaBancaria conta;

    public Banco() {
        // O banco começa com $200.000
        this.conta = new ContaBancaria(200000);
    }

    /**
     * Retorna a conta bancária do banco.
     * @return Conta do banco.
     */
    public ContaBancaria getConta() {
        return conta;
    }

    /**
     * Reabastece o banco, resetando seu saldo para $200.000.
     */
    public void abastece() {
        // Reabastece o banco caso seja necessário
        this.conta = new ContaBancaria(200000);
    }

   /**
     * Recebe pagamento de uma conta de origem para o banco.
     * @param origem Conta do jogador que está pagando.
     * @param valor Valor a ser pago.
     * @return true se o pagamento foi realizado com sucesso, false caso contrário.
     */
    public boolean receberPagamento(ContaBancaria origem, int valor) {
        return origem.paga(this.conta, valor);
    }

    /**
     * Paga um valor da conta do banco para uma conta de destino.
     * @param destino Conta que receberá o valor.
     * @param valor Valor a ser pago.
     * @return true se o pagamento foi realizado com sucesso, false caso o valor seja inválido ou insuficiente.
     */
    public boolean pagarPara(ContaBancaria destino, int valor) {
        if (valor <= 0) return false;
        return this.conta.paga(destino, valor);
    }

    /**
     * Retorna o saldo atual do banco.
     * @return Saldo da conta do banco.
     */
    public int getSaldo() {
        return conta.getSaldo();
    }
}
