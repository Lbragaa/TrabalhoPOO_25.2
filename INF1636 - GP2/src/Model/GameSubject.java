package Model;

/**
 * Subject (observado) do padrão Observer.
 * <p>
 * Classes que publicam eventos de jogo expõem esta interface para que
 * observadores (ex.: a camada de Controller/GUI) possam assinar e
 * cancelar a assinatura das notificações.
 * </p>
 */
public interface GameSubject {
    /** Registra um observador para receber callbacks. */
    void addObserver(GameObserver o);

    /** Remove um observador previamente registrado. */
    void removeObserver(GameObserver o);
}
