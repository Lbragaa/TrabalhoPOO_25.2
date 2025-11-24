package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/** Responsável por salvar/carregar o estado da partida em arquivo texto ASCII. */
final class GameStateIO {
    private GameStateIO() {}

    // Paleta fixa alinhada aos pinos (0..5)
    private static final List<java.awt.Color> PIN_PALETTE = List.of(
            java.awt.Color.RED, java.awt.Color.BLUE, java.awt.Color.ORANGE,
            java.awt.Color.YELLOW, java.awt.Color.PINK, java.awt.Color.GRAY
    );

    private static int colorToIndex(java.awt.Color c) {
        if (c == null) return 0;
        for (int i = 0; i < PIN_PALETTE.size(); i++) if (PIN_PALETTE.get(i).equals(c)) return i;
        return 0;
    }
    private static java.awt.Color indexToColor(int idx) {
        if (idx < 0 || idx >= PIN_PALETTE.size()) return PIN_PALETTE.get(0);
        return PIN_PALETTE.get(idx);
    }

    static void salvar(GameStateSnapshot snapshot, File arquivo) throws IOException {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.US_ASCII))) {
            out.println("BANCO=" + snapshot.bancoSaldo());
            out.println("ORDEM=" + joinIntList(snapshot.ordem()));
            out.println("PONTEIRO=" + snapshot.ponteiro());
            out.println("PLAYERS=" + snapshot.players().size());
            for (GameStateSnapshot.PlayerData p : snapshot.players()) {
                out.println("PLAYER|" + esc(p.nome()) + "|" + p.saldo() + "|" + p.posicao() + "|" +
                        (p.preso() ? 1 : 0) + "|" + (p.falido() ? 1 : 0) + "|" + p.cartasLiberacao() + "|" +
                        p.corIndex());
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
        List<Integer> ordem = null;
        int ponteiro = 0;
        int nPlayers = 0;
        List<GameStateSnapshot.PlayerData> players = new ArrayList<>();
        List<GameStateSnapshot.PropertyData> props = new ArrayList<>();
        List<Carta> deck = new ArrayList<>();

        while (it.hasNext()) {
            String ln = it.next().trim();
            if (ln.isEmpty()) continue;
            if (ln.startsWith("BANCO=")) { bancoSaldo = Integer.parseInt(ln.substring(6)); continue; }
            if (ln.startsWith("ORDEM=")) { ordem = parseIntList(ln.substring(6)); continue; }
            if (ln.startsWith("PONTEIRO=")) { ponteiro = Integer.parseInt(ln.substring(9)); continue; }
            if (ln.startsWith("PLAYERS=")) { nPlayers = Integer.parseInt(ln.substring(8)); continue; }
            if (ln.equals("PROPS")) { break; }
            if (ln.startsWith("PLAYER|")) {
                StringTokenizer tok = new StringTokenizer(ln, "|");
                tok.nextToken(); // "PLAYER"
                String nome = tok.nextToken();
                int saldo = Integer.parseInt(tok.nextToken());
                int pos = Integer.parseInt(tok.nextToken());
                boolean preso = "1".equals(tok.nextToken());
                boolean falido = "1".equals(tok.nextToken());
                int cartas = Integer.parseInt(tok.nextToken());
                int corIndex = Integer.parseInt(tok.nextToken());
                players.add(new GameStateSnapshot.PlayerData(nome, corIndex, saldo, pos, preso, falido, cartas));
            }
        }
        while (it.hasNext()) {
            String ln = it.next().trim();
            if (ln.equals("DECK")) break;
            if (ln.startsWith("PROP|")) {
                StringTokenizer tok = new StringTokenizer(ln, "|");
                tok.nextToken(); // "PROP"
                props.add(new GameStateSnapshot.PropertyData(
                        Integer.parseInt(tok.nextToken()),
                        Integer.parseInt(tok.nextToken()),
                        Integer.parseInt(tok.nextToken()),
                        Integer.parseInt(tok.nextToken())
                ));
            }
        }
        while (it.hasNext()) {
            String ln = it.next().trim();
            if (ln.startsWith("CARD|")) {
                StringTokenizer tok = new StringTokenizer(ln, "|");
                tok.nextToken(); // "CARD"
                deck.add(new Carta(
                        TipoCarta.valueOf(tok.nextToken()),
                        Integer.parseInt(tok.nextToken()),
                        Integer.parseInt(tok.nextToken())
                ));
            }
        }

        if (ordem == null) ordem = new ArrayList<>();
        if (ordem.isEmpty()) {
            for (int i = 0; i < nPlayers; i++) ordem.add(i);
        }
        return new GameStateSnapshot(bancoSaldo, ordem, ponteiro, players, props, deck);
    }

    private static String joinIntList(List<Integer> arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(arr.get(i));
        }
        return sb.toString();
    }

    private static List<Integer> parseIntList(String s) {
        List<Integer> out = new ArrayList<>();
        int acc = 0;
        boolean inNumber = false, negative = false;
        for (int i = 0; i <= s.length(); i++) {
            char ch = (i < s.length() ? s.charAt(i) : ',');
            if (ch == ',' || ch == ' ' || ch == '\t') {
                if (inNumber) {
                    out.add(negative ? -acc : acc);
                    acc = 0;
                    inNumber = false;
                    negative = false;
                }
            } else if (ch == '-') {
                negative = true;
            } else if (Character.isDigit(ch)) {
                acc = acc * 10 + (ch - '0');
                inNumber = true;
            }
        }
        return out;
    }

    private static String esc(String s) {
        return s.replace("|", "/");
    }
}
