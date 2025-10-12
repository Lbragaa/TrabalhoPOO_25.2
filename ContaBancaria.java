package model;

class ContaBancaria {

    private int saldo;

    public ContaBancaria(int saldoInicial) {
        this.saldo = saldoInicial;
    }

    public void depositar(int valor) {
        if (valor > 0) {
            saldo += valor;
        }
    }

    public boolean sacar(int valor) {
        if (valor <= 0) return false;
        if (saldo - valor < 0) {
            return false; // saldo insuficiente
        }
        saldo -= valor;
        return true;
    }

    public boolean paga(ContaBancaria destino, int valor) {
        if (sacar(valor)) {
            destino.depositar(valor);
            return true;
        }
        return false;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }
}
