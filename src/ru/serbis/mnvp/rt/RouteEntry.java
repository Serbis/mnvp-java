package ru.serbis.mnvp.rt;

/**
 * Представление строки таблицы маршрутизации
 */
public class RouteEntry {
    /** Сетевой адрес целевого узел */
    private int dest;
    /** Сетевой адрес шлюзового узла */
    private int gateway;
    /** Дистанция до целевого узла */
    private int distance;

    public int getDest() {
        return dest;
    }

    public void setDest(int dest) {
        this.dest = dest;
    }

    public int getGateway() {
        return gateway;
    }

    public void setGateway(int gateway) {
        this.gateway = gateway;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
