package ru.serbis.mnvp.ticks;

import ru.serbis.mnvp.gateways.Gateway;
import ru.serbis.mnvp.gateways.GatewaysController;
import ru.serbis.mnvp.general.NodeVars;
import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.mix.NetworkUtils;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.structs.general.Packet;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;

/**
 * Поток реализуеющий пятнадцатисекундное тактирование сети. В его задачу
 * входит:
 *
 * --1. Проверка животси шлюзов с последующими в случае обнаружение мертвого
 *      шлюза:
 *      --1.1 Удаление записи в таблице маршрутизации об узле которому
 *            принадлежал шлюз
 *      --1.2 Удаление шлюза
 * --2. Отсылка hello пакетов оставшимся в живых шлюзам
 */
public class FifteenThread extends TimerTask implements Log, NetworkUtils,PacketUtils {
    /** Текстовая метка*/
    private String label;
    /** Метка узла */
    private String nodeLabel;

    public FifteenThread(String label, String nodeLabel) {
        this.label = label;
        this.nodeLabel = nodeLabel;
    }

    @Override
    public void run() {
        sendHello();
        changeActivity();

    }

    /**
     * Реализует отправку hello сообщений по всейм живым шлюзам
     */
    private void sendHello() {
        Iterator<Map.Entry<String, Gateway>> iterator = GatewaysController.getInstance(nodeLabel).getGatewaysPoolIterator();

        while (iterator.hasNext()) {
            Gateway gateway = iterator.next().getValue();
            log(String.format("<blue>[%s] Отправка hello пакета шлюз %s с адресом %d<nc>", label, gateway.getLabel(), gateway.getNetworkAddress()), 3, nodeLabel);
            Packet helloPacket = createHelloPacket(getNewMsgId(nodeLabel), NodeVars.getInstance(nodeLabel).getNetworkAddress());
            try {
                gateway.getSendSemaphore().acquire();
                gateway.send(helloPacket);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Проверяет метку последней активности шлюза и если шлюз не активен
     * более 15 секунд, удаляет шлюз и очищает таблицу маршрутизации от
     * записей по нему.
     */
    private synchronized void changeActivity() {
        Iterator<Map.Entry<String, Gateway>> iterator = GatewaysController.getInstance(nodeLabel).getGatewaysPoolIterator();

        while (iterator.hasNext()) {
            Gateway gateway = iterator.next().getValue();
            if (gateway.getLastIncomingActivity() < new Date().getTime() - 15000) {
                log(String.format("<blue>[%s] Шлюз %s более не является активным, инициация процедуры удаления шлюза<nc>", label, gateway.getLabel()), 3, nodeLabel);
                GatewaysController.getInstance(nodeLabel).removeGateway(gateway.getLabel());
            }
        }
    }
}
