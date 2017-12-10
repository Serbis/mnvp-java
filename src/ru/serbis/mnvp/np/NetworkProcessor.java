package ru.serbis.mnvp.np;

import ru.serbis.mnvp.gateways.Gateway;
import ru.serbis.mnvp.gateways.GatewaysController;
import ru.serbis.mnvp.general.NodeVars;
import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.mix.NetworkUtils;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.np.translations.PreqTranslation;
import ru.serbis.mnvp.np.translations.Translation;
import ru.serbis.mnvp.rt.RoutingTable;
import ru.serbis.mnvp.structs.general.Packet;

import java.util.*;
import java.util.concurrent.*;

/**
 * Контроллер сети. Является опорным классом для запуска потоков обработки
 * входящих пакетов а так же предоставляет большую часть api для работы
 * с сетью
 *
 * Данный объект является пулом синглетонов с номерной регистрацией
 *
 */
public class NetworkProcessor implements Log, PacketUtils, NetworkUtils {
    /** Пул синглетонов */
    private static Map<String, NetworkProcessor> instancesPool = new HashMap<>();
    /** Пул потоков ресивера входящих пакетов */
    private ThreadPoolExecutor executor;
    /** Список запущенных потоков ресивера пакетов*/
    private List<PacketReceiverThread> receiverThreadList;
    /** Пул пакетов */
    private LinkedList<Packet> packetsStack;
    /** Текстова метка */
    private String label;
    /** Метка узла */
    private String nodeLabel;
    /** Таблица маршрутизации */
    private RoutingTable routingTable;
    /** Пул трансляций */
    private Map<Integer, Translation> translationsPool = new HashMap<>();
    /** Семафор разрешения размещения нового пакета в пуле */
    private Semaphore packetPoolSemaphore = new Semaphore(1);
   // /** Семафор резрешения получение нового пакета из пула */
    //private Semaphore getNewPacketSemaphore = new Semaphore(1);
    private PacketReceiverThread prt;

    private PacketPool packetPool;

    /**
     * Регестрирует новый синглетон с заданным номером
     *
     * @param label метка синглетона
     */
    public static synchronized void registerInstance(String label) {
        instancesPool.put(label, new NetworkProcessor());
    }

    /**
     * Экстрактор синглетона
     *
     * @param label метка синглетона
     * @return синглетон
     */
    public static NetworkProcessor getInstance(String label) {
        return instancesPool.get(label);
    }

    /**
     * Запускает сетевой процессор в работу. Инициализирует систему данными
     * из конфигурации. Запуска потоки ресиверы пакетов
     *
     * @param config конфигурация сетевого процессора
     */
    public void run(NetworkProcessorConfig config) {
        log(String.format("<blue>[%s->%s] Создан новый сетевой процессор с меткой %s<nc>", nodeLabel, label, label), 3, nodeLabel);

        packetsStack = new LinkedList<>();
        receiverThreadList = new ArrayList<>();
        routingTable = new RoutingTable(nodeLabel);

        prt = new PacketReceiverThread(String.format("%s->%s->PacketReceiverThread_%d", nodeLabel, label, ThreadLocalRandom.current().nextInt(10000, 20000)), nodeLabel, packetPool);
        Thread thread = new Thread(prt);
        thread.start();
        //executor = new ThreadPoolExecutor(config.getPacketReceiverThreadCount(), config.getPacketReceiverThreadCount(), 1000000,
        //        TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

        //for (int i = 0; i < config.getPacketReceiverThreadCount(); i++) {
        //    PacketReceiverThread packetReceiverThread = new PacketReceiverThread(String.format("%s->%s->PacketReceiverThread_%d", nodeLabel, label, ThreadLocalRandom.current().nextInt(10000, 20000)), nodeLabel);
        //    receiverThreadList.add(packetReceiverThread);
        //    executor.execute(packetReceiverThread);
        //}
    }

    /**
     * Останавливает сетевой процессор
     */
    public void stop() {
        receiverThreadList.forEach(PacketReceiverThread::stop);
        receiverThreadList.forEach(receiverThread -> {
            while (!receiverThread.isStopped()) {}
        });
    }

