package Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Façade + Singleton: ponto único de contato da UI com o Model. */
public final class GameFacade {

    // ---------- Singleton ----------
    private static GameFacade INSTANCE;

    /** Cria (uma vez) e retorna a instância única. */
    public static GameFacade init(String[] nomes, int[] ordemSorteada) {
        if (INSTANCE == null) INSTANCE = new GameFacade(nomes, ordemSorteada);
        return INSTANCE;
    }

    /** Obtém a instância já criada. */
    public static GameFacade get() {
        if (INSTANCE == null)
            throw new IllegalStateException("GameFacade.init(...) ainda não foi chamado.");
        return INSTANCE;
    }

    /** (Opcional para testes) limpa a instância única. */
    public static void resetForTests() { INSTANCE = null; }

    // ---------- Estado interno ----------
    private final Banco banco;
    private final Tabuleiro tabuleiro;
    private final MotorDeJogo motor;
    private final List<Jogador> jogadores = new ArrayList<>();
    private final List<GameObserver> observers = new ArrayList<>();

    private final int[] ordem; // ordem sorteada (índices 0..n-1)
    private int turnPtr = 0;   // ponteiro dentro de 'ordem'

    // Construtor privado (Singleton)
    private GameFacade(String[] nomes, int[] ordemSorteada) {
        this.banco = new Banco();
        this.tabuleiro = new Tabuleiro();

        // >>> CADASTRO DAS PROPRIEDADES DO TABULEIRO <<<
        cadastrarPropriedadesPadrao();

        // Jogadores
        for (String nome : nomes) {
            Jogador j = new Jogador(nome);
            jogadores.add(j);
            tabuleiro.addJogador(j);
        }

        if (ordemSorteada != null && ordemSorteada.length == jogadores.size()) {
            this.ordem = ordemSorteada.clone();
        } else {
            this.ordem = new int[jogadores.size()];
            for (int i = 0; i < this.ordem.length; i++) this.ordem[i] = i;
        }

        this.motor = new MotorDeJogo(banco, tabuleiro);
        // Se quiser cartas de teste: this.tabuleiro.inicializarBaralhoTeste();
    }

    // ---------- Observer ----------
    public void addObserver(GameObserver o) { if (o != null && !observers.contains(o)) observers.add(o); }
    public void removeObserver(GameObserver o) { observers.remove(o); }

    // ---------- Consultas para a UI ----------
    public int  getIndiceJogadorDaVez()                 { return ordem[turnPtr]; }
    public int  getPosicao(int playerIndex)             { return jogadores.get(playerIndex).getPosicao(); }
    public int  getSaldo(int playerIndex)               { return jogadores.get(playerIndex).getConta().getSaldo(); }
    public boolean jogadorEstaPreso(int playerIndex)    { return jogadores.get(playerIndex).estaPreso(); }
    public String getNomeJogador(int playerIndex)       { return jogadores.get(playerIndex).getNome(); }

    // ----------- NOVAS consultas para HUD / cards -----------
    /** Lista os nomes das propriedades pertencentes ao jogador. */
    public List<String> getPropriedadesDoJogador(int playerIndex) {
        List<String> out = new ArrayList<>();
        if (playerIndex < 0 || playerIndex >= jogadores.size()) return out;
        Jogador dono = jogadores.get(playerIndex);
        for (int pos = 0; pos < 40; pos++) {
            Propriedade p = tabuleiro.getPropriedadeNaPosicao(pos);
            if (p != null && p.getProprietario() == dono) {
                out.add(p.getNome());
            }
        }
        return out;
        // (Se quiser devolver objetos, crie uma DTO, mas p/ HUD nomes bastam.)
    }

