package infra;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Utilitário simples para carregar e reusar imagens do classpath. */
public final class ImageStore {
    private ImageStore() {}

    // cache opcional para evitar reler do disco
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    /** Carrega uma imagem do classpath (ex.: "/dados/die_face_1.png"). */
    public static BufferedImage load(String resourcePath) {
        try {
            return ImageIO.read(ImageStore.class.getResourceAsStream(resourcePath));
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    /** Retorna imagem do cache (ou carrega se ainda não houver). */
    public static BufferedImage loadCached(String path) {
        return CACHE.computeIfAbsent(path, p -> load(p));
    }

    /** Ícone de dado escalado (face 1..6; size em pixels do lado). */
    public static Icon diceIcon(int face, int size) {
        BufferedImage img = loadCached("/dados/die_face_" + face + ".png");
        if (img == null) return new ImageIcon();
        Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
