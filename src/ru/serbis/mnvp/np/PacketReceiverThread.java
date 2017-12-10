package ru.serbis.mnvp.np;

import jdk.nashorn.internal.objects.Global;
import ru.serbis.mnvp.debugger.NodeDebugger;
import ru.serbis.mnvp.gateways.Gateway;
import ru.serbis.mnvp.gateways.GatewayConfig;
import ru.serbis.mnvp.gateways.GatewaysController;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.general.NodeVars;
import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.np.translations.PreqTranslation;
import ru.serbis.mnvp.np.translations.Translation;
import ru.serbis.mnvp.structs.general.Packet;
import sun.nio.ch.Net;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Поток реализующий логику обработки входящего пакета
 */
public class PacketReceiverThread implements Runnable, Log {
    /** Метка полного останова потока */
    private boolean stopped = false;
    /** Флаг активности потока */
    private boolean alive = true;
    /** Текстовая метка потока */
    private String label;
    /** Текстовая метка узла */
    private String nodeLabel;

    private LinkedList<Packet> packetsStack;

    private PacketPool packetPool;


    public PacketReceiverThread(String label, String nodeLabel, PacketPool packetPool) {
        this.label = label;
        this.nodeLabel = nodeLabel;
        this.packetPool = packetPool;
        packetsStack = new LinkedList<>();
    }

    @Override
    public void run() {
        log(String.format("<blue>[%s] Запущен поток ресивера пакетов %s<nc>", label, label), 3, nodeLabel);
        while (alive) {

            Packet packet = null;
            try {
                //try {
                    /*NetworkProcessor np = NetworkProcessor.getInstance(nodeLabel);
                    if (np != null) {
                        np.getPacketPoolSemaphore().acquire();
                        packet = np.getNewPacket();
                    } else {
                        log(String.format("<blue>[%s] NP_NULL<nc>", label), 100, nodeLabel);

                    }*/
                packet = packetPool.get();


                //} catch (InterruptedException e) {
                //    e.printStackTrace();
               // }
            } catch (NullPointerException e) {
                System.out.println("AAAAAAAAAA");
                e.printStackTrace();
                //return;
            }

            if (packet != null) {
                processIncomingPacket(packet);

            } else {
                //log(String.format("<blue>[%s] PACKET NULL<nc>", label), 3, nodeLabel);
            }
        }
        log(String.format("<blue>[%s] Остановлен поток ресивера пакетов %s<nc>", label, label), 3, nodeLabel);
        stopped = true;
    }

    public LinkedList<Packet> getPacketsStack() {
        return packetsStack;
    }

    /**
     * Останавливает поток
     */
    public void stop() {
        alive = false;
    }

    public boolean isStopped() {
        return stopped;
    }

    /**
     * Производит обработку входящего пакета данных
     */
    private void processIncomingPacket(Packet packet) {

        //Если узел находится в режиме отладки
        /*if (NodeVars.getInstance(nodeLabel).isDebugMode()) {
            if (!NodeDebugger.getInstance(nodeLabel).intercrptIncomingPacket(packet))
                return;
        }*/

        //Уменьшаем ttl пакета независимо от типа
        packet.setTtl((short) (packet.getTtl() - 1));

        //Заносим новый маршрут в таблицу маршрутизации
        int source = packet.getSource();
        int gateway = GatewaysController.getInstance(nodeLabel).getGatewayNetworkAddress(packet.getGatewayLabel());
        short dest = (short) (packet.getStartTtl() - packet.getTtl());
        //Занести в таблицу маршрутищации все за исключением пакетов источником которых является сам узел
        if (source != NodeVars.getInstance(nodeLabel).getNetworkAddress())
            NetworkProcessor.getInstance(nodeLabel).updateRoute(source, gateway, dest);

        //Определяем являет ли пакет транзитнымтранзитным
        if (packet.getDest() != NodeVars.getInstance(nodeLabel).getNetworkAddress() && packet.getType() != 0) {
            //Если ttl пакета меньше или рано нулу, паке дальше не пройдет
            if (packet.getTtl() <= 0)
                return;

            switch (packet.getType()) {
                case 2: //PREQ
                    processTransitPreqPacket(packet);
                    break;
            }

        } else {
            switch (packet.getType()) {
                case 0: //HELLO
                    processTargetHelloPacket(packet);
                    break;
                case 2: //HELLO
                    processTargetPreqPacket(packet);
                    break;
            }
        }


    }

