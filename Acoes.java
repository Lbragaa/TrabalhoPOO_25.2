package model;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

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
        jogador.move(somaDados, banco); // passa o banco atual para possivelmente pagar o jogador

        // Resolve cobrança de aluguel automaticamente (Regra 5)
        // Aqui vai ocorrer a checagem se o jogador caiu em alguma propriedade
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
    // Em Acoes.java (mesmo package 'model')
    public void pagarAluguel(Jogador jogador, Propriedade propriedade) {
        if (jogador == null || propriedade == null) return;
    
        // Se não há dono ou o dono é o próprio jogador, não paga
        Jogador dono = propriedade.proprietario; // campo protegido; OK no mesmo package
        if (dono == null || dono == jogador) return;
    
        int valorAPagar;
    
        if (propriedade instanceof Terreno) {
            Terreno t = (Terreno) propriedade;
    
            // Regra atualizada: "pelo menos 1 casa"
            if (t.getNumCasas() >= 1) {
                valorAPagar = t.calculaAluguel(); // terreno com casas: usa cálculo do terreno
            } else {
                // Else explícito: não paga nada e retorna
                return;
            }
        } else {
            // Propriedade genérica (sem casas): paga aluguel base
            valorAPagar = propriedade.aluguelBase; // protegido; OK no mesmo package
        }
    
        boolean pagou = jogador.getConta().paga(dono.getConta(), valorAPagar);
        if (!pagou) {
            verificarFalencia(jogador);
        }
    }


    // 6️⃣ Verificar prisão (entrada/saída)
    public void verificarPrisao(Jogador jogador) {
        if (jogador == null) return;
        else if (tabuleiro.isCasaPrisao(jogador.getPosicao())) {
            jogador.prende();
        }
    }
    // Liberar se ele tirar dupla
    // 2) Soltar se tirou dupla (não move aqui; só libera)
    public boolean soltarSeDupla(Jogador jogador,List<Integer> dados) {
        if (jogador == null || !jogador.estaPreso()) return false;
        if (ehDupla(dados)) {
            jogador.solta();
            return true;
        }
        return false;
    }
    
    // pegandoo as cartas aq
    public void puxarSorteReves(Jogador j) {
        Carta c = tabuleiro.comprarCartaSorteReves();
        switch (c.tipo) {
            case VAI_PARA_PRISAO -> entrarNaPrisaoPorCarta(j);
            case SAIDA_LIVRE     -> j.adicionarCartaLiberacao(); // jogador guarda; deck já NÃO reintroduziu SOMA +1 nas cartas de liberação
            case PAGAR           -> { j.getConta().paga(banco.getConta(), c.valor); verificarFalencia(j); } //paga o valor da carta se for desse tipo
            case RECEBER         -> banco.getConta().paga(j.getConta(), c.valor); // tem que ver se ta certo isso aqui sla 
        }
    }

    public boolean usarCartaLiberacao(Jogador j) {
        if (j == null || !j.estaPreso()) return false;
        if (!j.consumirCartaLiberacao()) return false;
        tabuleiro.devolverCartaLiberacao(); // volta uma SAÍDA_LIVRE pro fim da fila
        j.solta();
        return true;
    }

    public 

    // 7️⃣ Verificar falência e remover jogador do jogo
    boolean verificarFalencia(Jogador jogador) {
        
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







