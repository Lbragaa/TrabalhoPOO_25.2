package Model;

enum TipoCarta {
    VAI_PARA_PRISAO,
    SAIDA_LIVRE,
    PAGAR,
    RECEBER
}

final class Carta {
    final TipoCarta tipo;
    final int valor; // se a carta for de liberar/ir à prisão, valor = 0

    Carta(TipoCarta tipo, int valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "Carta{tipo=" + tipo + ", valor=" + valor + "}";
    }
}