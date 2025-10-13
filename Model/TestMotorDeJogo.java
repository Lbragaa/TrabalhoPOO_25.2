package Model;

import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

/**
 * Classe de testes unitários para o componente Model (MotorDeJogo).
 * 
 * Este conjunto de testes valida todas as funcionalidades obrigatórias da 1ª iteração do projeto Banco Imobiliário.
 * Cada teste cobre uma das principais regras:
 * 
 * 1. Lançamento dos dados
 * 2. Movimentação dos jogadores (simples, wrap, dados nulos)
 * 3. Compra de propriedades (disponível, sem saldo, já com dono)
 * 4. Construção de casas (válida, fora da casa, em terreno de outro jogador)
 * 5. Pagamento automático de aluguel (terrenos e propriedades genéricas)
 * 6. Regras de prisão e cartas Sorte/Reves
 * 7. Falência automática
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
     * 1️ Testa o lançamento de dados.
     * Verifica se o método retorna exatamente 2 valores dentro do intervalo válido (1–6).
     */
    @Test
    public void testLancarDados() {
        List<Integer> d = motor.lancarDados();
        assertEquals(2, d.size());
        assertTrue(d.get(0) >= 1 && d.get(0) <= 6);
        assertTrue(d.get(1) >= 1 && d.get(1) <= 6);
    }

    /**
     * 2️ Testa movimento simples.
     * O jogador deve avançar a soma dos dados sem ultrapassar o final do tabuleiro.
     */
    @Test
    public void testMovimentoSimples() {
        j1.setPosicao(5);

        List<Integer> dados = new ArrayList<>();
        dados.add(3);
        dados.add(2); // soma = 5 → nova posição = 10
        motor.moverJogador(j1, dados);

        assertEquals(10, j1.getPosicao());
    }

    /**
     * 2️ Testa movimento com wrap (passar pela saída).
     * O jogador que ultrapassa a última casa deve retornar ao início (posição 0)
     * e receber o bônus de 200 unidades monetárias.
     */
    @Test
    public void testMovimentoComWrapEPassarSaida() {
        j1.setPosicao(Tabuleiro.getNumCasas() - 1); // posição inicial: 39
        int saldoAntes = j1.getConta().getSaldo();

        List<Integer> dados = new ArrayList<>();
        dados.add(1);
        dados.add(1); // soma = 2 → nova posição = 1
        motor.moverJogador(j1, dados);

        assertEquals(1, j1.getPosicao());
        assertEquals(saldoAntes + 200, j1.getConta().getSaldo());
    }

    /**
     * 2️ Testa proteção contra dados nulos.
     * Se os dados forem nulos, o método deve retornar silenciosamente sem lançar exceção.
     */
    @Test
    public void testMovimentoComDadosNulos() {
        j1.setPosicao(5);
        motor.moverJogador(j1, null);
        assertEquals(5, j1.getPosicao());
    }

    /**
     * 3️ Testa compra de propriedade disponível.
     * O jogador paga o preço e se torna o proprietário.
     * 
     * Parâmetros do construtor:
     * ("Mercado", 300, 20, 7)
     * nome = "Mercado"
     * preço = 300
     * aluguelBase = 20
     * posição = 7
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
     * 3️ Testa tentativa de compra sem saldo suficiente.
     * O jogador não deve conseguir comprar, não deve falir e o saldo deve permanecer o mesmo.
     * 
     * Parâmetros:
     * ("Aeroporto", 999_999, 50, 9)
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
     * 3️ Testa tentativa de compra de propriedade já com dono.
     * Deve ser ignorada: sem mudança de saldo e sem troca de dono.
     * 
     * Parâmetros:
     * ("Shopping", 400, 25, 8)
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
     * 4️ Testa construção de casa em terreno válido.
     * O jogador é o dono, está na posição correta e paga o valor da casa.
     * 
     * Parâmetros:
     * ("Vila Azul", 120, 50, 20, 4)
     * nome = "Vila Azul"
     * preço = 120
     * valorCasa = 50
     * aluguelBase = 20
     * posição = 4
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
     * 4️ Testa tentativa de construção em uma casa diferente da posição atual.
     * Não deve adicionar casas nem alterar o saldo.
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
     * 4️ Testa tentativa de construção em terreno de outro jogador.
     * O método deve ser ignorado e o número de casas permanecer inalterado.
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
     * 5️ Testa cobrança automática de aluguel em terreno com casa.
     * O jogador que cai paga o aluguel base multiplicado pelo número de casas.
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
        dados.add(1); dados.add(1); // 9 → 11
        motor.moverJogador(j1, dados);

        assertEquals(saldoJ1 - 30, j1.getConta().getSaldo());
        assertEquals(saldoJ2 + 30, j2.getConta().getSaldo());
    }

    /**
     * 5️ Testa cobrança de aluguel em propriedade genérica (empresa).
     * O valor cobrado é o aluguel base configurado.
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
        dados.add(1); dados.add(1); // 6 → 8
        motor.moverJogador(j1, dados);

        assertEquals(s1 - 25, j1.getConta().getSaldo());
        assertEquals(s2 + 25, j2.getConta().getSaldo());
    }

    /**
     * 5️ Testa que terrenos com 0 casas não geram cobrança de aluguel.
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
        dados.add(1); dados.add(1); // 10 → 12
        motor.moverJogador(j1, dados);

        assertEquals(saldoJ1Antes, j1.getConta().getSaldo());
        assertEquals(saldoJ2Antes, j2.getConta().getSaldo());
    }

    /**
     * 6️ Testa casa "Vai para Prisão".
     * Ao cair nela, o jogador deve ser preso automaticamente e movido para a posição da prisão.
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
     * 6️ Testa carta Sorte/Reves "Vai para Prisão": 
     * o jogador deve ser preso imediatamente.
     */
    @Test
    public void testCartaVaiParaPrisao() {
        tabuleiro.inicializarBaralhoTeste();
        motor.puxarSorteReves(j1);

        assertTrue(j1.estaPreso());
        assertEquals(Tabuleiro.getPosicaoVisitaPrisao(), j1.getPosicao());
    }

    /**
     * 6️ Testa carta Sorte/Reves "Saída Livre":
     * o jogador guarda a carta e pode utilizá-la para sair da prisão.
     */
    @Test
    public void testCartaSaidaLivre() {
        tabuleiro.inicializarBaralhoTeste();

        motor.puxarSorteReves(j1); // consome "Vai para Prisão"
        motor.puxarSorteReves(j1); // próxima é "Saída Livre"

        assertEquals(1, j1.getCartasLiberacao());

        j1.prende();
        boolean usou = motor.usarCartaLiberacao(j1);

        assertTrue(usou);
        assertFalse(j1.estaPreso());
        assertEquals(0, j1.getCartasLiberacao());
    }

    /**
     * 6️ Testa soltura por dupla:
     * jogador preso deve ser liberado ao tirar dois dados iguais.
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
     * 6️ Testa que o jogador permanece preso se não tirar uma dupla.
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
     * 7 Testa falência automática:
     * quando o jogador cai em um terreno de outro jogador e não tem saldo suficiente,
     * ele deve ser declarado falido e removido do jogo.
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
        dados.add(1); dados.add(1); // 25 → 27
        motor.moverJogador(j1, dados);

        assertTrue(j1.isFalido());
        assertFalse(tabuleiro.estaNoJogo(j1));
    }
}
