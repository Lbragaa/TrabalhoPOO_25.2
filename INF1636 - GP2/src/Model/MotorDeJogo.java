package Model;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

/**
 * Motor com as regras operacionais: rolar dados, mover, prisão, aluguel, compra,
 * construir, cartas, saída por dupla e falência.
 * <p>É usado pela {@link GameFacade} para executar ações do turno.</p>
 */
public class MotorDeJogo {

    private final Banco banco;
    private final Tabuleiro tabuleiro;
    private final Random rng = new Random(); // reuso

    public MotorDeJogo(Banco banco, Tabuleiro tabuleiro) {
        this.banco = banco;
        this.tabuleiro = tabuleiro;
    }

    /** Lança dois dados de 6 faces. */
    public List<Integer> lancarDados() {
        List<Integer> dados = new ArrayList<>(2);
        dados.add(rng.nextInt(6) + 1);
        dados.add(rng.nextInt(6) + 1);
        return dados;
    }

    /** Verifica dupla (mesmo valor nos 2 dados). */
    private static boolean ehDupla(List<Integer> dados) {
        return dados != null && dados.size() >= 2
            && dados.get(0) != null && dados.get(1) != null
            && dados.get(0).intValue() == dados.get(1).intValue();
    }

    /** Move e resolve "vá para a prisão" + aluguel. */
    public void moverJogador(Jogador jogador, List<Integer> dados) {
        if (jogador == null || dados == null || dados.isEmpty()) return;

        if (jogador.estaPreso()) {
            // em GUI evitamos prints/logs
            return;
        }

        int somaDados = 0;
        for (Integer d : dados) if (d != null) somaDados += d;

        jogador.move(somaDados, banco);
        verificarPrisao(jogador);
        if (jogador.estaPreso()) return;

        // Isso aqui tava fazendo pagar duas vezes, ja que no game facade tambem tinha uma outra cobranca.
//        Propriedade propriedade = tabuleiro.getPropriedadeNaPosicao(jogador.getPosicao());
//        if (propriedade != null) pagarAluguel(jogador, propriedade);
    }

    /** Compra propriedade sem dono. */
    public void comprarPropriedade(Jogador jogador, Propriedade propriedade) {
        if (propriedade.estaDisponivel()) {
            boolean pagou = jogador.getConta().paga(banco.getConta(), propriedade.getPreco());
            if (pagou) propriedade.setProprietario(jogador);
            else verificarFalencia(jogador);
        }
    }

    /** Constrói casa no terreno onde o jogador está (se puder e pagar). */
    public void construirCasa(Jogador jogador, Propriedade propriedade) {
        Propriedade atual = tabuleiro.getPropriedadeNaPosicao(jogador.getPosicao());
        if (propriedade != atual) return;
        if (propriedade instanceof Terreno terreno) {
            if (terreno.getProprietario() == jogador && terreno.podeConstruir()) {
                boolean pagou = jogador.getConta().paga(banco.getConta(), terreno.getValorCasa());
                if (pagou) terreno.adicionaCasa(); else verificarFalencia(jogador);
            }
        }
    }

    /**
     * Cobrança de aluguel (regra existente):
     * - Terreno só cobra se tiver ≥ 1 casa;
     * - Demais propriedades usam sua própria implementação de cálculo.
     */
    public void pagarAluguel(Jogador jogador, Propriedade propriedade) {
        if (jogador == null || propriedade == null) return;

        Jogador dono = propriedade.proprietario;
        if (dono == null || dono == jogador) return;

        int valorAPagar = 0;
        if (propriedade instanceof Terreno terreno) {
            if (terreno.getNumCasas() >= 1) valorAPagar = terreno.calculaAluguel(); else return;
        } else {
            valorAPagar = propriedade.calculaAluguel();
        }

        boolean pagou = jogador.getConta().paga(dono.getConta(), valorAPagar);
        if (!pagou) {
            // sinalizamos falência sem forçar saldo negativo
            jogador.setFalido(true);
            verificarFalencia(jogador);
        }
    }

    /** Checa casa "vá para a prisão". */
    public void verificarPrisao(Jogador jogador) {
        if (jogador == null) return;
        if (tabuleiro.isCasaPrisao(jogador.getPosicao())) jogador.prende();
    }

    /** Se estiver preso, sai com dupla. */
    public boolean soltarSeDupla(Jogador jogador, List<Integer> dados) {
        if (jogador == null || !jogador.estaPreso()) return false;
        if (ehDupla(dados)) { jogador.solta(); return true; }
        return false;
    }

    /** Puxa carta, aplica o efeito e retorna a carta sorteada. */
    public Carta puxarSorteReves(Jogador j) {
        Carta c = tabuleiro.comprarCartaSorteReves();
        switch (c.tipo) {
            case VAI_PARA_PRISAO -> j.prende();
            case SAIDA_LIVRE     -> j.adicionarCartaLiberacao();
            case PAGAR           -> { j.getConta().paga(banco.getConta(), c.valor); verificarFalencia(j); }
            case RECEBER         -> banco.getConta().paga(j.getConta(), c.valor);
            case RECEBER_DE_CADA -> {
                for (Jogador outro : tabuleiro.getJogadoresAtivos()) {
                    if (outro == j) continue;
                    boolean ok = outro.getConta().paga(j.getConta(), c.valor);
                    if (!ok) { outro.setFalido(true); verificarFalencia(outro); }
                }
            }
        }
        return c;
    }

    /** Usa carta de saída livre, devolvendo-a ao fim do baralho. */
    public boolean usarCartaLiberacao(Jogador j) {
        if (j == null || !j.estaPreso()) return false;
        if (!j.consumirCartaLiberacao()) return false;
        tabuleiro.devolverCartaLiberacao();
        j.solta();
        return true;
    }

    /**
     * Falência: libera propriedades e remove do tabuleiro.
     * <p><b>Atenção:</b> a {@link GameFacade} ainda mantém o jogador na ordem/lista.
     * Ela deve ignorar falidos ou reagir a essa remoção.</p>
     */
    public boolean verificarFalencia(Jogador jogador) {
        if (jogador.getConta().getSaldo() < 0 || jogador.isFalido()) {
            jogador.setFalido(true);
            tabuleiro.limparPropriedadesDe(jogador);
            tabuleiro.removerJogador(jogador);
            return true;
        }
        return false;
    }
}
