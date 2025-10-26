package Model;

/**
 * Tipos de cartas de Sorte/Revés.
 * <ul>
 *   <li>{@link #VAI_PARA_PRISAO}</li>
 *   <li>{@link #SAIDA_LIVRE}</li>
 *   <li>{@link #PAGAR}</li>
 *   <li>{@link #RECEBER}</li>
 * </ul>
 */
enum TipoCarta {
    VAI_PARA_PRISAO,
    SAIDA_LIVRE,
    PAGAR,
    RECEBER
}

/**
 * Representa uma carta de Sorte/Revés.
 * <p>
 * Para cartas de ir/ sair da prisão, convenciona-se {@code valor = 0}.
 * Para cartas de pagar/receber, {@code valor} indica o montante.
 * </p>
 */
final class Carta {
    /** Tipo da carta. */
    final TipoCarta tipo;
    /** Valor associado (0 para cartas de prisão/saída-livre). */
    final int valor;

    /**
     * Cria uma carta.
     * @param tipo tipo da carta
     * @param valor valor associado (0 para prisão/saída-livre)
     */
    Carta(TipoCarta tipo, int valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "Carta{tipo=" + tipo + ", valor=" + valor + "}";
    }
}
