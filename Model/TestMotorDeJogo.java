package Model;

import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

/**
 * Classe de testes unitários para o componente Model (MotorDeJogo).
 * 
 * Testa todas as principais regras exigidas na 1ª iteração:
 * 1. Lançamento dos dados
 * 2. Movimentação dos jogadores
 * 3. Compra de propriedades
 * 4. Construção de casas
 * 5. Pagamento automático de aluguel
 * 6. Regras de prisão e cartas Sorte/Reves
 * 7. Falência
 */
public class TestMotorDeJogo {

    private Banco banco;
    private Tabuleiro tabuleiro;
    private MotorDeJogo motor;

    private Jogador j1;
    private Jogador j2;

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

    /** 
     * Testa o lançamento de dados: devem ser 2 valores no intervalo [1..6].
     */
    @Test
    public void testLancarDados() {
        List<Integer> d = motor.lancarDados();
        assertEquals(2, d.size());
        assertTrue(d.get(0) >= 1 && d.get(0) <= 6);
        assertTrue(d.get(1) >= 1 && d.get(1) <= 6);
    }

    /** 
     * Testa movimento simples: jogador avança a soma dos dados, sem passar pela saída.
     */
    @Test
    public void testMovimentoSimples() {
        j1.setPosicao(5);

        List<Integer> dados = new ArrayList<>();
        dados.add(3);
        dados.add(2);
        motor.moverJogador(j1, dados);

        assertEquals(10, j1.getPosicao());
    }

    /** 
     * Testa movimento com wrap: jogador passa pela saída e recebe bônus de 200.
     */
    @Test
    public void testMovimentoComWrapEPassarSaida() {
        j1.setPosicao(Tabuleiro.getNumCasas() - 1);
        int saldoAntes = j1.getConta().getSaldo();

        List<Integer> dados = new ArrayList<>();
        dados.add(1);
        dados.add(1);
        motor.moverJogador(j1, dados);

        assertEquals(1, j1.getPosicao());
        assertEquals(saldoAntes + 200, j1.getConta().getSaldo());
    }

    /** 
     * Testa proteção contra dados nulos: deve ignorar o movimento sem lançar erro.
     */
    @Test
    public void testMovimentoComDadosNulos() {
        j1.setPosicao(5);
        motor.moverJogador(j1, null);
        assertEquals(5, j1.getPosicao());
    }

    /** 
     * Testa compra de propriedade disponível: jogador paga e vira dono.
     */
    @Test
    public void testCompraPropriedadeDisponivel() {
        Propriedade p = new Propriedade("Mercado", 300, 20, 7);
        tabuleiro.addPropriedade(p);
        int saldoAntes = j1.getConta().getSaldo();

        motor.comprarPropriedade(j1, p);

        assertEquals(j1, p.getProprietario());
        assertEquals(saldoAntes - 300, j1.getConta().getSaldo());
        assertEquals(200000 + 300, banco.getSaldo());
    }

    /** 
     * Testa tentativa de compra sem saldo suficiente: deve falhar sem causar falência.
     */
    @Test
    public void testCompraInsuficienteNaoFali() {
        Propriedade p = new Propriedade("Aeroporto", 999_999, 50, 9);
        tabuleiro.addPropriedade(p);
        int saldoAntes = j1.getConta().getSaldo();

        motor.comprarPropriedade(j1, p);

        assertNull(p.getProprietario());
        assertEquals(saldoAntes, j1.getConta().getSaldo());
        assertTrue(tabuleiro.estaNoJogo(j1));
        assertFalse(j1.isFalido());
    }

