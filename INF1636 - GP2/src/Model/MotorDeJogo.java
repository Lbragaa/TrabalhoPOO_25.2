package Model;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class MotorDeJogo {

    private Banco banco;
    private Tabuleiro tabuleiro;

    public MotorDeJogo(Banco banco, Tabuleiro tabuleiro) {
        this.banco = banco;
        this.tabuleiro = tabuleiro;
    }

    /** Lança dois dados de 6 faces e retorna os valores. */
    public List<Integer> lancarDados() {
        Random r = new Random();
        List<Integer> dados = new ArrayList<>();
        dados.add(r.nextInt(6) + 1);
        dados.add(r.nextInt(6) + 1);
        return dados;
    }

    /** Verifica dupla (mesmo valor nos 2 dados). */
    private static boolean ehDupla(List<Integer> dados) {
        return dados != null && dados.size() >= 2
                && dados.get(0) != null && dados.get(1) != null
                && dados.get(0).intValue() == dados.get(1).intValue();
    }

    /** Move e resolve prisão (30) + aluguel. */
    public void moverJogador(Jogador jogador, List<Integer> dados) {
        if (jogador == null || dados == null || dados.isEmpty()) return;

        // Se está preso, não se move
        if (jogador.estaPreso()) {
            System.out.println(jogador.getNome() + " está preso e não pode se mover.");
            return;
        }

        int somaDados = 0;
        for (Integer d : dados) if (d != null) somaDados += d;

        // Move (regras de passar pela saída estão em Jogador.move)
        jogador.move(somaDados, banco);

        // Se caiu na casa "Vá para a prisão" (30), prende e encerra
        verificarPrisao(jogador);
        if (jogador.estaPreso()) {
            System.out.println(jogador.getNome() + " foi preso!");
            return;
        }

        // Cobrança de aluguel automática
        Propriedade prop = tabuleiro.getPropriedadeNaPosicao(jogador.getPosicao());
        if (prop != null) pagarAluguel(jogador, prop);
    }

    /** Compra propriedade sem dono. */
    public void comprarPropriedade(Jogador jogador, Propriedade propriedade) {
        if (propriedade.estaDisponivel()) {
            boolean pagou = jogador.getConta().paga(banco.getConta(), propriedade.getPreco());
            if (pagou) {
                propriedade.setProprietario(jogador);
            } else {
                verificarFalencia(jogador);
            }
        }
    }

    /** Constrói casa em terreno do próprio jogador (estando na casa). */
    public void construirCasa(Jogador jogador, Propriedade propriedade) {
        Propriedade atual = tabuleiro.getPropriedadeNaPosicao(jogador.getPosicao());
        if (propriedade != atual) return; // só constrói se estiver na propriedade
        if (propriedade instanceof Terreno terreno) {
            if (terreno.getProprietario() == jogador && terreno.podeConstruir()) {
                boolean pagou = jogador.getConta().paga(banco.getConta(), terreno.getValorCasa());
                if (pagou) terreno.adicionaCasa();
                else verificarFalencia(jogador);
            }
        }
    }

    /** Cobrança de aluguel: terreno só cobra se tem ≥1 casa; demais cobram base. */
    public void pagarAluguel(Jogador jogador, Propriedade propriedade) {
        if (jogador == null || propriedade == null) return;

        Jogador dono = propriedade.proprietario;
        if (dono == null || dono == jogador) return;

        int valorAPagar = 0;
        if (propriedade instanceof Terreno terreno) {
            if (terreno.getNumCasas() >= 1) valorAPagar = terreno.calculaAluguel();
            else return; // terreno sem casa não cobra
        } else {
            valorAPagar = propriedade.calculaAluguel();
        }

        boolean pagou = jogador.getConta().paga(dono.getConta(), valorAPagar);
        if (!pagou) {
            // força negativo para evidenciar débito e aciona falência
            int saldoAtual = jogador.getConta().getSaldo();
            jogador.getConta().setSaldo(saldoAtual - valorAPagar);
            jogador.setFalido(true);
            verificarFalencia(jogador);
            System.out.println(jogador.getNome() + " não conseguiu pagar o aluguel (" + valorAPagar + ") e faliu!");
        }
    }

    /** Checa casa 30 (vá para a prisão). */
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

    /** Puxa carta e aplica; não retorna objeto. */
    public void puxarSorteReves(Jogador j) {
        Carta c = tabuleiro.comprarCartaSorteReves();
        switch (c.tipo) {
            case VAI_PARA_PRISAO -> j.prende();
            case SAIDA_LIVRE     -> j.adicionarCartaLiberacao(); // guarda; não volta já
            case PAGAR           -> { j.getConta().paga(banco.getConta(), c.valor); verificarFalencia(j); }
            case RECEBER         -> banco.getConta().paga(j.getConta(), c.valor);
        }
    }

    /** Usa carta de saída livre, devolvendo-a ao fim do deck. */
    public boolean usarCartaLiberacao(Jogador j) {
        if (j == null || !j.estaPreso()) return false;
        if (!j.consumirCartaLiberacao()) return false;
        tabuleiro.devolverCartaLiberacao(); // volta uma SAÍDA_LIVRE pro fim da fila
        j.solta();
        return true;
    }

    /** Falência: libera propriedades e remove do jogo. */
    public boolean verificarFalencia(Jogador jogador) {
        if (jogador.getConta().getSaldo() < 0 || jogador.isFalido()) {
            jogador.setFalido(true);
            tabuleiro.limparPropriedadesDe(jogador);
            tabuleiro.removerJogador(jogador);
            System.out.println(jogador.getNome() + " faliu e saiu do jogo.");
            return true;
        }
        return false;
    }
}