    // NOVO: células das propriedades do jogador (para abrir cartas por clique)
    public List<Integer> getCelulasPropriedadesDoJogador(int playerIndex) {
    List<Integer> out = new ArrayList<>();
    if (playerIndex < 0 || playerIndex >= jogadores.size()) return out;
    Jogador dono = jogadores.get(playerIndex);
    for (int pos = 0; pos < 40; pos++) {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(pos);
        if (p != null && p.getProprietario() == dono) out.add(pos);
    }
    return out;
}


    /** Índice do dono da propriedade na 'cell' (ou null se não houver). */
    public Integer getIndiceDonoDaPosicao(int cell) {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(cell));
        if (p == null) return null;
        Jogador dono = p.getProprietario();
        if (dono == null) return null;
        for (int i = 0; i < jogadores.size(); i++) {
            if (jogadores.get(i) == dono) return i;
        }
        return null;
    }

    // ----------- Consultas sobre propriedades (para UI decidir interações) -----------
    public boolean posicaoTemPropriedade(int cell) {
        return tabuleiro.getPropriedadeNaPosicao(norm40(cell)) != null;
    }

    public boolean propriedadeDisponivel(int cell) {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(cell));
        return p != null && p.estaDisponivel();
    }

    public String getNomePropriedade(int cell) {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(cell));
        return (p != null ? p.getNome() : null);
    }

    public int getPrecoPropriedade(int cell) {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(cell));
        return (p != null ? p.getPreco() : 0);
    }

    public boolean jogadorEhDonoDaPosicao(int playerIndex, int cell) {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(cell));
        if (p == null) return false;
        return p.getProprietario() == jogadores.get(playerIndex);
    }

    public boolean podeConstruirAqui(int playerIndex) {
        Jogador j = jogadores.get(playerIndex);
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(j.getPosicao());
        if (p instanceof Terreno t) {
            return t.getProprietario() == j && t.podeConstruir();
        }
        return false;
    }

    public int getValorCasaAqui(int playerIndex) {
        Jogador j = jogadores.get(playerIndex);
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(j.getPosicao());
        if (p instanceof Terreno t) return t.getValorCasa();
        return 0;
    }

    // ---------- Ações (chamadas pela UI) ----------
    /** UI pediu rolagem aleatória — usa MotorDeJogo.lancarDados(). */
    public void rolarDadosAleatorio() {
        List<Integer> d = motor.lancarDados();
        processarRolagem(d.get(0), d.get(1));
    }

    /** UI pediu rolagem com valores definidos (modo teste). */
    public void rolarDadosForcado(int d1, int d2) {
        processarRolagem(d1, d2);
    }

    /** Compra a propriedade na posição informada para o jogador. */
    public void comprarPropriedade(int playerIndex, int posicao) {
        Jogador j = jogadores.get(playerIndex);
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(posicao));
        if (p != null) motor.comprarPropriedade(j, p);
        // HUD será atualizado pelo Controller após a ação.
    }

    /** Compra a propriedade onde o jogador está. */
    public void comprarPropriedadeAtual(int playerIndex) {
        comprarPropriedade(playerIndex, jogadores.get(playerIndex).getPosicao());
    }

    /** Constrói casa (se puder) na propriedade onde o jogador está. */
    public void construirCasaNoLocal(int playerIndex) {
        Jogador j = jogadores.get(playerIndex);
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(j.getPosicao());
        if (p != null) motor.construirCasa(j, p);
        // HUD será atualizado pelo Controller após a ação.
    }

    /** Puxa uma carta de Sorte/Revés e aplica seus efeitos ao jogador. */
    public void puxarSorteReves(int playerIndex) { motor.puxarSorteReves(jogadores.get(playerIndex)); }

    /** Tenta usar carta de saída livre. */
    public boolean usarCartaLiberacao(int playerIndex) { return motor.usarCartaLiberacao(jogadores.get(playerIndex)); }

    /** Verifica falência explicitamente. */
    public boolean verificarFalencia(int playerIndex) { return motor.verificarFalencia(jogadores.get(playerIndex)); }

    // ---------- Fluxo interno ----------
    private void processarRolagem(int d1, int d2) {
        // notifica faces dos dados
        for (GameObserver o : observers) o.onDice(d1, d2);

        int jIndex = getIndiceJogadorDaVez();
        Jogador j = jogadores.get(jIndex);
        int from = j.getPosicao();

        // Se estiver preso, regra de saída por dupla é do Motor
        if (j.estaPreso()) {
            boolean liberado = motor.soltarSeDupla(j, Arrays.asList(d1, d2));
            if (!liberado) { // não sai — turno passa
                avancarVezENotificar();
                return;
            }
            // se liberou, segue com o movimento normal
        }

        // Movimento + (prisão, aluguel etc.) ficam NO motor
        motor.moverJogador(j, Arrays.asList(d1, d2));
        int to = j.getPosicao();

        // Notifica UI do deslocamento
        for (GameObserver o : observers) o.onMoved(jIndex, from, to);

        // Avança turno
        avancarVezENotificar();
    }

    private void avancarVezENotificar() {
        turnPtr = (turnPtr + 1) % ordem.length;
        int atual = getIndiceJogadorDaVez();
        for (GameObserver o : observers) o.onTurnChanged(atual);
    }

    private static int norm40(int v) { return ((v % 40) + 40) % 40; }

    // ---------- Cadastro das propriedades (preços/aluguéis) ----------
    private void cadastrarPropriedadesPadrao() {
        // (seus registros atuais — complete/ajuste conforme seu tabuleiro)
        tabuleiro.addPropriedade(new Terreno("Leblon",                     100, 50, 10,  1));
        tabuleiro.addPropriedade(new Terreno("Av. Pres. Vargas",           120, 60, 12,  3));
        tabuleiro.addPropriedade(new Terreno("Av. N. S. Copacabana",       140, 70, 14,  4));
        tabuleiro.addPropriedade(new Terreno("Av. Brigadeiro Faria Lima",  160, 80, 16,  6));
        tabuleiro.addPropriedade(new Propriedade("Companhia 1",            200, 25,     5));
        tabuleiro.addPropriedade(new Propriedade("Companhia 2",            220, 28,     7));
        tabuleiro.addPropriedade(new Terreno("Av. Rebouças",               180, 90, 18,  8));
        tabuleiro.addPropriedade(new Terreno("Av. 9 de Julho",             200, 100,20,  9));
        tabuleiro.addPropriedade(new Terreno("Av. Europa",                 220, 110,22, 11));
        tabuleiro.addPropriedade(new Terreno("Rua Augusta",                240, 120,24, 13));
        tabuleiro.addPropriedade(new Terreno("Av. Pacaembú",               260, 130,26, 14));
        tabuleiro.addPropriedade(new Terreno("Interlagos",                 280, 140,28, 17));
        tabuleiro.addPropriedade(new Terreno("Morumbi",                    300, 150,30, 19));
        tabuleiro.addPropriedade(new Propriedade("Companhia 3",            240, 30,    15));
        tabuleiro.addPropriedade(new Propriedade("Companhia 4",            260, 33,    25));
        tabuleiro.addPropriedade(new Propriedade("Companhia 5",            280, 36,    32));
        tabuleiro.addPropriedade(new Propriedade("Companhia 6",            300, 40,    35));
        tabuleiro.addPropriedade(new Terreno("Copacabana",                 320, 160,32, 31));
        tabuleiro.addPropriedade(new Terreno("Av. Vieira Souto",           340, 170,34, 33));
        tabuleiro.addPropriedade(new Terreno("Av. Atlântica",              360, 180,36, 34));
        tabuleiro.addPropriedade(new Terreno("Ipanema",                    380, 190,38, 36));
        tabuleiro.addPropriedade(new Terreno("Jardim Paulista",            400, 200,40, 38));
        tabuleiro.addPropriedade(new Terreno("Brooklin",                   420, 210,42, 39));
    }
}