    /** 
     * Testa tentativa de compra de propriedade já com dono: operação deve ser ignorada.
     */
    @Test
    public void testCompraPropriedadeJaTemDono() {
        Propriedade p = new Propriedade("Shopping", 400, 25, 8);
        tabuleiro.addPropriedade(p);

        motor.comprarPropriedade(j2, p);
        assertEquals(j2, p.getProprietario());

        int saldoJ1Antes = j1.getConta().getSaldo();
        int saldoJ2Antes = j2.getConta().getSaldo();
        int saldoBancoAntes = banco.getSaldo();

        motor.comprarPropriedade(j1, p);

        assertEquals(j2, p.getProprietario());
        assertEquals(saldoJ1Antes, j1.getConta().getSaldo());
        assertEquals(saldoJ2Antes, j2.getConta().getSaldo());
        assertEquals(saldoBancoAntes, banco.getSaldo());
    }

    /** 
     * Testa construção de casa: jogador é dono e está na posição do terreno.
     */
    @Test
    public void testConstruirCasaBasico() {
        Terreno t = new Terreno("Vila Azul", 120, 50, 20, 4);
        tabuleiro.addPropriedade(t);

        motor.comprarPropriedade(j1, t);
        j1.setPosicao(4);
        int saldoAntes = j1.getConta().getSaldo();

        motor.construirCasa(j1, t);

        assertEquals(1, t.getNumCasas());
        assertEquals(saldoAntes - t.getValorCasa(), j1.getConta().getSaldo());
    }

    /** 
     * Testa tentativa de construção fora da casa atual: não deve adicionar casas.
     */
    @Test
    public void testConstruirCasaForaDaCasaNaoConstrui() {
        Terreno t = new Terreno("Jardim Verde", 140, 50, 22, 6);
        tabuleiro.addPropriedade(t);

        motor.comprarPropriedade(j1, t);
        j1.setPosicao(5);

        motor.construirCasa(j1, t);
        assertEquals(0, t.getNumCasas());
    }

    /** 
     * Testa tentativa de construção em terreno que pertence a outro jogador: ignorado.
     */
    @Test
    public void testConstruirCasaEmTerrenoDeOutroJogador() {
        Terreno t = new Terreno("Campo Dourado", 200, 100, 40, 10);
        tabuleiro.addPropriedade(t);

        motor.comprarPropriedade(j2, t);
        j1.setPosicao(10);
        int casasAntes = t.getNumCasas();

        motor.construirCasa(j1, t);

        assertEquals(casasAntes, t.getNumCasas());
        assertEquals(j2, t.getProprietario());
    }

    /** 
     * Testa cobrança de aluguel em terreno com casa: jogador paga ao dono.
     */
    @Test
    public void testAluguelTerrenoComCasa() {
        Terreno t = new Terreno("Centro", 180, 100, 30, 11);
        tabuleiro.addPropriedade(t);

        motor.comprarPropriedade(j2, t);
        j2.setPosicao(11);
        motor.construirCasa(j2, t);

        int saldoJ1 = j1.getConta().getSaldo();
        int saldoJ2 = j2.getConta().getSaldo();

        j1.setPosicao(9);
        List<Integer> dados = new ArrayList<>();
        dados.add(1); dados.add(1);
        motor.moverJogador(j1, dados);

        assertEquals(saldoJ1 - 30, j1.getConta().getSaldo());
        assertEquals(saldoJ2 + 30, j2.getConta().getSaldo());
    }

    /** 
     * Testa cobrança de aluguel em propriedade genérica (empresa).
     */
    @Test
    public void testAluguelPropriedadeGenerica() {
        Propriedade p = new Propriedade("Estação", 200, 25, 8);
        tabuleiro.addPropriedade(p);

        motor.comprarPropriedade(j2, p);
        j1.setPosicao(6);

        int s1 = j1.getConta().getSaldo();
        int s2 = j2.getConta().getSaldo();

        List<Integer> dados = new ArrayList<>();
        dados.add(1); dados.add(1);
        motor.moverJogador(j1, dados);

        assertEquals(s1 - 25, j1.getConta().getSaldo());
        assertEquals(s2 + 25, j2.getConta().getSaldo());
    }

