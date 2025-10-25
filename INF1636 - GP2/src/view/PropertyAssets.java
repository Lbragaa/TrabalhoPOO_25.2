package view;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolve o caminho da imagem da "carta" para uma dada casa do tabuleiro.
 * Nesta etapa, mantemos um mapa simples. Se a casa não tiver carta mapeada,
 * retorna null e o painel mostra "Sem carta".
 *
 * ⚠️ Preencha/ajuste os caminhos conforme seus arquivos em /territorios e /companhias.
 */
public final class PropertyAssets {
    private static final Map<Integer, String> MAP = new HashMap<>();

    static {
        // EXEMPLOS — ajuste conforme suas imagens reais (nomes exatos de arquivo)
        // Use caminhos absolutos no classpath, ex.: "/territorios/Av. 9 de Julho.png"
        MAP.put(31, "/territorios/Av. 9 de Julho.png");
        MAP.put(32, "/territorios/Av. Rebouças.png");
        MAP.put(33, "/territorios/Av. Brigadeiro Faria Lima.png");
        MAP.put(34, "/territorios/Av. Presidente Vargas.png");
        MAP.put(35, "/territorios/Leblon.png");
        MAP.put(1,  "/territorios/Leblon.png"); // exemplo
        // Companhias (se tiverem cartas separadas):
        // MAP.put(?, "/companhias/company1.png");
        // MAP.put(?, "/companhias/company2.png");

        // ➜ Dica: preencha aos poucos conforme for testando cada casa.
    }

    private PropertyAssets(){}

    /** Caminho no classpath para a imagem da carta (ou null se não houver). */
    public static String cardPathForCell(int cellIndex) {
        return MAP.get(cellIndex);
    }
}
