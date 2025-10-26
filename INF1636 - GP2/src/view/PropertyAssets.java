// package view;

// import java.util.HashMap;
// import java.util.Map;

// /**
//  * Resolve o caminho da imagem da "carta" para uma dada casa do tabuleiro.
//  * Primeiro olha um override local; se não houver, delega ao {@link CardResolver}.
//  */
// public final class PropertyAssets {
//     private static final Map<Integer, String> MAP = new HashMap<>();

//     static {
//         // Overrides locais (preencha só o que divergir do CardResolver)
//         // Ex.: MAP.put(31, "/territorios/Av. 9 de Julho.png");
//     }

//     private PropertyAssets(){}

//     /** Caminho no classpath para a imagem da carta (ou null se não houver). */
//     public static String cardPathForCell(int cellIndex) {
//         String p = MAP.get(cellIndex);
//         return (p != null ? p : CardResolver.propertyCardPath(cellIndex));
//     }
// }