    /** 
     * Testa tentativa de cobrança de aluguel de terreno sem casas: não deve cobrar.
     */
    @Test
    public void testNaoCobraAluguelSemCasas() {
        Terreno t = new Terreno("Bairro Vazio", 150, 100, 25, 12);
        tabuleiro.addPropriedade(t);

        motor.comprarPropriedade(j2, t);
        j1.setPosicao(10);

        int saldoJ1Antes = j1.getConta().getSaldo();
        int saldoJ2Antes = j2.getConta().getSaldo();

        List<Integer> dados = new ArrayList<>();
        dados.add(1); dados.add(1);
        motor.moverJogador(j1, dados);

        assertEquals(saldoJ1Antes, j1.getConta().getSaldo());
        assertEquals(saldoJ2Antes, j2.getConta().getSaldo());
    }

    /** 
     * Testa cair na casa "Vai para Prisão": jogador deve ser preso automaticamente.
     */
    @Test
    public void testCairNaCasaVaiPraPrisao() {
        int posVaiPrisao = Tabuleiro.getPosicaoPrisao();
        j1.setPosicao(posVaiPrisao - 2);

        List<Integer> dados = new ArrayList<>();
        dados.add(1); dados.add(1);
        motor.moverJogador(j1, dados);

        assertTrue(j1.estaPreso());
        assertEquals(Tabuleiro.getPosicaoVisitaPrisao(), j1.getPosicao());
    }

    /** 
     * Testa carta Sorte/Reves "Vai para Prisão": prende o jogador.
     */
    @Test
    public void testCartaVaiParaPrisao() {
        tabuleiro.inicializarBaralhoTeste();

        motor.puxarSorteReves(j1);

        assertTrue(j1.estaPreso());
        assertEquals(Tabuleiro.getPosicaoVisitaPrisao(), j1.getPosicao());
    }

    /** 
     * Testa carta Sorte/Reves "Saída Livre": jogador guarda e consegue usá-la.
     */
    @Test
    public void testCartaSaidaLivre() {
        tabuleiro.inicializarBaralhoTeste();

        motor.puxarSorteReves(j1); // consome "Vai para Prisão"
        motor.puxarSorteReves(j1); // agora "Saída Livre"

        assertEquals(1, j1.getCartasLiberacao());

        j1.prende();
        boolean usou = motor.usarCartaLiberacao(j1);

        assertTrue(usou);
        assertFalse(j1.estaPreso());
        assertEquals(0, j1.getCartasLiberacao());
    }

    /** 
     * Testa soltura por dupla: jogador preso é liberado ao tirar dois dados iguais.
     */
    @Test
    public void testSoltarSeDupla() {
        j1.prende();
        assertTrue(j1.estaPreso());

        List<Integer> dadosDupla = new ArrayList<>();
        dadosDupla.add(4);
        dadosDupla.add(4);

        boolean liberado = motor.soltarSeDupla(j1, dadosDupla);

        assertTrue(liberado);
        assertFalse(j1.estaPreso());
    }

    /** 
     * Testa que o jogador continua preso se não tirar uma dupla.
     */
    @Test
    public void testNaoSoltaSemDupla() {
        j1.prende();
        assertTrue(j1.estaPreso());

        List<Integer> dadosNormais = new ArrayList<>();
        dadosNormais.add(3);
        dadosNormais.add(5);

        boolean liberado = motor.soltarSeDupla(j1, dadosNormais);

        assertFalse(liberado);
        assertTrue(j1.estaPreso());
    }

    /** 
     * Testa falência automática ao cair em terreno de outro jogador sem saldo.
     */
    @Test
    public void testFalenciaPorAluguel() {
        Terreno t = new Terreno("Zona Sul", 300, 200, 50, 27);
        tabuleiro.addPropriedade(t);

        motor.comprarPropriedade(j2, t);
        j2.setPosicao(27);
        motor.construirCasa(j2, t);

        j1.getConta().setSaldo(0);
        j1.setPosicao(25);

        List<Integer> dados = new ArrayList<>();
        dados.add(1); dados.add(1);
        motor.moverJogador(j1, dados);

        assertTrue(j1.isFalido());
        assertFalse(tabuleiro.estaNoJogo(j1));
    }
}
