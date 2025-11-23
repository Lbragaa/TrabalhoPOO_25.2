package Model;

import java.awt.Color;
import java.io.File;
import java.util.List;

/**
 * Testes de integração simples (executar como Java Application).
 * Não abre janelas: valida cenários chave de domínio/observer.
 */
public class IntegrationTests {

    @FunctionalInterface
    interface TestCase { void run() throws Exception; }

    private static int passed = 0, failed = 0;

    public static void main(String[] args) {
        // evita abrir UI em ambientes sem display
        System.setProperty("java.awt.headless", "true");

        run("Falência por imposto e remoção do tabuleiro", IntegrationTests::testFalenciaImposto);
        run("Vencedor automático quando resta um", IntegrationTests::testVencedorUnico);
        run("Uso automático da carta de liberação", IntegrationTests::testCartaLiberacao);
        run("Falência ao pagar aluguel caro", IntegrationTests::testFalenciaPorAluguel);
        run("Salvar/Carregar preserva falidos e ativos", IntegrationTests::testSalvarCarregarFalido);
        run("Baralho de chance: vai para prisão", IntegrationTests::testChanceVaiParaPrisao);
        run("Baralho de chance: ganha carta de liberação", IntegrationTests::testChanceGanhaLiberacao);
        run("Propriedade com casas/hotel persiste no snapshot", IntegrationTests::testSalvarCarregarConstrucoes);
        run("Ponteiro de turno ignora falidos", IntegrationTests::testPonteiroPulaFalidos);

        System.out.println("\nResumo: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) System.exit(1);
    }

    private static void run(String name, TestCase tc) {
        try {
            GameFacade.resetForTests();
            tc.run();
            passed++;
            System.out.println("[OK]   " + name);
        } catch (Throwable t) {
            failed++;
            System.err.println("[FAIL] " + name + " -> " + t.getMessage());
            t.printStackTrace();
        }
    }

    // ---------- Cenários ----------

    private static void testFalenciaImposto() {
        GameFacade game = GameFacade.init(new String[]{"A", "B"}, new int[]{0,1});
        Jogador j0 = game.getJogadores().get(0);
        j0.getConta().setSaldo(100); // insuficiente para pagar IR (2000 no teste)
        j0.setPosicao(24);

        game.aplicarCasasEspeciais(0);
        assertTrue(j0.isFalido(), "Jogador deveria estar falido após IR");
        assertFalse(game.getTabuleiro().getJogadoresAtivos().contains(j0), "Falido deve sair do tabuleiro");
    }

    private static void testVencedorUnico() {
        RecordingObserver obs = new RecordingObserver();
        GameFacade game = GameFacade.init(new String[]{"A", "B", "C"}, new int[]{0,1,2});
        game.addObserver(obs);

        // falir B e C
        Jogador b = game.getJogadores().get(1);
        b.getConta().setSaldo(-10);
        game.verificarFalencia(1);
        Jogador c = game.getJogadores().get(2);
        c.getConta().setSaldo(-10);
        game.verificarFalencia(2);

        game.encerrarPartida();

        assertTrue(obs.ended, "Deveria notificar fim de partida");
        assertTrue(obs.winnerIndex == 0, "Vencedor esperado: A");
    }

    private static void testCartaLiberacao() {
        RecordingObserver obs = new RecordingObserver();
        GameFacade game = GameFacade.init(new String[]{"A", "B"}, new int[]{0,1});
        game.addObserver(obs);
        Jogador j0 = game.getJogadores().get(0);
        j0.prende();
        j0.setCartasLiberacao(1);

        game.usarCartaLiberacaoAutomatica(0);

        assertFalse(j0.estaPreso(), "Jogador deve ser libertado");
        assertTrue(j0.getCartasLiberacao() == 0, "Carta de liberação deve ser consumida");
        assertTrue(obs.releaseUsed, "Observer deve ser notificado do uso da carta");
    }

    private static void testFalenciaPorAluguel() {
        GameFacade game = GameFacade.init(new String[]{"A", "B"}, new int[]{0,1});
        Jogador pagador = game.getJogadores().get(0);
        Jogador dono = game.getJogadores().get(1);
        pagador.getConta().setSaldo(50); // não cobre aluguel caro

        // Posição 19 = Morumbi (Terreno). Configura dono e uma casa.
        Terreno morumbi = (Terreno) game.getTabuleiro().getPropriedadeNaPosicao(19);
        morumbi.setProprietario(dono);
        morumbi.adicionaCasa(); // aluguel ~100
        pagador.setPosicao(19);

        game.cobrarAluguelSeNecessario(0);
        game.verificarFalencia(0);

        assertTrue(pagador.isFalido(), "Pagador deveria falir ao não pagar aluguel");
        assertFalse(game.getTabuleiro().getJogadoresAtivos().contains(pagador), "Falido deve ser removido do tabuleiro");
    }

    private static void testSalvarCarregarFalido() throws Exception {
        GameFacade game = GameFacade.init(new String[]{"A", "B", "C"}, new int[]{0,1,2});
        Jogador b = game.getJogadores().get(1);
        b.getConta().setSaldo(-100);
        game.verificarFalencia(1); // B falido

        File tmp = File.createTempFile("monopoly-test", ".txt");
        tmp.deleteOnExit();
        Color[] cores = new Color[]{Color.RED, Color.BLUE, Color.YELLOW};
        game.salvarParaArquivo(tmp, cores);

        GameStateSnapshot snap = GameFacade.carregarSnapshot(tmp);
        GameFacade.resetForTests();
        GameFacade game2 = GameFacade.initFromSnapshot(snap);

        Jogador b2 = game2.getJogadores().get(1);
        assertTrue(b2.isFalido(), "Falência deve persistir após carregar");
        assertFalse(game2.getTabuleiro().getJogadoresAtivos().contains(b2), "Falido não deve estar ativo no tabuleiro");
    }

    private static void testChanceVaiParaPrisao() {
        GameFacade game = GameFacade.init(new String[]{"A"}, new int[]{0});
        Jogador j = game.getJogadores().get(0);
        // controla baralho: coloca "vai para prisão" no topo
        game.getTabuleiro().baralhoSorteReves.clear();
        game.getTabuleiro().baralhoSorteReves.offer(new Carta(TipoCarta.VAI_PARA_PRISAO, 0, 23));
        j.setPosicao(2); // casa de sorte/revés
        game.resolverChanceSeNecessario(0);
        assertTrue(j.estaPreso(), "Jogador deveria ficar preso após carta de prisão");
    }

    private static void testChanceGanhaLiberacao() {
        GameFacade game = GameFacade.init(new String[]{"A"}, new int[]{0});
        Jogador j = game.getJogadores().get(0);
        // controlamos o baralho: coloca SAIDA_LIVRE no topo
        game.getTabuleiro().baralhoSorteReves.clear();
        game.getTabuleiro().baralhoSorteReves.offer(new Carta(TipoCarta.SAIDA_LIVRE, 0, 9));
        j.setPosicao(2);
        game.resolverChanceSeNecessario(0);
        assertTrue(j.getCartasLiberacao() == 1, "Jogador deve ganhar carta de liberação");
    }

    private static void testSalvarCarregarConstrucoes() throws Exception {
        GameFacade game = GameFacade.init(new String[]{"A", "B"}, new int[]{0,1});
        Jogador dono = game.getJogadores().get(0);
        Terreno terreno = (Terreno) game.getTabuleiro().getPropriedadeNaPosicao(19); // Morumbi
        terreno.setProprietario(dono);
        terreno.adicionaCasa();
        terreno.adicionaCasa();
        terreno.adicionaCasa();
        terreno.adicionaCasa();
        terreno.adicionaCasa(); // vira hotel

        File tmp = File.createTempFile("monopoly-props", ".txt"); tmp.deleteOnExit();
        game.salvarParaArquivo(tmp, new Color[]{Color.RED, Color.BLUE});
        GameStateSnapshot snap = GameFacade.carregarSnapshot(tmp);
        GameFacade.resetForTests();
        GameFacade game2 = GameFacade.initFromSnapshot(snap);
        Terreno terreno2 = (Terreno) game2.getTabuleiro().getPropriedadeNaPosicao(19);
        assertTrue(terreno2.getNumCasas() == 4 && terreno2.temHotel(), "Construções (4 casas + hotel) devem persistir");
        assertTrue(terreno2.getProprietario() == game2.getJogadores().get(0), "Proprietário deve persistir");
    }

    private static void testPonteiroPulaFalidos() {
        RecordingObserver obs = new RecordingObserver();
        GameFacade game = GameFacade.init(new String[]{"A", "B", "C"}, new int[]{0,1,2});
        game.addObserver(obs);
        // marca B como falido
        Jogador b = game.getJogadores().get(1);
        b.getConta().setSaldo(-10);
        game.verificarFalencia(1);
        // ponteiro inicialmente 0, avançar deve ir para C (2), não para B
        game.avancarTurnoENotificar();
        assertTrue(obs.lastTurn == 2, "Ponteiro deve pular jogador falido");
    }

    // ---------- Helpers ----------

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }
    private static void assertFalse(boolean cond, String msg) {
        if (cond) throw new AssertionError(msg);
    }

