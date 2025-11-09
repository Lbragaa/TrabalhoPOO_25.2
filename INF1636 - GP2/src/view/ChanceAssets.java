package view;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolve caminho da imagem de carta (Sorte/Revés) por ID lógico.
 * <p>Use {@link #imagePathForCardId(String)} e adicione novos mapeamentos no bloco estático.</p>
 */
public final class ChanceAssets {
    private static final Map<String,String> MAP = new HashMap<>();
    static {
        // IDs ↔ arquivos em /sorteReves/
        MAP.put("VAI_PARA_PRISAO_1", "/sorteReves/vai_pra_prisao.png");
        MAP.put("SAIDA_LIVRE_1",     "/sorteReves/saida_livre.png");
        MAP.put("PAGAR_100_1",       "/sorteReves/pagar_100.png");
        MAP.put("RECEBER_200_1",     "/sorteReves/receber_200.png");
        // adicione/ajuste demais IDs conforme seu baralho real
    }
    private ChanceAssets(){}

    /** Caminho do recurso (ou {@code null} se não mapeado). */
    public static String imagePathForCardId(String cardId) { return MAP.get(cardId); }
}