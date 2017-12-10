package ru.serbis.mnvp.gateways;

import ru.serbis.mnvp.acceptors.Acceptor;
import ru.serbis.mnvp.acceptors.AcceptorConfig;
import ru.serbis.mnvp.acceptors.tcp.TcpAcceptor;
import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGateway;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.np.NetworkProcessor;
import ru.serbis.mnvp.np.PacketPool;
import ru.serbis.mnvp.structs.general.Packet;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Контроллер шлюзов. Задачей данного класса, явялется создание,
 * содержание и управление всему существующими у узла шлюзами. Любой
 * шдюз, порождается, управляется и уничтожается тольк в этом классе.
 *
 * Данный объект является пулом синглетонов с номерной регистрацией
 *
 */
public class GatewaysController implements Log, PacketUtils {
    /** Метка узла */
    private String nodeLabel;
    /** Пул шлюзов */
    private  Map<String, Gateway> gatewaysPool = new HashMap<>();
    /** Пул синглетонов */
    private static Map<String, GatewaysController> instancesPool = new HashMap<>();

    private PacketPool packetPool;

    /**
     * Регестрирует новый синглетон с заданным номером
     *
     * @param label метка синглетона
     */
    public static void registerInstance(String label) {
        instancesPool.put(label, new GatewaysController());
    }

    /**
     * Снимает регистрацию синглетона с заданным номером
     *
     * @param label метка синглетона
     */
    public static void unregisterInstance(String label) {
        instancesPool.remove(label);
    }

    /**
     * Экстрактор синглетона
     *
     * @param label метка синглетона
     * @return синглетон
     */
    public static GatewaysController getInstance(String label) {
        return instancesPool.get(label);
    }

    /**
     * Останавливает все отырытые шлюзы
     */
    public void stopAllGateways() {
        gatewaysPool.entrySet().forEach(stringGatewayEntry -> stringGatewayEntry.getValue().stop());
    }

    /**
     * Создает шлюз методом инициации нового соединения.
     *
     * @param config конфигурация шлюза
     */
    public void generateGateway(GatewayConfig config) {
        if (config instanceof TcpGatewayConfig) {
            generateTcpGateway((TcpGatewayConfig) config);
        }
    }

