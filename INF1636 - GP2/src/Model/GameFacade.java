package Model;

import java.awt.Color;
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

    /** Paleta fixa de cores de pino (alinhada ao save). */
    private static final Color[] PIN_PALETTE = new Color[] {
            Color.RED, Color.BLUE, Color.ORANGE, Color.YELLOW, Color.PINK, Color.GRAY
    };

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
    public int  getIndiceJogadorDaVez()                { ajustarPonteiroParaJogadorAtivo(); return ordem[ponteiroDaVez]; }
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
        return podeConstruirCasaAqui(indiceJogador) || podeConstruirHotelAqui(indiceJogador);
    }

    /** Indica se o jogador pode construir casa na posicao atual. */
    public boolean podeConstruirCasaAqui(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(j.getPosicao());
        if (p instanceof Terreno t) return t.getProprietario() == j && t.podeConstruirCasa();
        return false;
    }

    /** Indica se o jogador pode construir hotel na posicao atual. */
    public boolean podeConstruirHotelAqui(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(j.getPosicao());
        if (p instanceof Terreno t) return t.getProprietario() == j && t.podeConstruirHotel();
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

            /** Indica se a proxima construcao obrigatoriamente sera hotel (sem vaga para novas casas). */
    public boolean proximaConstrucaoEhHotelAqui(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(j.getPosicao());
        if (p instanceof Terreno t) {
            return t.getProprietario() == j && !t.temHotel() && !t.podeConstruirCasa() && t.podeConstruirHotel();
        }
        return false;
    }
    /** Consulta se a célula é de Sorte/Revés (regra do domínio). */
    public boolean isChanceCell(int celula) { return tabuleiro.isChanceCell(norm40(celula)); }
    /** Posição da casa de prisão/visita (casa 10). */
    public int getPosicaoPrisao() { return Tabuleiro.getPosicaoVisitaPrisao(); }

    // ---------- Ações atômicas para o Controller orquestrar ----------
    /** Sorteia dois dados (1..6) e retorna lista [d1,d2]. */
    public List<Integer> sortearDados() { return motor.lancarDados(); }

    /** Notifica rolagem aos observadores. */
    public void notificarRolagem(int d1, int d2) {
        for (GameObserver o : observadores) o.onDice(d1, d2);
    }

    /** Tenta liberar da prisão caso seja dupla; retorna true se liberou. */
    public boolean tentarLiberarComDupla(int indiceJogador, int d1, int d2) {
        return motor.soltarSeDupla(jogadores.get(indiceJogador), Arrays.asList(d1, d2));
    }

    /** Move o jogador pelos dados e notifica movimento. */
    public void moverJogadorComDados(int indiceJogador, int d1, int d2) {
        Jogador j = jogadores.get(indiceJogador);
        int origem = j.getPosicao();
        motor.moverJogador(j, Arrays.asList(d1, d2));
        int destino = j.getPosicao();
        for (GameObserver o : observadores) o.onMoved(indiceJogador, origem, destino);
    }

    /** Aplica casas especiais fixas (lucros/dividendos e IR) e notifica. */
    public void aplicarCasasEspeciais(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        int celula = j.getPosicao();
        if (tabuleiro.isCasaLucrosDividendos(celula)) {
            banco.getConta().paga(j.getConta(), 200);
            for (GameObserver o : observadores) o.onSpecialCell(indiceJogador, celula, 200, "Lucros ou dividendos: +200");
        } else if (tabuleiro.isCasaImpostoRenda(celula)) {
            boolean pagou = j.getConta().paga(banco.getConta(), 200); // TEMP: valor elevado para testes de falência
            if (!pagou) {
                j.setFalido(true);
                motor.verificarFalencia(j);
            }
            for (GameObserver o : observadores) o.onSpecialCell(indiceJogador, celula, -200, "Imposto de renda: -200 (teste)");
        }
    }

    /** Cobra aluguel se aplicável; notifica via onRentPaid e retorna valor pago. */
    public int cobrarAluguelSeNecessario(int indicePagador) {
        Jogador pagador = jogadores.get(indicePagador);
        Propriedade prop = tabuleiro.getPropriedadeNaPosicao(pagador.getPosicao());
        if (prop == null) return 0;
        Jogador dono = prop.getProprietario();
        if (dono == null || dono == pagador) return 0;
        int saldoAntes = pagador.getConta().getSaldo();
        motor.pagarAluguel(pagador, prop);
        int valorPago = Math.max(0, saldoAntes - pagador.getConta().getSaldo());
        if (valorPago > 0) {
            int indiceDono = indexOf(dono);
            if (indiceDono >= 0) {
                for (GameObserver o : observadores) o.onRentPaid(indicePagador, indiceDono, pagador.getPosicao(), valorPago);
            }
        }
        return valorPago;
    }

    /** Resolve carta se estiver em casa de Sorte/Revés; aplica e notifica. Retorna carta ou null. */
    public Carta resolverChanceSeNecessario(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        int celula = j.getPosicao();
        if (!tabuleiro.isChanceCell(celula)) return null;
        Carta c = motor.puxarSorteReves(j);
        for (GameObserver o : observadores) {
            o.onChanceCard(indiceJogador, celula, c.codigo, c.tipo.name(), c.valor);
        }
        return c;
    }

    /** Usa carta de liberação automática se houver (já notifica onReleaseCardUsed). */
    public void usarCartaLiberacaoAutomatica(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        if (!j.estaPreso() || j.getCartasLiberacao() <= 0) return;
        boolean usou = motor.usarCartaLiberacao(j);
        if (usou) for (GameObserver o : observadores) o.onReleaseCardUsed(indiceJogador);
    }

    /** Notifica diffs de estado (saldo/preso/falido). */
    public void notificarEstado() { detectarENotificarEstadoGlobal(); }

    /** Avança turno e notifica. */
    public void avancarTurnoENotificar() { avancarVezENotificar(); }

    // ---------- Persistência ----------
    public void salvarParaArquivo(java.io.File arquivo, Color[] coresJogadores) throws java.io.IOException {
        GameStateIO.salvar(snapshot(coresJogadores), arquivo);
    }
    public static GameStateSnapshot carregarSnapshot(java.io.File arquivo) throws java.io.IOException {
        return GameStateIO.carregar(arquivo);
    }
    public static GameFacade initFromSnapshot(GameStateSnapshot snap) {
        return carregarDeSnapshot(snap);
    }

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

    /** ConstrA3i hotel na posicao atual do jogador (se puder e pagar). */
    public void construirHotelNoLocal(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        int celula = j.getPosicao();
        Propriedade p = tabuleiro.getPropriedadeNaPosicao(celula);

        boolean tinhaHotelAntes = p instanceof Terreno t && t.temHotel();
        int casasAntes = p instanceof Terreno t ? t.getNumCasas() : -1;

        motor.construirHotel(j, p);

        boolean temHotelDepois = p instanceof Terreno t && t.temHotel();
        int casasDepois = p instanceof Terreno t ? t.getNumCasas() : -1;

        if (p instanceof Terreno && (temHotelDepois && !tinhaHotelAntes || casasDepois > casasAntes)) {
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
        verificarFimPorUnicoRestante();
        return faliu;
    }

    /** Encerra partida manualmente (ex.: botão/fechar janela). */
    public void encerrarPartida() {
        notificarFimPartida();
    }

    // ---------- Fluxo interno ----------

    // ---------- Consultas auxiliares para HUD ----------
    public int getNumeroJogadores() {
        return jogadores.size();
    }

    private int indexOf(Jogador j) {
        for (int i = 0; i < jogadores.size(); i++) if (jogadores.get(i) == j) return i;
        return -1;
    }

    /** Ajusta ponteiro para um jogador não falido, se necessário. */
    private void ajustarPonteiroParaJogadorAtivo() {
        int tentativas = jogadores.size();
        while (tentativas-- > 0 && jogadores.get(ordem[ponteiroDaVez]).isFalido()) {
            ponteiroDaVez = (ponteiroDaVez + 1) % ordem.length;
        }
    }

    private void avancarVezENotificar() {
        // avança até encontrar um não falido ou concluir que acabou
        int vivos = 0;
        for (Jogador j : jogadores) if (!j.isFalido()) vivos++;
        if (vivos <= 1) { notificarFimPartida(); return; }

        int tentativas = jogadores.size();
        do {
            ponteiroDaVez = (ponteiroDaVez + 1) % ordem.length;
            tentativas--;
        } while (tentativas > 0 && jogadores.get(ordem[ponteiroDaVez]).isFalido());

        int atual = getIndiceJogadorDaVez();
        for (GameObserver o : observadores) o.onTurnChanged(atual);
    }

    private static int norm40(int v) { return ((v % 40) + 40) % 40; }

    // ---------- Suporte a salvar/carregar ----------
    GameStateSnapshot snapshot(Color[] coresJogadores) {
        List<GameStateSnapshot.PlayerData> players = new ArrayList<>();
        for (int i = 0; i < jogadores.size(); i++) {
            Jogador j = jogadores.get(i);
            Color cor = (coresJogadores != null && i < coresJogadores.length && coresJogadores[i] != null)
                    ? coresJogadores[i] : Color.GRAY;
            int corIndex = colorToIndex(cor);
            players.add(new GameStateSnapshot.PlayerData(
                    j.getNome(), corIndex, j.getConta().getSaldo(), j.getPosicao(),
                    j.estaPreso(), j.isFalido(), j.getCartasLiberacao()
            ));
        }
        List<GameStateSnapshot.PropertyData> props = new ArrayList<>();
        for (int pos = 0; pos < 40; pos++) {
            Propriedade p = tabuleiro.getPropriedadeNaPosicao(pos);
            if (p == null) continue;
            int owner = -1;
            if (p.getProprietario() != null) owner = jogadores.indexOf(p.getProprietario());
            int casas = (p instanceof Terreno t) ? t.getNumCasas() : 0;
            int hotel = (p instanceof Terreno t && t.temHotel()) ? 1 : 0;
            props.add(new GameStateSnapshot.PropertyData(pos, owner, casas, hotel));
        }
        List<Carta> deck = new ArrayList<>(tabuleiro.baralhoSorteReves);
        return new GameStateSnapshot(banco.getSaldo(), ordem.clone(), ponteiroDaVez, players, props, deck);
    }

    static GameFacade carregarDeSnapshot(GameStateSnapshot snap) {
        String[] nomes = snap.players().stream().map(GameStateSnapshot.PlayerData::nome).toArray(String[]::new);
        GameFacade gf = new GameFacade(nomes, snap.ordem());
        INSTANCIA = gf;

        gf.banco.setSaldo(snap.bancoSaldo());
        gf.ponteiroDaVez = ((snap.ponteiro() % gf.ordem.length) + gf.ordem.length) % gf.ordem.length;

        for (int i = 0; i < gf.jogadores.size(); i++) {
            GameStateSnapshot.PlayerData p = snap.players().get(i);
            Jogador j = gf.jogadores.get(i);
            j.setSaldo(p.saldo());
            j.setPosicao(p.posicao());
            j.setPreso(p.preso());
            j.setFalido(p.falido());
            j.setCartasLiberacao(p.cartasLiberacao());
            if (p.falido()) gf.tabuleiro.removerJogador(j);
        }

        for (GameStateSnapshot.PropertyData pd : snap.propriedades()) {
            Propriedade p = gf.tabuleiro.getPropriedadeNaPosicao(pd.posicao());
            if (p == null) continue;
            if (pd.ownerIndex() >= 0 && pd.ownerIndex() < gf.jogadores.size()) {
                p.setProprietario(gf.jogadores.get(pd.ownerIndex()));
            }
            if (p instanceof Terreno t) {
                t.resetConstrucoes();
                for (int i = 0; i < pd.casas(); i++) t.adicionaCasa();
                if (pd.hotel() == 1) t.adicionaHotel();
            }
        }

        gf.tabuleiro.baralhoSorteReves.clear();
        gf.tabuleiro.baralhoSorteReves.addAll(snap.deck());

        gf.recalcularDiffs();
        return gf;
    }

    /* package */ List<Jogador> getJogadores() { return jogadores; }
    /* package */ Tabuleiro getTabuleiro() { return tabuleiro; }
    /* package */ Banco getBanco() { return banco; }
    /* package */ int[] getOrdem() { return ordem; }
    /* package */ int getPonteiro() { return ponteiroDaVez; }
    /* package */ void setPonteiro(int p) { this.ponteiroDaVez = ((p % ordem.length) + ordem.length) % ordem.length; }
    /* package */ List<GameObserver> getObservadores() { return observadores; }
    /* package */ void recalcularDiffs() {
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
        tabuleiro.addPropriedade(new Terreno("Botafogo",                   100, 50, 10, 23));
        tabuleiro.addPropriedade(new Companhia("Companhia de Navegação",   150, 1, 25,  25));
        tabuleiro.addPropriedade(new Terreno("Av. Brasil",                 160, 80, 16, 26));
        tabuleiro.addPropriedade(new Terreno("Av. Paulista",               140, 70, 14, 28));
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

    /** Se restar apenas um jogador não falido, encerra a partida automaticamente. */
    private void verificarFimPorUnicoRestante() {
        int vivo = -1, vivos = 0;
        for (int i = 0; i < jogadores.size(); i++) {
            if (!jogadores.get(i).isFalido()) { vivos++; vivo = i; }
        }
        if (vivos == 1) {
            notificarFimPartida();
        }
    }

    /** Calcula capital de cada jogador e notifica observadores com o vencedor. */
    private void notificarFimPartida() {
        int n = jogadores.size();
        int[] capitais = new int[n];
        int winner = -1;
        int maior = Integer.MIN_VALUE;

        for (int i = 0; i < n; i++) {
            capitais[i] = calcularCapital(i);
            if (capitais[i] > maior) {
                maior = capitais[i];
                winner = i;
            }
        }
        for (GameObserver o : observadores) o.onGameEnded(winner, capitais);
    }

    /** Capital = saldo + valor das propriedades + construções. */
    private int calcularCapital(int indiceJogador) {
        Jogador j = jogadores.get(indiceJogador);
        if (j.isFalido()) return 0;
        int total = j.getConta().getSaldo();
        for (int pos = 0; pos < 40; pos++) {
            Propriedade p = tabuleiro.getPropriedadeNaPosicao(pos);
            if (p != null && p.getProprietario() == j) {
                total += p.getPreco();
                if (p instanceof Terreno t) {
                    total += t.getNumCasas() * t.getValorCasa();
                    if (t.temHotel()) total += t.getValorHotel();
                }
            }
        }
        return total;
    }

    /** Mapeia cor para o índice de pino (0..5) usado no save. */
    private static int colorToIndex(Color c) {
        if (c == null) return 0;
        for (int i = 0; i < PIN_PALETTE.length; i++) if (PIN_PALETTE[i].equals(c)) return i;
        return 0;
    }
}
