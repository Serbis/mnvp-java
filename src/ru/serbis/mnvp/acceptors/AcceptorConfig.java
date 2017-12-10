package ru.serbis.mnvp.acceptors;

/**
 * Общий инетрфейс конфигурации акцептора соединения
 */
public class AcceptorConfig {
    /** Имя акцептора. Данный параметр задает метку, которая будет
     * интегрироваться во все порождаемые ацептором структуры (потоки,
     * производные и пр.) */
    private String label = null;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
