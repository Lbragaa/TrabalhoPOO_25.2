package model;

public class Banco {

    private ContaBancaria conta;

    public Banco() {
        // O banco começa com $200.000
        this.conta = new ContaBancaria(200000);
    }

    public ContaBancaria getConta() {
        return conta;
    }

    public void abastece() {
        // Reabastece o banco caso seja necessário
        this.conta = new ContaBancaria(200000);
    }

    // Facilita o pagamento para o banco
    public boolean receberPagamento(ContaBancaria origem, int valor) {
        return origem.paga(this.conta, valor);
    }

    // Facilita o pagamento feito pelo banco
    public boolean pagarPara(ContaBancaria destino, int valor) {
        return this.conta.paga(destino, valor);
    }

    public int getSaldo() {
        return conta.getSaldo();
    }
}
