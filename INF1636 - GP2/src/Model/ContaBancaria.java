package Model;

/**
 * Conta bancária simples usada por banco e jogadores.
 * <p>Oferece depósito, saque (sem permitir saldo negativo) e transferência via {@link #paga}.</p>
 */
class ContaBancaria {

    private int saldo;

    /** Cria conta com saldo inicial. */
    public ContaBancaria(int saldoInicial) {
        this.saldo = saldoInicial;
    }

    /** Deposita um valor positivo. Valores <= 0 são ignorados. */
    public void depositar(int valor) {
        if (valor > 0) {
            saldo += valor;
        }
    }

    /**
     * Saca um valor se houver saldo suficiente.
     * @return {@code true} se sacou; {@code false} se valor inválido ou saldo insuficiente
     */
    public boolean sacar(int valor) {
        if (valor <= 0) return false;
        if (saldo - valor < 0) {
            return false; // saldo insuficiente
        }
        saldo -= valor;
        return true;
    }

    /**
     * Transfere valor desta conta para {@code destino}.
     * @return {@code true} se a operação foi concluída; {@code false} caso contrário
     */
    public boolean paga(ContaBancaria destino, int valor) {
        if (sacar(valor)) {
            destino.depositar(valor);
            return true;
        }
        return false;
    }

    /** Saldo atual. */
    public int getSaldo() {
        return saldo;
    }

    /**
     * Define o saldo diretamente.
     * <p><strong>Atenção:</strong> considerar validar {@code saldo >= 0} ou restringir visibilidade.</p>
     */
    public void setSaldo(int saldo) {
        this.saldo = saldo; // TODO: validar não-negativo, se desejado
    }
}
