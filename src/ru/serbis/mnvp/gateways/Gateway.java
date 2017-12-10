package ru.serbis.mnvp.gateways;

import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.np.PacketPool;
import ru.serbis.mnvp.structs.general.Packet;

import java.util.concurrent.Semaphore;

/**
 * Суперкласс сетевого шлюза. Под последним следует понимать либой вид сетевого
 * соединения, ведущий к другому узлу обернутый с пециальный класс, содержащий
 * методы для работы с данным соединением. Например объект Socket, является
 * tcp соединеним вокруг которого оборачивает представлене шлюза TcpGateWay
 */
public class Gateway implements Log {
    /** Метка узла */
    private String nodeLabel;
    /** Текстовая метка шлюза */
    private String label;
    /** Време последней внешней активности шлюаз (читай получения данных из шлюза) */
    private long lastIncomingActivity;
    /** Сетевой адрес узла, к котому веден данный шлюз */
    private int networkAddress;
    /** Семафор разрешения записи данных в шлюз */
    private Semaphore sendSemaphore = new Semaphore(1);

    private PacketPool packetPool;


    /**
     * Релизация логики работы шлюза. В стандартной ситуации, реализация
     * данного метода, должна создать поток, которй должен отслеживать
     * поступающие из шлюза данные, формировать из них пакеты и передавать
     * сетевому процессору.
     */
    public void run() {}

    /**
     * Реализует логику закрытия шлюза. В стандартной ситуации, реализация
     * данного метода должны зыкрыть все активные потоки шлюза.
     */
    public void stop() {}

    /**
     * Реализует отправку пакета в шлюз
     *
     * @param packet пакет
     */
    public boolean send(Packet packet) { return true; }


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

    public long getLastIncomingActivity() {
        return lastIncomingActivity;
    }

    public void setLastIncomingActivity(long lastIncomingActivity) {
        log(String.format("<blue>[%s->%s] Обновлена метка времени последней активности шлюза<nc>", nodeLabel, label), 10, nodeLabel);
        this.lastIncomingActivity = lastIncomingActivity;
    }

    public int getNetworkAddress() {
        return networkAddress;
    }

    public void setNetworkAddress(int networkAddress) {
        this.networkAddress = networkAddress;
    }

    public Semaphore getSendSemaphore() {
        return sendSemaphore;
    }

    public PacketPool getPacketPool() {
        return packetPool;
    }

    public void setPacketPool(PacketPool packetPool) {
        this.packetPool = packetPool;
    }
}
