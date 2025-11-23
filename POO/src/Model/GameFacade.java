package Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Façade + Singleton: ponto único de contato da UI com o Model. */
public final class GameFacade implements GameSubject {

    // ---------- Singleton ----------
    private static GameFacade INSTANCIA;

    public static GameFacade init(String[] nomes, int[] ordemSorteada) {
        if (INSTANCIA == null) INSTANCIA = new GameFacade(nomes, ordemSorteada);
        return INSTANCIA;
    }
    public static GameFacade get() {
        if (INSTANCIA == null)
            throw new IllegalStateException("GameFacade.init(...) ainda não foi chamado.");
        return INSTANCIA;
    }
    public static void resetForTests() { INSTANCIA = null; }

    // ---------- Estado interno ----------
    private final Banco banco;
    private final Tabuleiro tabuleiro;
    private final MotorDeJogo motor;
    private final List<Jogador> jogadores = new ArrayList<>();
    private final List<GameObserver> observadores = new ArrayList<>();

    private final int[] ordem;
    private int ponteiroDaVez = 0;

    // Estado anterior (para diffs)
    private int[] saldoAnterior;
    private boolean[] presoAnterior;
    private boolean[] falidoAnterior;

    // Construtor privado
    private GameFacade(String[] nomes, int[] ordemSorteada) {
        this.banco = new Banco();
        this.tabuleiro = new Tabuleiro();
        cadastrarPropriedadesPadrao();

        for (String nome : nomes) {
            Jogador j = new Jogador(nome);
            jogadores.add(j);
            tabuleiro.addJogador(j);
        }

        if (ordemSorteada != null && ordemSorteada.length == jogadores.size()) {
            this.ordem = ordemSorteada.clone();
        } else {
            this.ordem = new int[jogadores.size()];
            for (int i = 0; i < this.ordem.length; i++) this.ordem[i] = i;
        }

        this.motor = new MotorDeJogo(banco, tabuleiro);

        int n = jogadores.size();
        saldoAnterior  = new int[n];
        presoAnterior  = new boolean[n];
        falidoAnterior = new boolean[n];
        for (int i = 0; i < n; i++) {
            saldoAnterior[i]  = jogadores.get(i).getConta().getSaldo();
            presoAnterior[i]  = jogadores.get(i).estaPreso();
            falidoAnterior[i] = jogadores.get(i).isFalido();
        }
    }

    // ---------- Observer (Subject) ----------
    @Override
    public void addObserver(GameObserver o) {
        if (o != null && !observadores.contains(o)) observadores.add(o);
    }

    @Override
    public void removeObserver(GameObserver o) {
        observadores.remove(o);
    }

    // ---------- Consultas ----------
    public int  getIndiceJogadorDaVez()                { return ordem[ponteiroDaVez]; }
    public int  getPosicao(int indiceJogador)          { return jogadores.get(indiceJogador).getPosicao(); }
    public int  getSaldo(int indiceJogador)            { return jogadores.get(indiceJogador).getConta().getSaldo(); }
    public boolean jogadorEstaPreso(int indiceJogador) { return jogadores.get(indiceJogador).estaPreso(); }
    public String getNomeJogador(int indiceJogador)    { return jogadores.get(indiceJogador).getNome(); }

    public List<String> getPropriedadesDoJogador(int indiceJogador) {
        List<String> out = new ArrayList<>();
        if (indiceJogador < 0 || indiceJogador >= jogadores.size()) return out;
        Jogador dono = jogadores.get(indiceJogador);
        for (int pos = 0; pos < 40; pos++) {
            Propriedade p = tabuleiro.getPropriedadeNaPosicao(pos);
            if (p != null && p.getProprietario() == dono) out.add(p.getNome());
        }
        return out;
    }

    public List<Integer> getCelulasPropriedadesDoJogador(int indiceJogador) {
        List<Integer> out = new ArrayList<>();
        if (indiceJogador < 0 || indiceJogador >= jogadores.size()) return out;
        Jogador dono = jogadores.get(indiceJogador);
        for (int pos = 0; pos < 40; pos++) {
            Propriedade p = tabuleiro.getPropriedadeNaPosicao(pos);
            if (p != null && p.getProprietario() == dono) out.add(pos);
        }
        return out;
    }

