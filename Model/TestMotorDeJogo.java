package Model;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestMotorDeJogo {

    private Banco banco;
    private Tabuleiro tabuleiro;
    private MotorDeJogo motor;

    private Jogador j1;
    private Jogador j2;

    // Helpers
    private List<Integer> roll(int... vals) {
        return Arrays.asList(Arrays.stream(vals).boxed().toArray(Integer[]::new));
    }

    private Terreno terreno(String nome, int preco, int valorCasa, int aluguelBase, int pos) {
        Terreno t = new Terreno(nome, preco, valorCasa, aluguelBase, pos);
        tabuleiro.addPropriedade(t);
        return t;
    }

    private Propriedade empresa(String nome, int preco, int aluguelBase, int pos) {
        Propriedade p = new Propriedade(nome, preco, aluguelBase, pos);
        tabuleiro.addPropriedade(p);
        return p;
    }

    @Before
    public void setUp() {
        banco = new Banco();
        tabuleiro = new Tabuleiro();
        motor = new MotorDeJogo(banco, tabuleiro);

        j1 = new Jogador("A");
        j2 = new Jogador("B");

        tabuleiro.addJogador(j1);
        tabuleiro.addJogador(j2);
    }

    // 1) Dados: 2 valores em [1..6]
    @Test
    public void testLancarDados() {
        List<Integer> d = motor.lancarDados();
        assertEquals(2, d.size());
        assertTrue(d.get(0) >= 1 && d.get(0) <= 6);
        assertTrue(d.get(1) >= 1 && d.get(1) <= 6);
    }

    // 2) Movimento: soma de dados + wrap + bônus ao passar pela saída
    @Test
    public void testMovimentoWrapEPassarSaida() {
        j1.setPosicao(Tabuleiro.getNumCasas() - 1); // 39
        int saldoAntes = j1.getConta().getSaldo();

        motor.moverJogador(j1, roll(2)); // 39 + 2 -> 1 (passa pela saída)
        assertEquals(1, j1.getPosicao());
        assertEquals(saldoAntes + 200, j1.getConta().getSaldo()); // bônus
    }

    // 2b) Movimento bloqueado se preso
    @Test
    public void testMovimentoBloqueadoSePreso() {
        j1.prende();
        j1.setPosicao(5);
        motor.moverJogador(j1, roll(6, 6));
        assertEquals(5, j1.getPosicao()); // não se move
    }

    // 3) Comprar propriedade disponível (voluntário): paga e vira dono
    @Test
    public void testCompraPropriedadeDisponivel() {
        Propriedade p = empresa("Mercado", 300, 20, 7);
        int saldoJ1 = j1.getConta().getSaldo();

        motor.comprarPropriedade(j1, p);

        assertEquals(j1, p.getProprietario());
        assertEquals(saldoJ1 - 300, j1.getConta().getSaldo());
        assertEquals(200000 + 300, banco.getSaldo());
    }

    // 3b) Compra com saldo insuficiente (voluntário): não compra, não falência
    @Test
    public void testCompraInsuficienteNaoFali() {
        Propriedade p = empresa("Aeroporto", 999_999, 50, 9);
        int saldoJ1 = j1.getConta().getSaldo();

        motor.comprarPropriedade(j1, p);

        assertNull(p.getProprietario());
        assertEquals(saldoJ1, j1.getConta().getSaldo());
        assertTrue(tabuleiro.estaNoJogo(j1));
        assertFalse(j1.isFalido());
    }

    // 4) Construção: só no terreno onde está, sendo dono, e paga valorCasa
    @Test
    public void testConstruirCasaBasico() {
        Terreno t = terreno("Vila Azul", 120, 50, 20, 4);

        // j1 compra e se posiciona na casa
        motor.comprarPropriedade(j1, t);
        j1.setPosicao(4);
        int saldoAntes = j1.getConta().getSaldo();

        motor.construirCasa(j1, t);

        assertEquals(1, t.getNumCasas());
        assertEquals(saldoAntes - t.getValorCasa(), j1.getConta().getSaldo());
    }

    // 4b) Construção falha se não estiver na mesma casa
    @Test
    public void testConstruirCasaForaDaCasaNaoConstrui() {
        Terreno t = terreno("Jardim Verde", 140, 50, 22, 6);
        motor.comprarPropriedade(j1, t);
        j1.setPosicao(5); // não está na casa
        int casasAntes = t.getNumCasas();

        motor.construirCasa(j1, t);

        assertEquals(casasAntes, t.getNumCasas());
    }

    // 5) Aluguel automático: terreno cobra somente se >=1 casa
    @Test
    public void testAluguelTerrenoComCasa() {
        Terreno t = terreno("Centro", 180, 100, 30, 11);
        motor.comprarPropriedade(j2, t); // j2 é dono
        j1.setPosicao(9);

        // j2 constrói 1 casa (paga valorCasa)
        j2.setPosicao(11);
        motor.construirCasa(j2, t);
        assertEquals(1, t.getNumCasas());

        int saldoJ1 = j1.getConta().getSaldo();
        int saldoJ2 = j2.getConta().getSaldo();

        // j1 cai na posição 11
        motor.moverJogador(j1, roll(2)); // 9 -> 11
        // Aluguel = aluguelBase * numCasas = 30 * 1 = 30
        assertEquals(saldoJ1 - 30, j1.getConta().getSaldo());
        assertEquals(saldoJ2 + 30, j2.getConta().getSaldo());
    }

    // 5b) Empresa/Propriedade genérica cobra aluguelBase (calculaAluguel)
    @Test
    public void testAluguelEmpresaGenerica() {
        Propriedade p = empresa("Estação", 200, 25, 8);
        motor.comprarPropriedade(j2, p);
        j1.setPosicao(6);

        int saldoJ1 = j1.getConta().getSaldo();
        int saldoJ2 = j2.getConta().getSaldo();

        motor.moverJogador(j1, roll(2)); // 6 -> 8
        assertEquals(saldoJ1 - 25, j1.getConta().getSaldo());
        assertEquals(saldoJ2 + 25, j2.getConta().getSaldo());
    }

    // 6) Prisão: cair na casa "vai pra prisão" prende e não cobra aluguel
    @Test
    public void testCairNaCasaVaiPraPrisao() {
        int posVaiPrisao = Tabuleiro.getPosicaoPrisao(); // 26
        // garantir que o movimento leva até lá
        j1.setPosicao(posVaiPrisao - 2);
        motor.moverJogador(j1, roll(2)); // cai na 26 -> prende
        assertTrue(j1.estaPreso());
    }

    // 7) Falência: aluguel obrigatório com saldo insuficiente => falido e removido
    @Test
    public void testFalenciaPorAluguel() {
        Terreno t = terreno("Zona Sul", 300, 200, 50, 27);
        motor.comprarPropriedade(j2, t);
        j2.setPosicao(27);
        motor.construirCasa(j2, t); // 1 casa -> aluguel 50

        // zera saldo do j1 para forçar falência ao pagar
        j1.getConta().setSaldo(0);
        j1.setPosicao(25);

        // mover j1 para 27 => aluguel devido, falência imediata
        motor.moverJogador(j1, roll(2)); // 25 -> 27

        assertTrue(j1.isFalido());
        assertFalse(tabuleiro.estaNoJogo(j1)); // removido do jogo
        // propriedades do j1 (se houvesse) deveriam ter sido limpas — verificado indiretamente pelo remove
    }
}
