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
// 2️⃣ Mover jogador (agora bloqueia se estiver preso)
    public void moverJogador(Jogador jogador, List<Integer> dados) {
        if (jogador == null || dados == null || dados.isEmpty()) return;
    
        // Se o jogador está preso, ele não se move
        if (jogador.estaPreso()) {
            System.out.println(jogador.getNome() + " está preso e não pode se mover.");
            return;
        }
    
        int somaDados = 0;
        for (Integer d : dados) {
            if (d != null) somaDados += d;
        }
    
        // Move o jogador (regra de passar pela saída implementada em Jogador.move)
        jogador.move(somaDados, banco);
    
        // Verifica se caiu em casa de prisão (deve ocorrer antes de cobrar aluguel)
        verificarPrisao(jogador);
        if (jogador.estaPreso()) {
            System.out.println(jogador.getNome() + " foi preso!");
            return; // Não executa ações adicionais
        }
    
        // Resolve cobrança de aluguel automaticamente
        Propriedade prop = tabuleiro.getPropriedadeNaPosicao(jogador.getPosicao());
        if (prop != null) {
            pagarAluguel(jogador, prop);
        }
    
        // Espaço para futuras ações automáticas (cartas, taxas etc.)
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
    	Propriedade atual = tabuleiro.getPropriedadeNaPosicao(jogador.getPosicao());
    	if (propriedade != atual) return; // só constrói se estiver na propriedade
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

// 5️⃣ Pagar aluguel automaticamente (com falência integrada)
    public void pagarAluguel(Jogador jogador, Propriedade propriedade) {
        if (jogador == null || propriedade == null) return;

        Jogador dono = propriedade.proprietario;
        if (dono == null || dono == jogador) return;

        int valorAPagar = 0;

        if (propriedade instanceof Terreno terreno) {
            if (terreno.getNumCasas() >= 1) {
                valorAPagar = terreno.calculaAluguel();
            } else {
                return; // terreno sem casa não cobra
            }
        } else {
            valorAPagar = propriedade.calculaAluguel();
        }

        boolean pagou = jogador.getConta().paga(dono.getConta(), valorAPagar);
        if (!pagou) {
            // Força saldo negativo para refletir débito
            int saldoAtual = jogador.getConta().getSaldo();
            jogador.getConta().setSaldo(saldoAtual - valorAPagar);

            jogador.setFalido(true);
            verificarFalencia(jogador);
            System.out.println(jogador.getNome() + " não conseguiu pagar o aluguel (" + valorAPagar + ") e faliu!");
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
            case VAI_PARA_PRISAO -> j.prende();
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







