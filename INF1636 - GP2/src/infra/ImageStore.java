package infra;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilitário para carregar e reutilizar imagens do classpath.
 * <p>Escopo: apoio à UI (sem regras de negócio).</p>
 * <ul>
 *   <li>Fornece cache de imagens para evitar I/O repetido.</li>
 *   <li>Exposição de ícones de dado com escala suave.</li>
 * </ul>
 */
public final class ImageStore {
    private ImageStore() {}

    /** Cache thread-safe (reduz leituras/decodificações repetidas). */
    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();

    /**
     * Carrega uma imagem do classpath.
     * @param resourcePath caminho absoluto no classpath (ex.: "/dados/die_face_1.png")
     * @return imagem ou {@code null} se não encontrada ou falha de leitura
     */
    public static BufferedImage load(String resourcePath) {
        try {
            // ImageIO.read fecha o stream internamente
            return ImageIO.read(ImageStore.class.getResourceAsStream(resourcePath));
        } catch (IOException | NullPointerException e) {
            // TODO: opcional: logar caminho ausente para facilitar debug
            return null;
        }
    }

    /**
     * Retorna imagem do cache (ou carrega e guarda, se ainda não houver).
     * @param path caminho absoluto no classpath
     * @return imagem ou {@code null} se indisponível
     */
    public static BufferedImage loadCached(String path) {
        return CACHE.computeIfAbsent(path, ImageStore::load);
    }

    /**
     * Cria um ícone de dado escalado.
     * @param face face entre 1 e 6
     * @param size tamanho em pixels (lado)
     * @return {@link Icon} pronto para uso em Swing
     */
    public static Icon diceIcon(int face, int size) {
        // Validação defensiva: limita a 1..6
        int f = Math.max(1, Math.min(6, face));
        BufferedImage img = loadCached("/dados/die_face_" + f + ".png");
        if (img == null) return new ImageIcon(); // ícone vazio se recurso faltar
        Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
