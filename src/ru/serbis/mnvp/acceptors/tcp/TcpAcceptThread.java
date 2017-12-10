package ru.serbis.mnvp.acceptors.tcp;

import ru.serbis.mnvp.gateways.GatewaysController;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.mix.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Поток акцептора. Запускается как в единственном так и во множественном
 * экземпляре. Задачей потока является прослушивание серверного соеката
 * в режиме блокировк в ожидании входящего подключения. Если последнее
 * имеет место быть, порождается и регистрируется новый tcp шлюз, после
 * чего поток переходит в свое естестенное состоянии блокирующего акцепта.
 */
public class TcpAcceptThread implements Runnable, Log {
    /** Метка полного останова потока */
    private boolean stopped = false;
    /** Флаг активности поток */
    private boolean alive = true;
    /** Серверный сокет */
    private ServerSocket serverSocket;
    /** Метка потока */
    private String label;
    /** Метка узла*/
    private String nodeLabel;


    /**
     * Назначает потоку серверный сокет и метку
     *
     * @param serverSocket серверный сокет
     * @param label метка потока
     */
    public void init(ServerSocket serverSocket, String label, String nodeLabel) {
        this.nodeLabel = nodeLabel;
        this.label = label;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        log(String.format("<blue>[%s] Запущен поток ацептора tcp соединений %s<nc>", label, label), 3, nodeLabel);
        while (alive) {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(10000);
                log(String.format("<lblue>TcpAcceptThread -> %s ACCEPT<nc>", label), 10, nodeLabel);
                TcpGatewayConfig tcpGatewayConfig = new TcpGatewayConfig();
                tcpGatewayConfig.setSocket(socket);
                GatewaysController.getInstance(nodeLabel).wrapGateway(tcpGatewayConfig);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        log(String.format("<blue>[%s] Остановлен поток ацептора tcp соединений %s<nc>", label, label), 3, nodeLabel);
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }


    /**
     * Устанавливает флаг завершения потока
     */
    public void stop() {
        alive = false;
    }
}