    /**
     * Помещает в пул потоков новый пакет на обработку
     *
     * @param packet входящий пакет
     */
    public void receivePacket(Packet packet) {
        if (packet.getTtl() == 6 && nodeLabel.equals("node_B")) {
            System.out.println("x");
        }
        if (packet.getTtl() == 5 && nodeLabel.equals("node_D")) {
            System.out.println("x");
        }
        if (packet.getTtl() == 4 && nodeLabel.equals("node_H")) {
            System.out.println("x");
        }
        if (packet.getTtl() == 3 && nodeLabel.equals("node_N")) {
            System.out.println("x");
        }
        if (packet.getTtl() == 2 && nodeLabel.equals("node_R")) {
            System.out.println("x");
        }
        //if (packet.getTtl() == 1 && nodeLabel.equals("node_T")) {
        //    System.out.println("x");
        //}
        prt.getPacketsStack().addFirst(packet);
        packetPoolSemaphore.release();
    }

    /**
     * Изымает из пула пакетов. Данный метод вызывает поток ресивера пакетов
     *
     * @return пакет
     */
    public Packet getNewPacket() {
        try {

            Packet packet = null;
            if (packetsStack.size() > 0)
                packet = packetsStack.removeLast();
            /*if (packet.getTtl() == 6 && nodeLabel.equals("node_B")) {
                System.out.println("x");
            }
            if (packet.getTtl() == 5 && nodeLabel.equals("node_D")) {
                System.out.println("x");
            }
            if (packet.getTtl() == 4 && nodeLabel.equals("node_H")) {
                System.out.println("x");
            }
            if (packet.getTtl() == 3 && nodeLabel.equals("node_N")) {
                System.out.println("x");
            }
            if (packet.getTtl() == 2 && nodeLabel.equals("node_R")) {
                System.out.println("x");
            }*/
           // if (packet != null)
           //     packetsStack.remove(packet);
            packetPoolSemaphore.release();
            return packet;
        } catch (Exception ignored) {
            packetPoolSemaphore.release();
            return null;
        }
    }

    /**
     * Обновляет запись в таблице маршрутизации об узле
     *
     * @param dest адрес целевого узла
     * @param gateway шлюз к целвогму узлу
     * @param distance дистанция до целвого узла
     */
    public void updateRoute(int dest, int gateway, int distance) {
        routingTable.updateRoute(dest, gateway, distance);
    }

    /**
     * Выполняет эхо запрос к удаленному узлу.
     *
     * Данный запрос является транслятивным запросом.
     * Данный пакет является подтверждаемым ответным пакетом.
     *
     * @param packet пакет
     * @param timeout таймаут в мс, после которого, пакет будет считаться
     *                потерянным и трансляция будет завершена
     */
    public void sendEchoRequest(Packet packet, int timeout) {

    }

    /**
     * Выполняет запрос на динамический поиск маршрута до целевого узла
     *
     * @param dest сетевой адрес искомого узла
     * @param finisherCallback обртный вызов, который будет совершен после
     *                         заврешния трансляции
     */
    public void sendPreqRequest(int dest, PreqTranslation.TranslationFinisher finisherCallback) {
        Packet packet = createPreqPacket(getNewMsgId(nodeLabel), NodeVars.getInstance(nodeLabel).getNetworkAddress(), dest);
        //Создать preq пакет

        //Создать трансляцию
        PreqTranslation preqTranslation = new PreqTranslation(packet, nodeLabel, finisherCallback, (id) -> translationsPool.remove(id));

        //Внести трансляцию в пул трансляций
        translationsPool.put(packet.getMsgId(), preqTranslation);

        //Запустить трансляцию
        preqTranslation.start();
    }

