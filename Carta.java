package model;

// (package-private) visível só dentro de model
enum TipoCarta {
    VAI_PARA_PRISAO,
    SAIDA_LIVRE,
    PAGAR,
    RECEBER
}


final class Carta {
    final TipoCarta tipo;
    final int valor; 

    Carta(TipoCarta tipo, int valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    public String toString() {
        return "Carta{tipo=" + tipo + ", valor=" + valor + "}";
    }
}
