package model;

import java.util.Random;

public class Acoes {

    private Banco banco;
    private Tabuleiro tabuleiro;

    public Acoes(Banco banco, Tabuleiro tabuleiro) {
        this.banco = banco;
        this.tabuleiro = tabuleiro;
    }


// O método agora promete retornar qualquer tipo de Lista
    public List<Integer> lancarDados() {
        Random r = new Random();
        List<Integer> dados = new ArrayList<>();
        dados.add(r.nextInt(6) + 1);
        dados.add(r.nextInt(6) + 1);
        return dados;
    }
    /** Checa se saiu dupla para liberar da cadeia. */
    private static boolean ehDupla(List<Integer> dados) {
        return dados != null && dados.size() >= 2
                && dados.get(0) != null && dados.get(1) != null
                && dados.get(0).intValue() == dados.get(1).intValue();
    }
    // 2️⃣ Mover jogador
    public void moverJogador(Jogador jogador, List<Integer> dados) {
        if (jogador == null || dados == null || dados.isEmpty()) return;

        int somaDados = 0;
        for (Integer d : dados) {
            if (d != null) somaDados += d;
        }

        // Move (sua lógica de wrap/“passou pelo início” permanece no Jogador)
        jogador.move(somaDados);

        // Resolve cobrança de aluguel automaticamente (Regra 5)
        Propriedade prop = tabuleiro.getPropriedadeNaPosicao(jogador.getPosicao());
        if (prop != null) {
            pagarAluguel(jogador, prop);
        }

        // DEPOIS incluir mais efeitos automaticos aqui 
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
    public void verificarPrisao(Jogador jogador, List<Integer> dados) {
        boolean dadoIguais = ehDupla(dados);
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