    /**
     * Создает новый шлюз методом инициации. Метод требует что бы в
     * конфигурации шлюза были заполнены поля типа G. Устанавливает соединение
     * с хостом и размерщает новый шлюз в пул шлюзов.
     *
     * @param config
     */
    private void generateTcpGateway(TcpGatewayConfig config) {
        if (config.getHost() == null) {
            log(String.format("<red>[%s] Ошибка при создании TCP шлюза методом инициации соединения. Не задат объект хост<nc>", nodeLabel), 1, nodeLabel);
            return;
        }

        if (config.getPort() == -1) {
            log(String.format("<red>[%s] Ошибка при создании TCP шлюза методом инициации соединения. Не задат целевой порт<nc>", nodeLabel), 1, nodeLabel);
            return;
        }

        if (config.getLabel() == null)
            config.setLabel(String.format("Gateway_TCP_%d", ThreadLocalRandom.current().nextInt(10000, 20000)));

        int retryCount = 1;
        Socket socket = null;

        while (retryCount <= config.getHostConnectRetryCount()) {
            try {
                socket = new Socket(config.getHost(), config.getPort());
                break;
            } catch (IOException e) {
                log(String.format("<yellow>[%s] Ошибка при создании TCP шлюза методом инициации соединения. Не удается установить соединения с хостом %s:%d. Поптытка - %d<nc>", nodeLabel, config.getHost(), config.getPort(), retryCount), 2, nodeLabel);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                retryCount++;
            }
        }

        if (socket == null) {
            log(String.format("<red>[%s] Ошибка при создании TCP шлюза методом инициации соединения. Не удалось установить соединение с хостом %s:%d.<nc>", nodeLabel, config.getHost(), config.getPort()), 1, nodeLabel);
            return;
        }

        try {
            socket.setSoTimeout(10000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        TcpGateway tcpGateway = new TcpGateway(socket, config.getLabel(), packetPool);
        tcpGateway.setNodeLabel(nodeLabel);
        tcpGateway.setLastIncomingActivity(new Date().getTime());
        log(String.format("<blue>[%s] Создан новый шлюз TCP методом иницииации к хосту %s:%d с меткой %s<nc>",nodeLabel, config.getHost(), config.getPort(), config.getLabel()), 3, nodeLabel);
        tcpGateway.run();
        gatewaysPool.put(config.getLabel(), tcpGateway);


        /*Packet packet = new Packet();
        packet.setMsgId(1);
        packet.setType((byte) 1);
        packet.setSource(0L);
        packet.setDest(1L);
        packet.setLength((short) 3);
        packet.setBody("ABC".getBytes());

        byte[] bt = packetToByteArray(packet);

        try {
            socket.getOutputStream().write(bt);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * Создает шлюз методо оборачивания существуеющего соединения
     *
     * @param config конфигурация шлюза
     */
    public void wrapGateway(GatewayConfig config) {
        if (config instanceof TcpGatewayConfig) {
            wrapTcpGateway((TcpGatewayConfig) config);
        }
    }

    /**
     * Возвращает итератор пула шлюзов
     *
     * @return итератор
     */
    public synchronized Iterator<Map.Entry<String, Gateway>> getGatewaysPoolIterator() {
        return gatewaysPool.entrySet().stream().iterator();
    }

    /**
     * Обновляет временную метку активности шлюза
     *
     * @param gatewayLabel метка шлюза
     */
    public void updateGatewayActivity(String gatewayLabel) {
        Gateway gateway = gatewaysPool.get(gatewayLabel);
        if (gateway == null) {
            log(String.format("[%s] <yellow>Ошибка обновления активности шлюза. Шлюз с меткой %s не найден<nc>", nodeLabel, gatewayLabel), 2, nodeLabel);
            return;
        }
        gateway.setLastIncomingActivity(new Date().getTime());
    }

    /**
     * Назначает щлюзу указанный в параметре сетевой адрес
     *
     * @param gatewayLabel метка шлюза
     * @param networkAddress сетевой адрес
     */
    public void setGatewayNetworkAddress(String gatewayLabel, int networkAddress) {
        Gateway gateway = gatewaysPool.get(gatewayLabel);
        if (gateway == null) {
            log(String.format("[%s] <yellow>Ошибка при назначении сетевого адреса шлюзу. Шлюз с меткой %s не найден<nc>", nodeLabel, gatewayLabel), 2, nodeLabel);
            return;
        }

        gateway.setNetworkAddress(networkAddress);
    }

    /**
     * Возвращает сетевой адрес узла к которому ведет шлюз с указанной в
     * параметре меткой
     *
     * @param gatewayLabel метка шлюза
     * @return  адрес узла к которому ведет шлюз
     */
    public int getGatewayNetworkAddress(String gatewayLabel) {
        Gateway gateway = gatewaysPool.get(gatewayLabel);
        if (gateway == null) {
            log(String.format("[%s] <yellow>Ошибка при получении сетевого адреса шлюза. Шлюз с меткой %s не найден<nc>", nodeLabel, gatewayLabel), 2, nodeLabel);
            return -1;
        }

        return gateway.getNetworkAddress();
    }


    /**
     * Возвращает шлюз по его сетевому адресу
     *
     * @param networkAddress сетевой адрес искомого шлюза
     * @return шлюз или null если последний не был найден
     */
    public Gateway getGatewayByNetworkAddress(int networkAddress) {
        Iterator<Map.Entry<String, Gateway>> iterator = gatewaysPool.entrySet().stream().iterator();

        while (iterator.hasNext()) {
            Gateway gateway = iterator.next().getValue();
            if (gateway.getNetworkAddress() == networkAddress)
                return gateway;
        }

        return null;
    }

    /**
     * Создает новый tcp шлюз методом оборачиваня объекта socket и добавляет
     * его в пул шлюзов
     *
     * @param config конфигурация шлюза
     */
    private void wrapTcpGateway(TcpGatewayConfig config) {
        if (config.getSocket() == null) {
            log(String.format("<yellow>[%s] Ошибка при создании TCP шлюза методом оборачивания соединения. Не задат объект Socket<nc>", nodeLabel), 2, nodeLabel);
            return;
        }

        if (config.getLabel() == null)
            config.setLabel(String.format("Gateway_TCP_%d", ThreadLocalRandom.current().nextInt(10000, 20000)));

        TcpGateway tcpGateway = new TcpGateway(config.getSocket(), config.getLabel(), packetPool);
        tcpGateway.setNodeLabel(nodeLabel);
        tcpGateway.setLastIncomingActivity(new Date().getTime());
        log(String.format("<blue>[%s] Создан новый шлюз TCP методом обертывания с меткой %s<nc>",nodeLabel, config.getLabel()), 3, nodeLabel);
        tcpGateway.run();
        gatewaysPool.put(config.getLabel(), tcpGateway);

    }

    /**
     * Удаляет шлюз из пула. Данная процедура помимо удаления самого шлюза,
     * задействует процедуру удаления записей в таблице маршрутиизации
     * связанных с сетевым адром шлюза.
     */
    public void removeGateway(String gatewayLabel) {
        //Найти шлюз
        Gateway gateway = gatewaysPool.get(gatewayLabel);
        if (gateway == null) {
            log(String.format("[%s] <yellow>Ошибка при удалении сетевого адреса шлюза. Шлюз с меткой %s не найден<nc>", nodeLabel, gatewayLabel), 2, nodeLabel);
            return;
        }

        int gatewayAddress = gateway.getNetworkAddress();
        //Остановить и удалить
        gateway.stop();
        gatewaysPool.remove(gatewayLabel);
        log(String.format("<blue>[%s] Удален шлюз TCP с меткой %s<nc>",nodeLabel, gatewayLabel), 3, nodeLabel);

        //Очистить таблицу маршрутизации от записей с данным шлюзом
        NetworkProcessor.getInstance(nodeLabel).removeRoutesByGateway(gatewayAddress);
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public void setPacketPool(PacketPool packetPool) {
        this.packetPool = packetPool;
    }
}
