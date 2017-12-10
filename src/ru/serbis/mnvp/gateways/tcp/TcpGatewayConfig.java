package ru.serbis.mnvp.gateways.tcp;

import ru.serbis.mnvp.gateways.GatewayConfig;

import java.net.Socket;

/**
 * Конфигурация шлюза tcp соединений. Возможно два варианта использованеия
 * данной конфигурации. Первый для порождения нового соединения, второй для
 * оборачивания уже существующего соеденения. Первый как правило используется
 * для создания внешнего подключения при создании узла, а второй генерируется
 * акцепторами. Для понимания какой парметр к какому виду конфигурирования
 * относится, в комментариях к параметрам проставлены метки:
 * G (generated) - подключение
 * O (origin) - оборачивание существующего соединения
 */
public class TcpGatewayConfig extends GatewayConfig {
    /** (O) Сокет соединения*/
    private Socket socket;
    /** (G) Хост для соединения */
    private String host;
    /** (G) Порт для соединения */
    private int port;
    /** (G) Количество попыток при неудачном соединении */
    private int hostConnectRetryCount = 10;

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getHostConnectRetryCount() {
        return hostConnectRetryCount;
    }

    public void setHostConnectRetryCount(int hostConnectRetryCount) {
        this.hostConnectRetryCount = hostConnectRetryCount;
    }
}
