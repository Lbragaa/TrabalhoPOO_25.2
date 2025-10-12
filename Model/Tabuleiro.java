package Model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class Tabuleiro {

    // Número padrão de casas no Banco Imobiliário
    private static final int NUM_CASAS = 40;
    // Posição da prisão (geralmente casa 10)
    private static final int POSICAO_VAI_PRA_PRISAO = 26;
    private static final int POSICAO_PRISAO = 10;

    // Lista de propriedades existentes no tabuleiro
    protected List<Propriedade> propriedades;

    // Lista de jogadores ativos na partida
    protected List<Jogador> jogadoresAtivos;

    // Lista de baralho usando FILA, sempre compra no início e coloca no fim do baralhp
    protected final Queue<Carta> baralhoSorteReves;
    
    // ---------- CONSTRUTOR ----------
    public Tabuleiro() {
        this.propriedades = new ArrayList<>();
        this.jogadoresAtivos = new ArrayList<>();
        this.baralhoSorteReves = new LinkedList<>();
    }

    // ---------- MÉTODOS ESTÁTICOS ----------
    public static int getNumCasas() {
        return NUM_CASAS;
    }

    public static int getPosicaoPrisao() { //posicao quando o jogador tem que ir para prisao
        return POSICAO_VAI_PRA_PRISAO;
    }
    
    public static int getPosicaoVisitaPrisao() {
    	return POSICAO_PRISAO;      //posicao de destino do jogador quando ele é preso
    }
    public boolean isCasaPrisao(int posicao) { // verifica se é a casa que ele é mandado pra prisao
        return posicao == POSICAO_VAI_PRA_PRISAO;
    }
    //---------------------MÉTODOS AUXILIARES PRA TESTE--------------------------
    public void inicializarBaralhoTeste() {
        if (baralhoSorteReves == null) return;

        baralhoSorteReves.clear();

        // Cartas simples apenas para testar comportamentos básicos
        baralhoSorteReves.offer(new Carta(TipoCarta.VAI_PARA_PRISAO, 0));
        baralhoSorteReves.offer(new Carta(TipoCarta.SAIDA_LIVRE, 0));
        baralhoSorteReves.offer(new Carta(TipoCarta.PAGAR, 100));
        baralhoSorteReves.offer(new Carta(TipoCarta.RECEBER, 200));
    }
    private void inicializarPropriedades() {
        // limpa e (re)carrega o tabuleiro, evitando as casas 10 (prisão) e 26 (vai pra prisão)
        propriedades.clear();

        // nome, preço, valorCasa, aluguelBase, posição  (Terreno)
        // nome, preço, aluguelBase, posição             (Propriedade)
        propriedades.add(new Terreno("Terreno — Vila Azul",         120,  50, 20,  1));
        propriedades.add(new Propriedade("Praça Municipal",         100,      18,  3));
        propriedades.add(new Terreno("Terreno — Jardim Verde",      140,  50, 22,  6));
        propriedades.add(new Propriedade("Estação Rodoviária",      200,      25,  8));

        propriedades.add(new Terreno("Terreno — Centro",            180, 100, 30, 11));
        propriedades.add(new Propriedade("Mercado Popular",         160,      26, 13));
        propriedades.add(new Terreno("Terreno — Orla",              200, 100, 34, 14));
        propriedades.add(new Propriedade("Aeroporto Regional",      260,      40, 16));

        propriedades.add(new Terreno("Terreno — Parque Norte",      220, 150, 40, 19));
        propriedades.add(new Propriedade("Porto",                   240,      38, 21));
        propriedades.add(new Terreno("Terreno — Bairro Alto",       260, 150, 46, 24));
        propriedades.add(new Propriedade("Usina Hidrelétrica",      280,      45, 25));

        propriedades.add(new Terreno("Terreno — Zona Sul",          300, 200, 50, 27));
        propriedades.add(new Propriedade("Shopping Center",         300,      55, 29));
        propriedades.add(new Terreno("Terreno — Lagoa",             320, 200, 58, 31));
        propriedades.add(new Propriedade("Terminal de Cargas",      320,      60, 34));

        propriedades.add(new Terreno("Terreno — Avenida Central",   360, 250, 65, 37));
        propriedades.add(new Propriedade("Estação Central",         360,      70, 39));
    }

    // ---------- INICIALIZAÇÃO DE JOGADORES ----------
    public void inicializarJogadores(List<String> nomes) {
        if (nomes == null || nomes.isEmpty()) {
            throw new IllegalArgumentException("Lista de nomes de jogadores não pode ser vazia.");
        }
    
        // Zera jogadores anteriores e recria do zero (estado limpo)
        this.jogadoresAtivos.clear();
    
        for (String nome : nomes) {
            if (nome == null || nome.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome de jogador inválido.");
            }
            Jogador j = new Jogador(nome.trim());
            // Se no futuro você quiser fixar alguma regra extra de start, faça aqui
            // ex: j.getConta().setSaldo(4000); (já é feito no construtor)
            //     j.setPosicao(0); (já é feito no construtor)
            this.jogadoresAtivos.add(j);
        }
    }

    /**
     * Atalho: cria N jogadores com nomes "Jogador 1", "Jogador 2", ...
     */
    public void inicializarJogadores(int quantidade) {
        if (quantidade < 2) {
            throw new IllegalArgumentException("É necessário pelo menos 2 jogadores.");
        }
        java.util.List<String> nomes = new java.util.ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            nomes.add("Jogador " + i);
        }
        inicializarJogadores(nomes);
    }

    // ---------- PROPRIEDADES ----------
    public void addPropriedade(Propriedade p) {
        propriedades.add(p);
    }

    public List<Propriedade> getPropriedades() {
        return propriedades;
    }

    public void limparPropriedadesDe(Jogador jogador) {
        for (Propriedade p : propriedades) {
            if (p.getProprietario() == jogador) {
                p.setProprietario(null);
            }
        }
    }

    // ---------- JOGADORES ----------
    public void addJogador(Jogador jogador) {
        jogadoresAtivos.add(jogador);
    }

    public void removerJogador(Jogador jogador) {
        jogadoresAtivos.remove(jogador);
    }

    public List<Jogador> getJogadoresAtivos() {
        return jogadoresAtivos;
    }
    
    public Propriedade getPropriedadeNaPosicao(int posicao) {
        for (Propriedade p : propriedades) {
            if (p.getPosicao() == posicao) {
                return p;
            }
        }
        return null;
    }

    public boolean estaNoJogo(Jogador jogador) {
        return jogadoresAtivos.contains(jogador);
    }
    //--------------- CARTAS -------------
    public Carta comprarCartaSorteReves() {
        Carta c = baralhoSorteReves.poll();
        if (c == null) throw new IllegalStateException("Baralho de Sorte/Revés vazio.");

        if (c.tipo == TipoCarta.SAIDA_LIVRE) {
            // jogador guarda a carta (contador +1) e ELA NÃO VOLTA ao deck agora
            // não faz offer(c)
        } else {
            // cartas normais continuam no ciclo
            baralhoSorteReves.offer(c);
        }
        return c;
    }
    //Quando o jogador usa a carta ele tem que devolver pro final da fila, isso acontece la na ação quando ele ta preso e usa a carta pra sair
    void devolverCartaLiberacao() {
    baralhoSorteReves.offer(new Carta(TipoCarta.SAIDA_LIVRE, 0));
    }
}