    /**
     * Выполняет отправку пакета в сеть. В задачу входит поиск маршрута в
     * таблице маршрутизации и отправку пакета в найденый шлюз. Если маршрут
     * не был обнаружен в таблице, вовзращает статус ROUTE_NOT_FOUND. Если
     * отправка пакеты была выполнена успешно возвращает статус OK.
     *
     * @param packet пакет для отправки
     * @return статус отправки пакета
     */
    public PacketSendResult sendPacket(Packet packet) {
        int gatewayAddr = routingTable.findRoute(packet.getDest());
        if (gatewayAddr == -1) {
            log(String.format("<blue>[%s->%s] Не удалось отправить пакет к узлу %d - не найден маршрут<nc>", nodeLabel, label, packet.getDest()), 3, nodeLabel);

            return PacketSendResult.ROUTE_NOT_FOUND;
        }

        Gateway gateway = GatewaysController.getInstance(nodeLabel).getGatewayByNetworkAddress(gatewayAddr);

        if (gateway == null) {
            log(String.format("<red>[%s->%s] Не удалось отправить пакет к узлу %d - не найден объект шлюза<nc>", nodeLabel, label, packet.getDest()), 1, nodeLabel);

            return PacketSendResult.INTERNAL_ERROR;
        }

        boolean gwsr = false;
        try {
            gateway.getSendSemaphore().acquire();
            gwsr =  gateway.send(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        if (!gwsr)
            return PacketSendResult.INTERNAL_ERROR;

        return PacketSendResult.OK;
    }

    /**
     * Возвращает объект транслции по ее идентификатору
     *
     * @param translationId идентификатор трансляции
     * @return объект трансяции, может вернуть null, если трансляция не
     *         найдена в пуле
     */
    public Translation getTranslation(int translationId) {
        return translationsPool.get(translationId);
    }

    /**
     * Удаляет все записи в таблице маршрутизации с указанным шлюзом
     *
     * @param gateway сетевой адрес шлюза
     */
    public void removeRoutesByGateway(int gateway) {
        routingTable.removeAllRoutesByGateway(gateway);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    /*public Semaphore getReceivePacketSemaphore() {
        return receivePacketSemaphore;
    }

    public Semaphore getGetNewPacketSemaphore() {
        return getNewPacketSemaphore;
    }*/


    public Semaphore getPacketPoolSemaphore() {
        return packetPoolSemaphore;
    }

    public void setPacketPool(PacketPool packetPool) {
        this.packetPool = packetPool;
    }

    /**
     * Перечисление результатов выполнения отправки пакета
     */
    public enum PacketSendResult {
        OK, ROUTE_NOT_FOUND, INTERNAL_ERROR
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetworkProcessor that = (NetworkProcessor) o;

        if (executor != null ? !executor.equals(that.executor) : that.executor != null) return false;
        if (receiverThreadList != null ? !receiverThreadList.equals(that.receiverThreadList) : that.receiverThreadList != null)
            return false;
        if (packetsStack != null ? !packetsStack.equals(that.packetsStack) : that.packetsStack != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (nodeLabel != null ? !nodeLabel.equals(that.nodeLabel) : that.nodeLabel != null) return false;
        if (routingTable != null ? !routingTable.equals(that.routingTable) : that.routingTable != null) return false;
        if (translationsPool != null ? !translationsPool.equals(that.translationsPool) : that.translationsPool != null)
            return false;
        return packetPoolSemaphore != null ? packetPoolSemaphore.equals(that.packetPoolSemaphore) : that.packetPoolSemaphore == null;
    }

    @Override
    public int hashCode() {
        int result = executor != null ? executor.hashCode() : 0;
        result = 31 * result + (receiverThreadList != null ? receiverThreadList.hashCode() : 0);
        result = 31 * result + (packetsStack != null ? packetsStack.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (nodeLabel != null ? nodeLabel.hashCode() : 0);
        result = 31 * result + (routingTable != null ? routingTable.hashCode() : 0);
        result = 31 * result + (translationsPool != null ? translationsPool.hashCode() : 0);
        result = 31 * result + (packetPoolSemaphore != null ? packetPoolSemaphore.hashCode() : 0);
        return result;
    }
}
