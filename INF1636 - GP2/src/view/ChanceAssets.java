package view;

import java.util.HashMap;
import java.util.Map;

/** Resolve imagem da carta por ID (tempo O(1)). */
public final class ChanceAssets {
    private static final Map<String,String> MAP = new HashMap<>();
    static {
        // mapeie seus IDs reais para os PNGs em /sorteReves/
        MAP.put("VAI_PARA_PRISAO_1", "/sorteReves/vai_pra_prisao.png");
        MAP.put("SAIDA_LIVRE_1",     "/sorteReves/saida_livre.png");
        MAP.put("PAGAR_100_1",       "/sorteReves/pagar_100.png");
        MAP.put("RECEBER_200_1",     "/sorteReves/receber_200.png");
        // adicione/ajuste aqui seus demais IDs ↔ imagens
    }
    private ChanceAssets(){}

    public static String imagePathForCardId(String cardId) { return MAP.get(cardId); }
}
