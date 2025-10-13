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


    /**
     * Lança dois dados de 6 faces e retorna os valores.
     * @return Lista com dois inteiros representando os dados.
     */
    public List<Integer> lancarDados() {
        Random r = new Random();
        List<Integer> dados = new ArrayList<>();
        dados.add(r.nextInt(6) + 1);
        dados.add(r.nextInt(6) + 1);
        return dados;
    }
    /**
     * Verifica se os dados representam uma dupla (mesmo valor em ambos os dados).
     * @param dados Lista com dois inteiros representando os dados.
     * @return true se for dupla, false caso contrário.
     */
    private static boolean ehDupla(List<Integer> dados) {
        return dados != null && dados.size() >= 2
                && dados.get(0) != null && dados.get(1) != null
                && dados.get(0).intValue() == dados.get(1).intValue();
    }
    /**
     * Move o jogador no tabuleiro de acordo com a soma dos dados.
     * Bloqueia movimento se o jogador estiver preso.
     * @param jogador Jogador a ser movido.
     * @param dados Lista com os valores dos dados.
     */
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

    

    /**
     * Permite que um jogador compre uma propriedade sem dono.
     * @param jogador Jogador que deseja comprar.
     * @param propriedade Propriedade a ser comprada.
     */
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

    /**
     * Permite que um jogador construa casa em um terreno.
     * Só constrói se o jogador estiver na posição correta e for dono.
     * @param jogador Jogador que deseja construir.
     * @param propriedade Propriedade onde será construída a casa.
     */
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

     /**
     * Cobra aluguel automaticamente de um jogador que caiu em uma propriedade.
     * Se não puder pagar, o jogador faliu.
     * @param jogador Jogador que deve pagar.
     * @param propriedade Propriedade onde o jogador caiu.
     */
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



    /**
     * Verifica se o jogador caiu em uma casa de prisão e prende-o.
     * @param jogador Jogador a ser verificado.
     */
    public void verificarPrisao(Jogador jogador) {
        if (jogador == null) return;
        else if (tabuleiro.isCasaPrisao(jogador.getPosicao())) {
            jogador.prende();
        }
    }
    /**
     * Libera o jogador da prisão se tirou uma dupla nos dados.
     * @param jogador Jogador preso.
     * @param dados Dados lançados.
     * @return true se o jogador foi liberado, false caso contrário.
     */
    public boolean soltarSeDupla(Jogador jogador,List<Integer> dados) {
        if (jogador == null || !jogador.estaPreso()) return false;
        if (ehDupla(dados)) {
            jogador.solta();
            return true;
        }
        return false;
    }
    
    /**
     * Puxa uma carta de sorte/revés do tabuleiro e aplica seu efeito no jogador.
     * @param j Jogador que puxou a carta.
     */
    public void puxarSorteReves(Jogador j) {
        Carta c = tabuleiro.comprarCartaSorteReves();
        switch (c.tipo) {
            case VAI_PARA_PRISAO -> j.prende();
            case SAIDA_LIVRE     -> j.adicionarCartaLiberacao(); // jogador guarda; deck já NÃO reintroduziu SOMA +1 nas cartas de liberação
            case PAGAR           -> { j.getConta().paga(banco.getConta(), c.valor); verificarFalencia(j); } //paga o valor da carta se for desse tipo
            case RECEBER         -> banco.getConta().paga(j.getConta(), c.valor); // tem que ver se ta certo isso aqui sla 
        }
    }

    /**
     * Usa uma carta de liberação da prisão, se disponível, e libera o jogador.
     * @param j Jogador preso.
     * @return true se a carta foi usada com sucesso, false caso contrário.
     */
    public boolean usarCartaLiberacao(Jogador j) {
        if (j == null || !j.estaPreso()) return false;
        if (!j.consumirCartaLiberacao()) return false;
        tabuleiro.devolverCartaLiberacao(); // volta uma SAÍDA_LIVRE pro fim da fila
        j.solta();
        return true;
    }

    /**
     * Verifica se o jogador faliu (saldo negativo ou flag de falência) e remove do jogo.
     * @param jogador Jogador a ser verificado.
     * @return true se o jogador faliu, false caso contrário.
     */
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







