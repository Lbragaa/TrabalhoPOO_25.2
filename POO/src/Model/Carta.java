package Model;

/**
 * Tipos de cartas de Sorte/Revés.
 * <ul>
 *   <li>{@link #VAI_PARA_PRISAO}</li>
 *   <li>{@link #SAIDA_LIVRE}</li>
 *   <li>{@link #PAGAR}</li>
 *   <li>{@link #RECEBER}</li>
 *   <li>{@link #RECEBER_DE_CADA}</li>
 * </ul>
 */
enum TipoCarta {
    VAI_PARA_PRISAO,
    SAIDA_LIVRE,
    PAGAR,
    RECEBER,
    RECEBER_DE_CADA
}

/**
 * Representa uma carta de Sorte/Revés.
 * <p>
 * Para cartas de ir/sair da prisão, convenciona-se {@code valor = 0}.
 * Para pagar/receber/receber_de_cada, {@code valor} indica o montante.
 * O campo {@code codigo} é o número 1..30 que bate com a imagem
 * <code>/sorteReves/chance{codigo}.png</code>.
 * </p>
 */
final class Carta {
    /** Tipo da carta. */       final TipoCarta tipo;
    /** Valor associado. */     final int valor;
    /** Código 1..30 da carta. */ final int codigo;

    /**
     * Cria uma carta.
     * @param tipo   tipo da carta
     * @param valor  valor associado (0 para prisão/saída-livre)
     * @param codigo número da carta (1..30) que corresponde ao PNG
     */
    Carta(TipoCarta tipo, int valor, int codigo) {
        this.tipo = tipo;
        this.valor = valor;
        this.codigo = codigo;
    }

    @Override
    public String toString() {
        return "Carta{codigo=" + codigo + ", tipo=" + tipo + ", valor=" + valor + "}";
    }
}