    /** Observer que grava eventos de fim de jogo e uso de carta. */
    private static class RecordingObserver implements GameObserver {
        boolean ended = false;
        int winnerIndex = -1;
        boolean releaseUsed = false;
        int lastTurn = -1;
        @Override public void onDice(int d1, int d2) {}
        @Override public void onMoved(int indiceJogador, int celulaOrigem, int celulaDestino) {}
        @Override public void onTurnChanged(int currentPlayerIndex) { lastTurn = currentPlayerIndex; }
        @Override public void onBalanceChanged(int indiceJogador, int novoSaldo) {}
        @Override public void onPropertyBought(int indiceJogador, int celula) {}
        @Override public void onHouseBuilt(int indiceJogador, int celula, int numeroCasas) {}
        @Override public void onJailStatus(int indiceJogador, boolean preso) {}
        @Override public void onRentPaid(int indicePagador, int indiceDono, int celula, int valor) {}
        @Override public void onBankruptcy(int indiceJogador) {}
        @Override public void onSpecialCell(int indiceJogador, int celula, int valor, String descricao) {}
        @Override public void onReleaseCardUsed(int indiceJogador) { releaseUsed = true; }
        @Override public void onChanceCard(int indiceJogador, int celula, int numero, String tipo, int valor) {}
        @Override public void onGameEnded(int winnerIndex, int[] capitaisPorJogador) {
            this.ended = true;
            this.winnerIndex = winnerIndex;
        }
    }
}
