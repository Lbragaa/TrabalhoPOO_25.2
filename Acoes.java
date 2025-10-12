package model;

import java.util.Random;

public class Acoes {

    private Banco banco;
    private Tabuleiro tabuleiro;

    public Acoes(Banco banco, Tabuleiro tabuleiro) {
        this.banco = banco;
        this.tabuleiro = tabuleiro;
    }

    // 1️⃣ Roll dice
    public int[] lancarDados() {
        Random r = new Random();
        return new int[] { r.nextInt(6) + 1, r.nextInt(6) + 1 };
    }

    // 2️⃣ Move player
    public void moverJogador(Jogador jogador, int somaDados) {
        jogador.move(somaDados);
    }

    // 3️⃣ Buy property (unowned)
    public void comprarPropriedade(Jogador jogador, Terreno terreno) {
        if (terreno.getProprietario() == null) {
            boolean pagou = jogador.getConta().paga(banco.getConta(), terreno.getPreco());
            if (pagou) {
                terreno.setProprietario(jogador);
            } else {
                jogador.setFalido(true);
            }
        }
    }

    // 4️⃣ Build a house (on owned property)
    public void construirCasa(Jogador jogador, Terreno terreno) {
        if (terreno.getProprietario() == jogador && terreno.podeConstruir()) {
            boolean pagou = jogador.getConta().paga(banco.getConta(), terreno.getValorCasa());
            if (pagou) terreno.adicionaCasa();
        }
    }

    // 5️⃣ Pay rent automatically
    public void pagarAluguel(Jogador jogador, Terreno terreno) {
        Jogador dono = terreno.getProprietario();
        if (dono != null && dono != jogador && terreno.getNumCasas() >= 1) {
            boolean pagou = jogador.getConta().paga(dono.getConta(), terreno.calculaAluguel());
            if (!pagou) jogador.setFalido(true);
        }
    }

    // 6️⃣ Jail logic (enter/exit)
    public void verificarPrisao(Jogador jogador, boolean dadosIguais) {
        if (jogador.estaPreso() && dadosIguais) {
            jogador.solta();
        } else if (tabuleiro.isCasaPrisao(jogador.getPosicao())) {
            jogador.prende();
        }
    }

    // 7️⃣ Check bankruptcy
    public boolean verificarFalencia(Jogador jogador) {
        if (jogador.getConta().getSaldo() < 0) {
            jogador.setFalido(true);
            return true;
        }
        return false;
    }
}
