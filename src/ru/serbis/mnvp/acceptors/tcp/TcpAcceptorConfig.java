package ru.serbis.mnvp.acceptors.tcp;

import ru.serbis.mnvp.acceptors.AcceptorConfig;
import ru.serbis.mnvp.mix.Log;

/**
 * Конфигурация акцептора TCP соединений
 */
public class TcpAcceptorConfig extends AcceptorConfig implements Log {
    /** Порт на котором создается серверный сокет */
    private  int port = 5000;
    /** Таймаут входящих подключений, определяет как часто отдельный поток
     * акцептора будет вываливаться из захвата акцепта */
    private int soTimeout = 10000;
    /** Количество потоков акцептора */
    private int threadCount = 1;

    /**
     * Констркутор по умполчанию
     **/
    public TcpAcceptorConfig() {
    }

    /**
     * Констрктор с перечнем параметров 1
     *
     * @param port порт серверного соекта
     * @param soTimeout тамаутакцепта соединения
     * @param threadCount количество потоков захвата
     * @param label текстовая метка
     */
    public TcpAcceptorConfig(int port, int soTimeout, int threadCount, String label) {
        this.port = port;
        this.soTimeout = soTimeout;
        this.threadCount = threadCount;
        super.setLabel(label);
    }

    /**
     * Конструктор копирования
     *
     * @param other экземпляр для копирования
     */
    public TcpAcceptorConfig(TcpAcceptorConfig other) {
        this.port = other.port;
        this.soTimeout = other.soTimeout;
        this.threadCount = other.threadCount;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }





    @Override
    public String toString() {
        return "TcpAcceptorConfig{" +
                "port=" + port +
                ", soTimeout=" + soTimeout +
                ", threadCount=" + threadCount +
                '}';
    }
}
