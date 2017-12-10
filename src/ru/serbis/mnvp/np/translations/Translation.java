package ru.serbis.mnvp.np.translations;

/**
 * Описывает базовый объект трансляции
 */
public class Translation {
    /** Метка узла */
    private String nodeLabel;
    /** Идентификатор трансляции */
    private int id;
    /** Флаг жизни потока */
    private boolean alive = true;


    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
