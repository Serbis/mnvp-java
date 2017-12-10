package ru.serbis.mnvp.acceptors;

/**
 * Суперкласс акцетора сетевого протокола. Под акцептором следует понимать
 * программную конструкцию, предназначеную для захвата новых соединений для
 * порождения шлюзов.
 */
public class Acceptor {
    /** Конфигурация акцептора */
    private AcceptorConfig config;
    /** Текстовая метка узла */
    private String nodeLabel;
    /** Текстовая метка акцептора */
    private String label;


    public Acceptor() {
    }

    public Acceptor(AcceptorConfig config) {
        this.config = config;
    }

    /**
     * Задает конфигурацию акцептора
     *
     * @param config определение конфигурации
     */
    public void setConfig(AcceptorConfig config) {}

    /**
     * Запускает акцептор
     */
    public void run() {}

    /**
     * Останавливает акцетор
     */
    public void stop() {}

    public AcceptorConfig getConfig() {
        return config;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
