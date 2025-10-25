package view;

import infra.ImageStore;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Diálogo de setup:
 *  1) número de jogadores (3..6)
 *  2) nome (<=8 alfanum.) + cor (sem repetir) para cada jogador
 *  3) exibe a ORDEM sorteada antes de iniciar
 */
public class PlayerSetupDialog extends JDialog {
    private final BackgroundPanel bg;
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);

    // step 1
    private JSpinner spnPlayers;

    // step 2
    private JPanel playersPanel;
    private JTextField[] nameFields;
    private JComboBox<ColorItem>[] colorBoxes;
    private final List<ColorItem> palette = defaultPalette();

    // step 3 (ordem)
    private JList<String> ordemList;

    private boolean confirmed = false;
    private int numJogadores = 3;
    private Color[] coresEscolhidas;
    private String[] nomesEscolhidos;
    private int[] ordemSorteada;

    public PlayerSetupDialog(Window owner) {
        super(owner, "Configurar Partida", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        bg = new BackgroundPanel();
        bg.setLayout(new GridBagLayout());

        buildStep1();
        buildStep2();
        buildStep3();

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12,12,12,12);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1; c.weighty = 1;
        bg.add(cardPanel, c);

        setContentPane(bg);
        setSize(820, 560);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    // ---- STEP 1 ----
    private void buildStep1() {
        JPanel p = boxPanel("Número de jogadores");
        spnPlayers = new JSpinner(new SpinnerNumberModel(3, 3, 6, 1));
        p.add(new JLabel("Jogadores (3 a 6):"), gbc(0,0));
        p.add(spnPlayers, gbc(1,0));

        JButton next = new JButton("Continuar");
        next.addActionListener(e -> {
            numJogadores = (Integer) spnPlayers.getValue();
            buildPlayersUi(numJogadores);
            cards.show(cardPanel, "players");
        });
        p.add(next, gbcSpan(0,1,2,1, GridBagConstraints.EAST));
        cardPanel.add(p, "count");
    }

    // ---- STEP 2 ----
    @SuppressWarnings("unchecked")
    private void buildStep2() {
        playersPanel = boxPanel("Jogadores: nome (até 8) + cor (sem repetir)");
        cardPanel.add(playersPanel, "players");
    }

    private void buildPlayersUi(int n) {
        playersPanel.removeAll();
        nameFields = new JTextField[n];
        colorBoxes = new JComboBox[n];

        playersPanel.add(new JLabel("Informe nome e cor de cada jogador:"), gbcSpan(0,0,3,1, GridBagConstraints.WEST));

        for (int i = 0; i < n; i++) {
            JLabel lbl = new JLabel("Jogador " + (i+1) + ":");
            nameFields[i] = new JTextField(10);
            nameFields[i].setDocument(new LimitedDocument(8)); // limita a 8 chars
            colorBoxes[i] = new JComboBox<>(palette.toArray(new ColorItem[0]));
            colorBoxes[i].setRenderer(new ColorCellRenderer());

            playersPanel.add(lbl,           gbc(0, i+1));
            playersPanel.add(nameFields[i], gbc(1, i+1));
            playersPanel.add(colorBoxes[i], gbc(2, i+1));
        }

        JButton back = new JButton("Voltar");
        back.addActionListener(e -> cards.show(cardPanel, "count"));
        JButton next = new JButton("Sortear ordem");
        next.addActionListener(e -> onPlayersOk());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        buttons.add(back); buttons.add(next);

        playersPanel.add(buttons, gbcSpan(0, n+2, 3, 1, GridBagConstraints.EAST));
        playersPanel.revalidate(); playersPanel.repaint();
    }

    private void onPlayersOk() {
        int n = numJogadores;

        // valida nomes + cores
        Color[] chosenColors = new Color[n];
        String[] chosenNames  = new String[n];
        boolean[] used = new boolean[palette.size()];

        for (int i = 0; i < n; i++) {
            String nome = nameFields[i].getText().trim();
            if (!nome.matches("[A-Za-z0-9]{1,8}")) {
                showMsg("Nome do Jogador " + (i+1) + " inválido. Use 1..8 caracteres alfanuméricos.");
                return;
            }
            chosenNames[i] = nome;

            ColorItem item = (ColorItem) colorBoxes[i].getSelectedItem();
            if (item == null) { showMsg("Selecione todas as cores."); return; }
            int idx = palette.indexOf(item);
            if (idx >= 0 && used[idx]) { showMsg("As cores não podem se repetir."); return; }
            if (idx >= 0) used[idx] = true;
            chosenColors[i] = item.color();
        }

        this.nomesEscolhidos = chosenNames;
        this.coresEscolhidas = chosenColors;

        // sorteia ordem
        ordemSorteada = new int[n];
        for (int i = 0; i < n; i++) ordemSorteada[i] = i;
        shuffle(ordemSorteada, new Random());

        // monta lista visível
        DefaultListModel<String> model = new DefaultListModel<>();
        for (int pos = 0; pos < n; pos++) {
            int j = ordemSorteada[pos];
            String corNome = colorName(coresEscolhidas[j]);
            model.addElement((pos+1) + "º — " + nomesEscolhidos[j] + " (" + corNome + ")");
        }
        ordemList.setModel(model);

        cards.show(cardPanel, "order");
    }

    // ---- STEP 3 ----
    private void buildStep3() {
        JPanel p = boxPanel("Ordem sorteada");
        ordemList = new JList<>(new DefaultListModel<>());
        ordemList.setVisibleRowCount(6);
        p.add(new JScrollPane(ordemList), gbcSpan(0,0,1,1, GridBagConstraints.CENTER));

        JButton ok = new JButton("Iniciar partida");
        ok.addActionListener(e -> { confirmed = true; dispose(); });
        p.add(ok, gbc(0,1));

        cardPanel.add(p, "order");
    }

    // ---- getters usados pelo MainFrame ----
    public boolean isConfirmed()       { return confirmed; }
    public int getNumJogadores()       { return numJogadores; }
    public Color[] getCoresEscolhidas(){ return coresEscolhidas; }
    public String[] getNomesEscolhidos(){ return nomesEscolhidos; }
    public int[] getOrdemSorteada()    { return ordemSorteada; }

    // ---- helpers UI ----
    private void showMsg(String s) {
        JOptionPane.showMessageDialog(this, s, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
    private static GridBagConstraints gbc(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x; c.gridy = y;
        c.insets = new Insets(8,8,8,8);
        c.anchor = GridBagConstraints.WEST;
        return c;
    }
    private static GridBagConstraints gbcSpan(int x, int y, int w, int h, int anchor) {
        GridBagConstraints c = gbc(x,y);
        c.gridwidth = w; c.gridheight = h;
        c.anchor = anchor;
        return c;
    }
    private static JPanel boxPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout()){ @Override public boolean isOpaque(){ return false; } };
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255,255,255,120)), title));
        return p;
    }

    // paleta
    private record ColorItem(String name, Color color) { @Override public String toString(){ return name; } }
    private static List<ColorItem> defaultPalette() {
        List<ColorItem> list = new ArrayList<>();
        list.add(new ColorItem("Vermelho", new Color(220, 20, 60)));
        list.add(new ColorItem("Azul",     new Color(25, 130, 196)));
        list.add(new ColorItem("Verde",    new Color(34, 139, 34)));
        list.add(new ColorItem("Laranja",  new Color(255, 140, 0)));
        list.add(new ColorItem("Roxo",     new Color(148, 0, 211)));
        list.add(new ColorItem("Amarelo",  new Color(240, 200, 0)));
        return list;
    }
    private static String colorName(Color c) {
        if (c==null) return "?";
        if (c.equals(new Color(220,20,60))) return "Vermelho";
        if (c.equals(new Color(25,130,196))) return "Azul";
        if (c.equals(new Color(34,139,34))) return "Verde";
        if (c.equals(new Color(255,140,0))) return "Laranja";
        if (c.equals(new Color(148,0,211))) return "Roxo";
        if (c.equals(new Color(240,200,0))) return "Amarelo";
        return "Cor";
    }

    /** Fundo com tabuleiro ofuscado. */
    private static class BackgroundPanel extends JPanel {
        private final Image board = ImageStore.load("/tabuleiro.png");
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(20,20,20)); g2.fillRect(0,0,getWidth(),getHeight());
            if (board != null) {
                int bw = board.getWidth(null), bh = board.getHeight(null);
                int x = (getWidth() - bw)/2, y = (getHeight() - bh)/2;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
                g2.drawImage(board, x, y, null);
                g2.setComposite(AlphaComposite.SrcOver);
            }
            g2.setColor(new Color(0,0,0,120)); g2.fillRect(0,0,getWidth(),getHeight());
            g2.dispose();
        }
    }

    /** Limita TextField a N chars. */
    private static class LimitedDocument extends javax.swing.text.PlainDocument {
        private final int max;
        LimitedDocument(int max){ this.max = max; }
        @Override public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                throws javax.swing.text.BadLocationException {
            if (str==null) return;
            if ((getLength() + str.length()) <= max) super.insertString(offs, str, a);
        }
    }

    /** Renderer que mostra cor ao lado do nome. */
    private static class ColorCellRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ColorItem ci) {
                lbl.setText(ci.name());
                lbl.setIcon(colorIcon(ci.color()));
            }
            return lbl;
        }
        private static Icon colorIcon(Color c) {
            BufferedImage img = new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(Color.BLACK); g2.fillRect(0,0,16,16);
            g2.setColor(c); g2.fillRect(1,1,14,14);
            g2.dispose();
            return new ImageIcon(img);
        }
    }

    private static void shuffle(int[] a, Random rnd) {
        for (int i=a.length-1; i>0; i--) {
            int j = rnd.nextInt(i+1);
            int t=a[i]; a[i]=a[j]; a[j]=t;
        }
    }
}
