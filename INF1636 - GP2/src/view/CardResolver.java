package view;

import java.util.*;

/**
 * Resolve imagens de cartas para a casa onde o peão parou.
 * - Sorte/Revés: retorna uma chance aleatória de /sorteReves/chance{1..30}.png
 * - Propriedades/Companhias: usa um MAP de índice->caminho (nomes EXATOS em resources)
 */
public final class CardResolver {

    // Índices de Sorte/Revés (ajuste se seu tabuleiro usar outros)
    private static final Set<Integer> CHANCE_CELLS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(2, 12, 16, 22, 27, 37))
    );

    // Caminhos possíveis para cartas de Sorte/Revés
    private static final List<String> CHANCE_PATHS = makeChancePaths();

    // Mapa índice->caminho para cartas de propriedade/companhia (nomes EXATOS)
    private static final Map<Integer, String> PROPERTY_MAP = new LinkedHashMap<>();
    static {
        // Territórios
        PROPERTY_MAP.put(1,  "/territorios/Leblon.png");
        PROPERTY_MAP.put(3,  "/territorios/Av. Presidente Vargas.png");
        PROPERTY_MAP.put(4,  "/territorios/Av. Nossa S. de Copacabana.png");
        PROPERTY_MAP.put(6,  "/territorios/Av. Brigadeiro Faria Lima.png");
        PROPERTY_MAP.put(8,  "/territorios/Av. Rebouças.png");
        PROPERTY_MAP.put(9,  "/territorios/Av. 9 de Julho.png");
        PROPERTY_MAP.put(11, "/territorios/Av. Europa.png");
        PROPERTY_MAP.put(13, "/territorios/Rua Augusta.png");
        PROPERTY_MAP.put(14, "/territorios/Av. Pacaembú.png");
        PROPERTY_MAP.put(17, "/territorios/Interlagos.png");
        PROPERTY_MAP.put(19, "/territorios/Morumbi.png");
        // (RJ/SP meio do tabuleiro — inclua somente se existirem no Model)
        PROPERTY_MAP.put(21, "/territorios/Flamengo.png");
        PROPERTY_MAP.put(23, "/territorios/Botafogo.png"); // (dedup: só uma vez)
        PROPERTY_MAP.put(26, "/territorios/Av. Brasil.png");
        PROPERTY_MAP.put(28, "/territorios/Av. Paulista.png");
        PROPERTY_MAP.put(29, "/territorios/Jardim Europa.png");

        PROPERTY_MAP.put(31, "/territorios/Copacabana.png");
        PROPERTY_MAP.put(33, "/territorios/Av. Vieira Souto.png");
        PROPERTY_MAP.put(34, "/territorios/Av. Atlântica.png");
        PROPERTY_MAP.put(36, "/territorios/Ipanema.png");
        PROPERTY_MAP.put(38, "/territorios/Jardim Paulista.png");
        PROPERTY_MAP.put(39, "/territorios/Brooklin.png");

        // Companhias/transportes
        PROPERTY_MAP.put(5,  "/companhias/company1.png");
        PROPERTY_MAP.put(7,  "/companhias/company2.png");
        PROPERTY_MAP.put(15, "/companhias/company3.png");
        PROPERTY_MAP.put(25, "/companhias/company4.png");
        PROPERTY_MAP.put(32, "/companhias/company5.png");
        PROPERTY_MAP.put(35, "/companhias/company6.png");
    }

    private CardResolver(){}

    /** Verdadeiro se a casa é de Sorte/Revés. */
    public static boolean isChanceCell(int cellIndex) {
        return CHANCE_CELLS.contains(normalize(cellIndex));
    }

    /** Caminho para uma carta aleatória de Sorte/Revés. */
    public static String randomChanceCardPath() {
        return CHANCE_PATHS.get(new Random().nextInt(CHANCE_PATHS.size()));
    }

    /** Caminho para a carta da propriedade/companhia daquela casa (ou null se não mapeada/arquivo ausente). */
    public static String propertyCardPath(int cellIndex) {
        String path = PROPERTY_MAP.get(normalize(cellIndex));
        return (path != null && resourceExists(path)) ? path : null;
    }

    /** Registre/ajuste um mapeamento (índice -> caminho do arquivo). */
    public static void putProperty(int cellIndex, String resourcePath) {
        PROPERTY_MAP.put(normalize(cellIndex), resourcePath);
    }

    private static int normalize(int c) {
        return ((c % 40) + 40) % 40;
    }

    private static List<String> makeChancePaths() {
        List<String> list = new ArrayList<>(30);
        for (int i = 1; i <= 30; i++) {
            String p = "/sorteReves/chance" + i + ".png";
            if (resourceExists(p)) list.add(p);
        }
        // Se nada encontrado (nomes diferentes), deixe uma lista padrão para não quebrar:
        if (list.isEmpty()) for (int i = 1; i <= 30; i++) list.add("/sorteReves/chance" + i + ".png");
        return Collections.unmodifiableList(list);
    }

    private static boolean resourceExists(String path) {
        return CardResolver.class.getResource(path) != null;
    }
}
