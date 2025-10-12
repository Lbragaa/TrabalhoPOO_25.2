package model;

import java.util.Random;

public class Acoes {

    private Banco banco;
    private Tabuleiro tabuleiro;

    public Acoes(Banco banco, Tabuleiro tabuleiro) {
        this.banco = banco;
        this.tabuleiro = tabuleiro;
    }

    // 1️⃣ Lançar os dados
    public int[] lancarDados() {//TEM QUE SER ARRAYLIST AQUI NAO PODE SER ARRAY COMUM
        Random r = new Random();
        return new int[] { r.nextInt(6) + 1, r.nextInt(6) + 1 };
    }

    // 2️⃣ Mover jogador
    public void moverJogador(Jogador jogador, int somaDados) {//PASSSAR O NOVO ARRAY GERADO EM lancarDados COMO PARAMETRO E FAZER A SOMA DENTRO DESSE METODO
        jogador.move(somaDados);
    }

    // 3️⃣ Comprar propriedade (sem dono)
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

    // 4️⃣ Construir casa (apenas terrenos)
    public void construirCasa(Jogador jogador, Propriedade propriedade) {
        if (propriedade instanceof Terreno terreno) {
            if (terreno.getProprietario() == jogador && terreno.podeConstruir()) {
                boolean pagou = jogador.getConta().paga(banco.getConta(), terreno.getValorCasa());
                if (pagou) {
                    terreno.adicionaCasa();
                } else {
                    verificarFalencia(jogador);
                }
            }
        }
    }

    // 5️⃣ Pagar aluguel automaticamente
    public void pagarAluguel(Jogador jogador, Propriedade propriedade) {
        Jogador dono = propriedade.getProprietario();
        if (dono != null && dono != jogador) {
            int aluguel = propriedade.calculaAluguel();
            boolean pagou = jogador.getConta().paga(dono.getConta(), aluguel);
            if (!pagou) {
                verificarFalencia(jogador);
            }
        }
    }

    // 6️⃣ Verificar prisão (entrada/saída)
    public void verificarPrisao(Jogador jogador, boolean dadosIguais) {
        if (jogador.estaPreso() && dadosIguais) {
            jogador.solta();
        } else if (tabuleiro.isCasaPrisao(jogador.getPosicao())) {
            jogador.prende();
        }
    }

    // 7️⃣ Verificar falência e remover jogador do jogo
    public boolean verificarFalencia(Jogador jogador) {
        
        if (jogador.getConta().getSaldo() < 0 || jogador.isFalido()) {
            jogador.setFalido(true);

            // Libera todas as propriedades do jogador
            tabuleiro.limparPropriedadesDe(jogador);

            // Remove o jogador do tabuleiro
            tabuleiro.removerJogador(jogador);

            System.out.println(jogador.getNome() + " faliu e saiu do jogo.");
            return true;
        }
        return false;
    }
}


