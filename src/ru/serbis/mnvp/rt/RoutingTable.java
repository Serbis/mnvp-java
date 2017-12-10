package ru.serbis.mnvp.rt;

import ru.serbis.mnvp.mix.Log;

import java.util.*;

/**
 * Инкапслирует таблица маршрутизации и логиуку работы с ней
 */
public class RoutingTable implements Log {
    /** Метка узла */
    private String nodeLabel;
    /** Таблица маршрутов */
    private List<RouteEntry> table = new ArrayList<>();


    public RoutingTable(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    /**
     * Обновляет маршрут в таблице. Если маршрут не был найден в таблице,
     * создает новую маршрутную запись.
     *
     * @param dest сетевой адрес узла наначения
     * @param gateway сетевой адрес шлюза
     * @param distance дистанция до целевого узла
     */
    public synchronized void updateRoute(int dest, int gateway, int distance) {
        if (gateway == 0)
            return;

        RouteEntry entry = null;
        if (dest == 7 && nodeLabel.equals("node_A"))
            System.out.println("x");
        for (RouteEntry e: table) {
            if (e.getDest() == dest && e.getGateway() == gateway && e.getDistance() == distance)
                entry = e;
        }

        if (entry != null) {
            if (entry.getDest() != dest) {
                entry.setDistance(distance);
                log(String.format("<blue>[%s] Обновлена запись в таблице маршрутизации Dest=%d Gateway=%d Distance=%d<nc>", nodeLabel, dest, gateway, distance), 3, nodeLabel);
            }
        } else {
            RouteEntry ne = new RouteEntry();
            ne.setDest(dest);
            ne.setGateway(gateway);
            ne.setDistance(distance);

            table.add(ne);
            log(String.format("<blue>[%s] Созданна запись в таблице маршрутизации Dest=%d Gateway=%d Distance=%d<nc>", nodeLabel, dest, gateway, distance), 3, nodeLabel);
        }

        //log(String.format("<blue>[%s] UPDATE ROUTE - dest = %d, gateway = %d, distance = %d<nc>", nodeLabel, dest, gateway, distance), 10, nodeLabel);
    }

    /**
     * Удаляет все маршруты из таблицы, шлюзом в которых является адрес
     * указанный в параметре.
     *
     * @param gateway сетевой адрес искомого для удаления шлюза
     */
    public synchronized void removeAllRoutesByGateway(int gateway) {
        Iterator<RouteEntry> iterator = table.iterator();

        while (iterator.hasNext()) {
            RouteEntry e = iterator.next();
            if (e.getGateway() == gateway) {
                iterator.remove();
                log(String.format("<blue>[%s] Удалена запись в таблице маршрутизации Dest=%d Gateway=%d Distance=%d<nc>", nodeLabel, e.getDest(), e.getGateway(), e.getDistance()), 3, nodeLabel);
            }
        }
    }

    /**
     * Ищет шлюз до целвого узла и возвращает его номер, или -1 в том случае
     * если последний не был найден.
     *
     * @param dest искомый сетевой вдрес цели
     * @return сетевой адрес шлюза или -1 если маршрут не был найден
     */
    public synchronized int findRoute(int dest) {
        Iterator<RouteEntry> iterator = table.iterator();

        int min = 1000000;
        RouteEntry re = null;

        while (iterator.hasNext()) {
            RouteEntry entry = iterator.next();
            if (entry.getDest() == dest && entry.getDistance() < min) {
                re = entry;
                min = entry.getDistance();
            }
        }

        if (re != null)
            return re.getGateway();

        return -1;
    }
}
