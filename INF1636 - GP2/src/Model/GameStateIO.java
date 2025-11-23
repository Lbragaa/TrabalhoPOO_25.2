package Model;

import java.awt.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Responsável por salvar/carregar o estado da partida em arquivo texto ASCII. */
final class GameStateIO {
    private GameStateIO() {}

    static void salvar(GameStateSnapshot snapshot, File arquivo) throws IOException {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.US_ASCII))) {
            out.println("BANCO=" + snapshot.bancoSaldo());
            out.println("ORDEM=" + joinIntArray(snapshot.ordem()));
            out.println("PONTEIRO=" + snapshot.ponteiro());
            out.println("PLAYERS=" + snapshot.players().size());
            for (GameStateSnapshot.PlayerData p : snapshot.players()) {
                Color c = p.cor();
                out.println("PLAYER|" + esc(p.nome()) + "|" + p.saldo() + "|" + p.posicao() + "|" +
                        (p.preso() ? 1 : 0) + "|" + (p.falido() ? 1 : 0) + "|" + p.cartasLiberacao() + "|" +
                        c.getRed() + "," + c.getGreen() + "," + c.getBlue());
            }
            out.println("PROPS");
            for (GameStateSnapshot.PropertyData p : snapshot.propriedades()) {
                out.println("PROP|" + p.posicao() + "|" + p.ownerIndex() + "|" + p.casas() + "|" + p.hotel());
            }
            out.println("DECK");
            for (Carta c : snapshot.deck()) {
                out.println("CARD|" + c.tipo.name() + "|" + c.valor + "|" + c.codigo);
            }
        }
    }

    static GameStateSnapshot carregar(File arquivo) throws IOException {
        List<String> linhas = Files.readAllLines(arquivo.toPath(), StandardCharsets.US_ASCII);
        Iterator<String> it = linhas.iterator();
        int bancoSaldo = 0;
        int[] ordem = null;
        int ponteiro = 0;
        int nPlayers = 0;
        List<GameStateSnapshot.PlayerData> players = new ArrayList<>();
        List<GameStateSnapshot.PropertyData> props = new ArrayList<>();
        List<Carta> deck = new ArrayList<>();

        while (it.hasNext()) {
            String ln = it.next().trim();
            if (ln.isEmpty()) continue;
            if (ln.startsWith("BANCO=")) { bancoSaldo = Integer.parseInt(ln.substring(6)); continue; }
            if (ln.startsWith("ORDEM=")) { ordem = parseIntArray(ln.substring(6)); continue; }
            if (ln.startsWith("PONTEIRO=")) { ponteiro = Integer.parseInt(ln.substring(9)); continue; }
            if (ln.startsWith("PLAYERS=")) { nPlayers = Integer.parseInt(ln.substring(8)); continue; }
            if (ln.equals("PROPS")) { break; }
            if (ln.startsWith("PLAYER|")) {
                String[] p = ln.split("\\|");
                String nome = p[1];
                int saldo = Integer.parseInt(p[2]);
                int pos = Integer.parseInt(p[3]);
                boolean preso = "1".equals(p[4]);
                boolean falido = "1".equals(p[5]);
                int cartas = Integer.parseInt(p[6]);
                Color cor = parseColor(p[7]);
                players.add(new GameStateSnapshot.PlayerData(nome, cor, saldo, pos, preso, falido, cartas));
            }
        }
        while (it.hasNext()) {
            String ln = it.next().trim();
            if (ln.equals("DECK")) break;
            if (ln.startsWith("PROP|")) {
                String[] p = ln.split("\\|");
                props.add(new GameStateSnapshot.PropertyData(
                        Integer.parseInt(p[1]),
                        Integer.parseInt(p[2]),
                        Integer.parseInt(p[3]),
                        Integer.parseInt(p[4])
                ));
            }
        }
        while (it.hasNext()) {
            String ln = it.next().trim();
            if (ln.startsWith("CARD|")) {
                String[] p = ln.split("\\|");
                deck.add(new Carta(TipoCarta.valueOf(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3])));
            }
        }

        if (ordem == null) ordem = new int[Math.max(1, nPlayers)];
        return new GameStateSnapshot(bancoSaldo, ordem, ponteiro, players, props, deck);
    }

    private static String joinIntArray(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    private static int[] parseIntArray(String s) {
        String[] parts = s.split(",");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) out[i] = Integer.parseInt(parts[i].trim());
        return out;
    }

    private static Color parseColor(String rgb) {
        String[] parts = rgb.split(",");
        int r = Integer.parseInt(parts[0]);
        int g = Integer.parseInt(parts[1]);
        int b = Integer.parseInt(parts[2]);
        return new Color(r, g, b);
    }

    private static String esc(String s) {
        return s.replace("|", "/");
    }
}
