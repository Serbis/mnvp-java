package ru.serbis.mnvp.general;

import ru.serbis.mnvp.acceptors.Acceptor;
import ru.serbis.mnvp.acceptors.AcceptorConfig;
import ru.serbis.mnvp.acceptors.tcp.TcpAcceptor;
import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.mix.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Класс окружения узла. В нем глобальные переменные узла.
 *
 * Данный объект является пулом синглетонов с номерной регистрацией
 *
 */
public class NodeVars implements Log {
    /** Сетевой адрес узла */
    private int networkAddress;
    /** Счетчик номеров сообщений */
    private int msgCounter;
    /** Флаг отладочного режима узла */
    private boolean debugMode;


    private static Map<String, NodeVars> instancesPool = new HashMap<>();

    /**
     * Регестрирует новый синглетон с заданным номером
     *
     * @param label метка синглетона
     */
    public static void registerInstance(String label) {
        instancesPool.put(label, new NodeVars());
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
    public static NodeVars getInstance(String label) {
        return instancesPool.get(label);
    }

    public int getNetworkAddress() {
        return networkAddress;
    }

    public void setNetworkAddress(int networkAddress) {
        this.networkAddress = networkAddress;
    }

    public int getMsgCounter() {
        return msgCounter;
    }

    public void setMsgCounter(int msgCounter) {
        this.msgCounter = msgCounter;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
}
