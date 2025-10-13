package Model;

import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

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

    // 1) Lançar dados: 2 valores ∈ [1..6]
    @Test
    public void testLancarDados() {
        List<Integer> d = motor.lancarDados();
        assertEquals(2, d.size());
        assertTrue(d.get(0) >= 1 && d.get(0) <= 6);
        assertTrue(d.get(1) >= 1 && d.get(1) <= 6);
    }

    // 2a) Movimento simples: jogador avança corretamente sem passar pela saída
    @Test
    public void testMovimentoSimples() {
        j1.setPosicao(5);

        // Dados = 3 + 2 = 5 → nova posição = 10
        List<Integer> dados = new ArrayList<>();
        dados.add(3);
        dados.add(2);
        motor.moverJogador(j1, dados);

        assertEquals(10, j1.getPosicao());
    }
    // 2b) Movimento com wrap: jogador passa pela saída e recebe bônus
    @Test
    public void testMovimentoComWrapEPassarSaida() {
        j1.setPosicao(Tabuleiro.getNumCasas() - 1); // 39
        int saldoAntes = j1.getConta().getSaldo();

        // Soma = 2 → 39 + 2 = 41 → posição final = 1 (passou pela saída)
        List<Integer> dados = new ArrayList<>();
        dados.add(1);
        dados.add(1);
        motor.moverJogador(j1, dados);

        assertEquals(1, j1.getPosicao());
        assertEquals(saldoAntes + 200, j1.getConta().getSaldo());
    }
    // 2c) Movimento com dados nulos: função deve apenas retornar sem erro
    @Test
    public void testMovimentoComDadosNulos() {
        j1.setPosicao(5);
        motor.moverJogador(j1, null); // deve ser ignorado silenciosamente
        assertEquals(5, j1.getPosicao());
    }
    // 3) Compra de propriedade disponível (voluntário): paga e vira dono
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

    // 3b) Compra com saldo insuficiente (voluntário): não compra, sem falência
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

    // 3c) Tentativa de compra de propriedade que já possui dono: deve ser ignorada
    @Test
    public void testCompraPropriedadeJaTemDono() {
        Propriedade p = new Propriedade("Shopping", 400, 25, 8);
        tabuleiro.addPropriedade(p);

        // j2 compra primeiro
        motor.comprarPropriedade(j2, p);
        assertEquals(j2, p.getProprietario());

        // j1 tenta comprar novamente
        int saldoJ1Antes = j1.getConta().getSaldo();
        int saldoJ2Antes = j2.getConta().getSaldo();
        int saldoBancoAntes = banco.getSaldo();

        motor.comprarPropriedade(j1, p); // não deve alterar nada

        assertEquals(j2, p.getProprietario()); // dono permanece
        assertEquals(saldoJ1Antes, j1.getConta().getSaldo());
        assertEquals(saldoJ2Antes, j2.getConta().getSaldo());
        assertEquals(saldoBancoAntes, banco.getSaldo());
    }


    // 4) Construção: no terreno onde está, sendo dono, paga valorCasa
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

    // 4b) Construção falha se não estiver na mesma casa
    @Test
    public void testConstruirCasaForaDaCasaNaoConstrui() {
        Terreno t = new Terreno("Jardim Verde", 140, 50, 22, 6);
        tabuleiro.addPropriedade(t);

        motor.comprarPropriedade(j1, t);
        j1.setPosicao(5);

        motor.construirCasa(j1, t);
        assertEquals(0, t.getNumCasas());
    }
    
    // 4c) Tentar construir em um terreno que NÃO pertence ao jogador: não deve adicionar casa
    @Test
    public void testConstruirCasaEmTerrenoDeOutroJogador() {
        Terreno t = new Terreno("Campo Dourado", 200, 100, 40, 10);
        tabuleiro.addPropriedade(t);

        // j2 compra o terreno, j1 tenta construir
        motor.comprarPropriedade(j2, t);
        j1.setPosicao(10);
        int casasAntes = t.getNumCasas();

        motor.construirCasa(j1, t);

        assertEquals(casasAntes, t.getNumCasas()); // nada muda
        assertEquals(j2, t.getProprietario());
    }

    // 5) Aluguel automático: terreno só cobra se >=1 casa
    @Test
    public void testAluguelTerrenoComCasa() {
        Terreno t = new Terreno("Centro", 180, 100, 30, 11);
        tabuleiro.addPropriedade(t);

        motor.comprarPropriedade(j2, t);
        j2.setPosicao(11);
        motor.construirCasa(j2, t); // 1 casa

        int saldoJ1 = j1.getConta().getSaldo();
        int saldoJ2 = j2.getConta().getSaldo();

        j1.setPosicao(9);
        List<Integer> dados = new ArrayList<>();
        dados.add(1); dados.add(1); // 9 -> 11
        motor.moverJogador(j1, dados);

        assertEquals(saldoJ1 - 30, j1.getConta().getSaldo());
        assertEquals(saldoJ2 + 30, j2.getConta().getSaldo());
    }

    // 5b) Empresa/Propriedade genérica cobra aluguelBase (via calculaAluguel)
    @Test
    public void testAluguelPropriedadeGenerica() {
        Propriedade p = new Propriedade("Estação", 200, 25, 8);
        tabuleiro.addPropriedade(p);

        motor.comprarPropriedade(j2, p);
        j1.setPosicao(6);

        int s1 = j1.getConta().getSaldo();
        int s2 = j2.getConta().getSaldo();

        List<Integer> dados = new ArrayList<>();
        dados.add(1); dados.add(1); // 6 -> 8
        motor.moverJogador(j1, dados);

        assertEquals(s1 - 25, j1.getConta().getSaldo());
        assertEquals(s2 + 25, j2.getConta().getSaldo());
    }
    
    // 5c) Tentar cobrar aluguel de um terreno com 0 casas: não deve cobrar
    @Test
    public void testNaoCobraAluguelSemCasas() {
        Terreno t = new Terreno("Bairro Vazio", 150, 100, 25, 12);
        tabuleiro.addPropriedade(t);

        // j2 é dono, mas ainda sem casas
        motor.comprarPropriedade(j2, t);
        j1.setPosicao(10);

        int saldoJ1Antes = j1.getConta().getSaldo();
        int saldoJ2Antes = j2.getConta().getSaldo();

        // j1 cai na posição 12
        List<Integer> dados = new ArrayList<>();
        dados.add(1); dados.add(1); // 10 -> 12
        motor.moverJogador(j1, dados);

        // Nenhum valor deve mudar
        assertEquals(saldoJ1Antes, j1.getConta().getSaldo());
        assertEquals(saldoJ2Antes, j2.getConta().getSaldo());
    }


    // 6) Prisão: cair em "vai pra prisão" prende
    @Test
    public void testCairNaCasaVaiPraPrisao() {
        int posVaiPrisao = Tabuleiro.getPosicaoPrisao(); // 26
        j1.setPosicao(posVaiPrisao - 2);

        List<Integer> dados = new ArrayList<>();
        dados.add(1); dados.add(1);
        motor.moverJogador(j1, dados);

        assertTrue(j1.estaPreso());
        assertEquals(Tabuleiro.getPosicaoVisitaPrisao(), j1.getPosicao());
    }

    // 6b) Cartas: baralho de teste aplica VAI_PARA_PRISAO e SAIDA_LIVRE
    @Test
    public void testBaralhoSorteRevesBasico() {
        tabuleiro.inicializarBaralhoTeste();

        // 1ª carta: VAI_PARA_PRISAO
        motor.puxarSorteReves(j1);
        assertTrue(j1.estaPreso());

        // 2ª carta: SAIDA_LIVRE (guarda carta)
        motor.puxarSorteReves(j1);
        assertEquals(1, j1.getCartasLiberacao());

        // Usa a carta para sair
        boolean usou = motor.usarCartaLiberacao(j1);
        assertTrue(usou);
        assertFalse(j1.estaPreso());
        assertEquals(0, j1.getCartasLiberacao());
    }

    // 7) Falência: aluguel obrigatório com saldo insuficiente => falido e removido
    @Test
    public void testFalenciaPorAluguel() {
        Terreno t = new Terreno("Zona Sul", 300, 200, 50, 27);
        tabuleiro.addPropriedade(t);

        motor.comprarPropriedade(j2, t);
        j2.setPosicao(27);
        motor.construirCasa(j2, t); // 1 casa -> aluguel 50

        j1.getConta().setSaldo(0);
        j1.setPosicao(25);

        List<Integer> dados = new ArrayList<>();
        dados.add(1); dados.add(1); // 25 -> 27
        motor.moverJogador(j1, dados);

        assertTrue(j1.isFalido());
        assertFalse(tabuleiro.estaNoJogo(j1));
    }
}
