package ru.serbis.mnvp.gateways.tcp;

import ru.serbis.mnvp.gateways.Gateway;
import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.np.PacketPool;
import ru.serbis.mnvp.structs.general.Packet;

import java.io.IOException;
import java.net.Socket;

/**
 *  Шлюз tcp соединения
 */
public class TcpGateway extends Gateway implements Log, PacketUtils {
    /** Сокет соединения */
    private Socket socket;
    /** Поток обработки входящих данных */
    private TcpGatewayThread tcpGatewayThread;



    public TcpGateway(Socket socket, String label, PacketPool packetPool) {
        super.setLabel(label);
        this.socket = socket;
        super.setPacketPool(packetPool);
    }

    /**
     * Запускает процесс обработки данных входящего потока
     */
    @Override
    public void run() {
        tcpGatewayThread = new TcpGatewayThread(socket, String.format("%s->%s->%s", super.getNodeLabel(),  super.getLabel(), "GatewayThread"), super.getLabel(), super.getNodeLabel(), super.getPacketPool());
        Thread thread = new Thread(tcpGatewayThread);
        thread.start();
    }

    /**
     * Останавливает поток шлюза тем самым реализую процедуру закрытия
     * последнего
     */
    @Override
    public void stop() {
        tcpGatewayThread.stop();
        while (!tcpGatewayThread.isStopped()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log(String.format("<blue>[%s->%s] Остановлен TCP шлюз %s<nc>", super.getNodeLabel(), super.getLabel(), super.getLabel()), 3, super.getNodeLabel());
    }

    /**
     * Выполяняет отправку пакета в шлюз. В задачу метода входит преобразование
     * пакета в бинарное представление, проверка работоспособности выходного
     * потока данных TCP сокета и отправку в него данных.
     *
     * @param packet пакет
     * @return успеность выполнения операции
     */
    @Override
    public boolean send(Packet packet) {
        if (socket == null) {
            log(String.format("<red>[%s->%s] Ошика отправки пакета в TCP шлюз, socket == null -> %s<nc>", super.getNodeLabel(), super.getLabel(), packet.toString()), 1, super.getNodeLabel());
            return false;
        }

        if (socket.isClosed()) {
            log(String.format("<red>[%s->%s] Ошика отправки пакета в TCP шлюз, сокет закрыт -> %s<nc>", super.getNodeLabel(), super.getLabel(), packet.toString()), 1, super.getNodeLabel());
            return false;
        }

        try {
            socket.getOutputStream().write(packetToByteArray(packet));
        } catch (IOException e) {
            log(String.format("<red>[%s->%s] Ошика отправки пакета в TCP шлюз, ошибка ввода-вывода -> %s<nc>", super.getNodeLabel(), super.getLabel(), packet.toString()), 1, super.getNodeLabel());
            e.printStackTrace();
        }

        super.getSendSemaphore().release();

        return true;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}