    public Integer getIndiceDonoDaPosicao(int celula) {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(celula));
        if (p == null) return null;
        Jogador dono = p.getProprietario();
        if (dono == null) return null;
        for (int i = 0; i < jogadores.size(); i++) if (jogadores.get(i) == dono) return i;
        return null;
    }

    public boolean posicaoTemPropriedade(int celula)            { return tabuleiro.getPropriedadeNaPosicao(norm40(celula)) != null; }
    public boolean propriedadeDisponivel(int celula)            {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(celula));
        return p != null && p.estaDisponivel();
    }
    public String getNomePropriedade(int celula)                {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(celula));
        return (p != null ? p.getNome() : null);
    }
    public int getPrecoPropriedade(int celula)                  {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(celula));
        return (p != null ? p.getPreco() : 0);
    }
    public boolean jogadorEhDonoDaPosicao(int indiceJogador, int celula) {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(celula));
        if (p == null) return false;
        return p.getProprietario() == jogadores.get(indiceJogador);
    }

    public boolean podeConstruirAqui(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(j.getPosicao());
        if (p instanceof Terreno t) return t.getProprietario() == j && t.podeConstruir();
        return false;
    }

    public int getValorCasaAqui(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(j.getPosicao());
        if (p instanceof Terreno t) return t.getValorCasa();
        return 0;
    }

    /** NOVO: valor do hotel na posição atual do jogador. */
    public int getValorHotelAqui(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(j.getPosicao());
        if (p instanceof Terreno t) return t.getValorHotel();
        return 0;
    }

    /** NOVO: expomos a quantidade de casas em uma célula (0 se não for Terreno). */
    public int getNumeroCasasNaPosicao(int celula) {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(celula));
        if (p instanceof Terreno t) return t.getNumCasas();
        return 0;
    }

    /** NOVO: 0 ou 1 hotel na posição (apenas Terreno). */
    public int getNumeroHoteisNaPosicao(int celula) {
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(norm40(celula));
        if (p instanceof Terreno t) return t.temHotel() ? 1 : 0;
        return 0;
    }

    /** NOVO: indica se a próxima construção nesta posição será hotel (4 casas e ainda sem hotel). */
    public boolean proximaConstrucaoEhHotelAqui(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(j.getPosicao());
        if (p instanceof Terreno t) {
            return t.getProprietario() == j && t.getNumCasas() == 4 && !t.temHotel();
        }
        return false;
    }

    // ---------- Ações ----------
    public void rolarDadosAleatorio() {
        List<Integer> dados = motor.lancarDados();
        processarRolagem(dados.get(0), dados.get(1));
    }
    public void rolarDadosForcado(int d1, int d2) { processarRolagem(d1, d2); }

    public void comprarPropriedade(int indiceJogador, int posicao) {
        int celula = norm40(posicao);
        Jogador j = jogadores.get(indiceJogador);
        Propriedade antes = tabuleiro.getPropriedadeNaPosicao(celula);
        Jogador donoAntes = (antes != null ? antes.getProprietario() : null);

        motor.comprarPropriedade(j, antes);

        Propriedade depois = tabuleiro.getPropriedadeNaPosicao(celula);
        Jogador donoDepois = (depois != null ? depois.getProprietario() : null);
        if (depois != null && donoDepois == j && donoDepois != donoAntes) {
            notificarPropriedadeComprada(indiceJogador, celula);
        }
        detectarENotificarEstadoGlobal();
    }
    public void comprarPropriedadeAtual(int indiceJogador) { comprarPropriedade(indiceJogador, jogadores.get(indiceJogador).getPosicao()); }

    public void construirCasaNoLocal(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        int celula = j.getPosicao();
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(celula);

        int casasAntes = -1;
        boolean tinhaHotelAntes = false;
        if (p instanceof Terreno t) {
            casasAntes = t.getNumCasas();
            tinhaHotelAntes = t.temHotel();
        }

        motor.construirCasa(j, p);

        int casasDepois = -1;
        boolean temHotelDepois = false;
        if (p instanceof Terreno t) {
            casasDepois = t.getNumCasas();
            temHotelDepois = t.temHotel();
        }

        if (p instanceof Terreno && (casasDepois > casasAntes || (!tinhaHotelAntes && temHotelDepois))) {
            notificarCasaConstruida(indiceJogador, celula, casasDepois);
        }
        detectarENotificarEstadoGlobal();
    }

    public void puxarSorteReves(int indiceJogador) {
        // Mantido para compatibilidade (ex.: testes ou botões manuais)
        motor.puxarSorteReves(jogadores.get(indiceJogador));
        detectarENotificarEstadoGlobal();
    }
    public boolean usarCartaLiberacao(int indiceJogador) {
        boolean ok = motor.usarCartaLiberacao(jogadores.get(indiceJogador));
        detectarENotificarEstadoGlobal();
        return ok;
    }
    public boolean verificarFalencia(int indiceJogador) {
        boolean faliu = motor.verificarFalencia(jogadores.get(indiceJogador));
        detectarENotificarEstadoGlobal();
        return faliu;
    }

    // ---------- Fluxo interno ----------

    // ---------- Consultas auxiliares para HUD ----------
    public int getNumeroJogadores() {
        return jogadores.size();
    }

    private void processarRolagem(int d1, int d2) {
        for (GameObserver o : observadores) o.onDice(d1, d2);

        int indiceJogadorDaVez = getIndiceJogadorDaVez();
        Jogador jogadorDaVez = jogadores.get(indiceJogadorDaVez);
        int celulaOrigem = jogadorDaVez.getPosicao();

        if (jogadorDaVez.estaPreso()) {
            boolean liberado = motor.soltarSeDupla(jogadorDaVez, Arrays.asList(d1, d2));
            detectarENotificarEstadoGlobal();
            if (!liberado) {
                avancarVezENotificar();
                return;
            }
        }

        motor.moverJogador(jogadorDaVez, Arrays.asList(d1, d2));
        int celulaDestino = jogadorDaVez.getPosicao();

        for (GameObserver o : observadores) o.onMoved(indiceJogadorDaVez, celulaOrigem, celulaDestino);

        // Casas especiais fixas (lucros/dividendos e IR)
        if (tabuleiro.isCasaLucrosDividendos(celulaDestino)) {
            banco.getConta().paga(jogadorDaVez.getConta(), 200);
            for (GameObserver o : observadores) {
                o.onSpecialCell(indiceJogadorDaVez, celulaDestino, 200, "Lucros ou dividendos: +200");
            }
        } else if (tabuleiro.isCasaImpostoRenda(celulaDestino)) {
            jogadorDaVez.getConta().paga(banco.getConta(), 200);
            for (GameObserver o : observadores) {
                o.onSpecialCell(indiceJogadorDaVez, celulaDestino, -200, "Imposto de renda: -200");
            }
        }

        // Aluguel se for propriedade
        Propriedade propriedadeAtual = tabuleiro.getPropriedadeNaPosicao(celulaDestino);
        if (propriedadeAtual != null) {
            Jogador donoDaPropriedade = propriedadeAtual.getProprietario();
            if (donoDaPropriedade != null && donoDaPropriedade != jogadorDaVez) {
                int saldoAntesPagante = jogadorDaVez.getConta().getSaldo();
                motor.pagarAluguel(jogadorDaVez, propriedadeAtual);
                int saldoDepoisPagante = jogadorDaVez.getConta().getSaldo();
                int valorPago = Math.max(0, saldoAntesPagante - saldoDepoisPagante);
                if (valorPago > 0) {
                    int indiceDono = indexOf(donoDaPropriedade);
                    if (indiceDono >= 0) {
                        for (GameObserver o : observadores) {
                            o.onRentPaid(indiceJogadorDaVez, indiceDono, celulaDestino, valorPago);
                        }
                    }
                }
            }
        }

        // Sorte/Revés: puxa carta real, aplica e notifica número/imagem
        if (tabuleiro.isChanceCell(celulaDestino)) {
            Carta c = motor.puxarSorteReves(jogadorDaVez);
            for (GameObserver o : observadores) {
                o.onChanceCard(indiceJogadorDaVez, celulaDestino, c.codigo, c.tipo.name(), c.valor);
            }
        }

        // Se foi preso (casa 30 ou carta) e possui carta de liberação, usa automaticamente
        tentarUsarCartaLiberacaoAutomatica(jogadorDaVez, indiceJogadorDaVez);

        detectarENotificarEstadoGlobal();
        avancarVezENotificar();
    }

    private int indexOf(Jogador j) {
        for (int i = 0; i < jogadores.size(); i++) if (jogadores.get(i) == j) return i;
        return -1;
    }

    private void avancarVezENotificar() {
        ponteiroDaVez = (ponteiroDaVez + 1) % ordem.length;
        int atual = getIndiceJogadorDaVez();
        for (GameObserver o : observadores) o.onTurnChanged(atual);
    }

    private static int norm40(int v) { return ((v % 40) + 40) % 40; }

    // ---------- Cadastro das propriedades ----------
    private void cadastrarPropriedadesPadrao() {

        tabuleiro.addPropriedade(new Terreno("Leblon",                     100, 50, 10,  1));
        tabuleiro.addPropriedade(new Terreno("Av. Presidente Vargas",       60, 30,  6,  3));
        tabuleiro.addPropriedade(new Terreno("Av. Nossa Sra. De Copacabana",60, 30,  6,  4));
        tabuleiro.addPropriedade(new Companhia("Companhia Ferroviária",    200, 1, 25,   5));
        tabuleiro.addPropriedade(new Terreno("Av. Brigadeiro Faria Lima",  240,120, 24,  6));
        tabuleiro.addPropriedade(new Companhia("Companhia de Viação",      200, 1, 28,   7));
        tabuleiro.addPropriedade(new Terreno("Av. Rebouças",               220,110, 22,  8));
        tabuleiro.addPropriedade(new Terreno("Av. 9 de Julho",             220,110, 22,  9));
        tabuleiro.addPropriedade(new Terreno("Av. Europa",                 200,100, 20, 11));
        tabuleiro.addPropriedade(new Terreno("Rua Augusta",                180, 90, 18, 13));
        tabuleiro.addPropriedade(new Terreno("Av. Pacaembú",               180, 90, 18, 14));
        tabuleiro.addPropriedade(new Companhia("Companhia de Táxi",        150, 1, 22,  15));
        tabuleiro.addPropriedade(new Terreno("Interlagos",                 350,175, 35, 17));
        tabuleiro.addPropriedade(new Terreno("Morumbi",                    400,200, 40, 19));
        tabuleiro.addPropriedade(new Terreno("Flamengo",                   120, 60, 12, 21));
        tabuleiro.addPropriedade(new Terreno("Botafogo",                   100, 50, 10, 22));
        tabuleiro.addPropriedade(new Companhia("Companhia de Navegação",   150, 1, 25,  24));
        tabuleiro.addPropriedade(new Terreno("Av. Brasil",                 160, 80, 16, 26));
        tabuleiro.addPropriedade(new Terreno("Av. Paulista",               140, 70, 14, 27));
        tabuleiro.addPropriedade(new Terreno("Jardim Europa",              140, 70, 14, 29));
        tabuleiro.addPropriedade(new Terreno("Copacabana",                 260,130, 26, 31));
        tabuleiro.addPropriedade(new Companhia("Companhia de Aviação",     200, 1, 28,  32));
        tabuleiro.addPropriedade(new Terreno("Av. Vieira Souto",           320,160, 32, 33));
        tabuleiro.addPropriedade(new Terreno("Av. Atlântica",              300,150, 30, 34));
        tabuleiro.addPropriedade(new Companhia("Companhia de Táxi Aéreo",  200, 1, 30,  35));
        tabuleiro.addPropriedade(new Terreno("Ipanema",                    300,150, 30, 36));
        tabuleiro.addPropriedade(new Terreno("Jardim Paulista",            280,140, 28, 38));
        tabuleiro.addPropriedade(new Terreno("Brooklin",                   260,130, 26, 39));
    }


    // ==================== Notificação agregada (diff) ====================
    private void detectarENotificarEstadoGlobal() {
        for (int i = 0; i < jogadores.size(); i++) {
            Jogador j = jogadores.get(i);

            int saldoAtual = j.getConta().getSaldo();
            if (saldoAtual != saldoAnterior[i]) {
                for (GameObserver o : observadores) o.onBalanceChanged(i, saldoAtual);
                saldoAnterior[i] = saldoAtual;
            }

            boolean presoAtual = j.estaPreso();
            if (presoAtual != presoAnterior[i]) {
                for (GameObserver o : observadores) o.onJailStatus(i, presoAtual);
                presoAnterior[i] = presoAtual;
            }

            boolean falidoAtual = j.isFalido();
            if (falidoAtual && !falidoAnterior[i]) {
                for (GameObserver o : observadores) o.onBankruptcy(i);
                falidoAnterior[i] = true;
            }
        }
    }

    private void notificarPropriedadeComprada(int indiceJogador, int celula) {
        for (GameObserver o : observadores) o.onPropertyBought(indiceJogador, celula);
    }
    private void notificarCasaConstruida(int indiceJogador, int celula, int numeroCasas) {
        for (GameObserver o : observadores) o.onHouseBuilt(indiceJogador, celula, numeroCasas);
    }

    /** Usa carta de liberação se disponível, notificando a UI. */
    private void tentarUsarCartaLiberacaoAutomatica(Jogador jogador, int indiceJogador) {
        if (!jogador.estaPreso()) return;
        if (jogador.getCartasLiberacao() <= 0) return;
        boolean usou = motor.usarCartaLiberacao(jogador);
        if (usou) {
            for (GameObserver o : observadores) o.onReleaseCardUsed(indiceJogador);
        }
    }
}
