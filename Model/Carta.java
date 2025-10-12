package Model;

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
    //se a carta for de liberar ou colocar na prisao, o valor é 0
    public String toString() {
        return "Carta{tipo=" + tipo + ", valor=" + valor + "}";
    }
}