    /**
     * Процессируют hello сообщения от узла. Задача процессирования состоит
     * в следующем.
     *      -Обновлении флага последней активности шлюза
     *      -Поиске в таблице маршрутизации записи с данным адресом и
     *       добавлении ее туда если она не была найдена.
     */
    private void processTargetHelloPacket(Packet packet) {
        //Обновить метку активности шлюза
        GatewaysController.getInstance(nodeLabel).updateGatewayActivity(packet.getGatewayLabel());
        GatewaysController.getInstance(nodeLabel).setGatewayNetworkAddress(packet.getGatewayLabel(), (int) packet.getSource());

        //Обновить запись в таблице маршрутизации
        NetworkProcessor.getInstance(nodeLabel).updateRoute(packet.getSource(), packet.getSource(), 1);
    }

    private void processTransitPreqPacket(Packet packet) {
        if (packet.getFlags() == 0x00) {
            log(String.format("<blue>[%s] Получен транзитный PREQ пакет от узла %d к %d c ttl <nc>", label, packet.getSource(), packet.getDest(), packet.getTtl()), 3, nodeLabel);

            //int com = Integer.parseInt(new String(packet.getBody()));
            //com++;
            //packet.setBody(String.valueOf(com).getBytes());
            //packet.setLength((short) packet.getBody().length);

            Iterator<Map.Entry<String, Gateway>> iterator = GatewaysController.getInstance(nodeLabel).getGatewaysPoolIterator();

            while (iterator.hasNext()) {
                Gateway gateway = iterator.next().getValue();
                if (!gateway.getLabel().equals(packet.getGatewayLabel())) {
                    try {
                        gateway.getSendSemaphore().acquire();
                        gateway.send(packet);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    log(String.format("[%s] <blue>Отправлен транзитный PREQ пакет к узлу %d через шлюз %s с сетевым адресом %d<nc>", nodeLabel, packet.getDest(), gateway.getLabel(), gateway.getNetworkAddress()), 3, nodeLabel);
                }
            }
        } else {
            log(String.format("<blue>[%s] Получен транзитный PREQ_ACK пакет от узла %d к %d<nc>", label, packet.getSource(), packet.getDest()), 3, nodeLabel);

            NetworkProcessor.getInstance(nodeLabel).sendPacket(packet);
        }


    }

    private void processTargetPreqPacket(Packet packet) {
        if (packet.getFlags() == 0x00) {
            log(String.format("<blue>[%s] Получен целевой PREQ пакет от узла %d<nc>", label, packet.getSource()), 3, nodeLabel);

            int source = packet.getSource();
            int dest = packet.getDest();
            packet.setSource(dest);
            packet.setDest(source);
            packet.setTtl(packet.getStartTtl());
            packet.setFlags((byte) 0x01);

            NetworkProcessor.getInstance(nodeLabel).sendPacket(packet);
            log(String.format("<blue>[%s] Произведен ответ на PREQ пакет от узла %d<nc>", label, packet.getSource()), 3, nodeLabel);
        } else {
            log(String.format("<blue>[%s] Получен целевой PREQ_ACK пакет от узла %d к %d<nc>", label, packet.getSource(), packet.getDest()), 3, nodeLabel);

            Translation translation = NetworkProcessor.getInstance(nodeLabel).getTranslation(packet.getMsgId());

            if (translation == null) {
                log(String.format("<blue>[%s] Невозможно обработать целевой PREQ_ACK пакет от узла %d по причине отутствия трансляции<nc>", label, packet.getDest()), 3, nodeLabel);
                return;
            }

            PreqTranslation preqTranslation = (PreqTranslation) translation;
            preqTranslation.receivePacket(packet, GatewaysController.getInstance(nodeLabel).getGatewayNetworkAddress(packet.getGatewayLabel()));
        }
    }
}
