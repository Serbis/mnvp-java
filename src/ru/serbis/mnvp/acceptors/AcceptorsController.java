package ru.serbis.mnvp.acceptors;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptor;
import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.np.PacketPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Контроллер акцепторов. Задачей данного класса, явялется создание,
 * содержание и управление всему существующими у узла акцепторами. Любой
 * акцетор, порождается, управляется и уничтожается тольк в этом классе.
 *
 * Данный объект является пулом синглетонов с номерной регистрацией
 *
 */
public class AcceptorsController implements Log {
    /** Метка узла */
    String nodeLabel;
    /** Пул акцепторов */
    Map<String, Acceptor> acceptorsPool = new HashMap<>();
    /** Пул синглетонов */
    private static Map<String, AcceptorsController> instancesPool = new HashMap<>();


    /**
     * Регестрирует новый синглетон с заданным номером
     *
     * @param label метка синглетона
     */
    public static void registerInstance(String label) {
        instancesPool.put(label, new AcceptorsController());
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
    public static AcceptorsController getInstance(String label) {
        return instancesPool.get(label);
    }


    /**
     * Создает новый акцтопр из входящей конфигураци. Размещает его в пуле
     * и запускает.
     *
     * @param config конфигурация создаваемого акцептора
     */
    public void createAcceptor(AcceptorConfig config) {
        if (config instanceof TcpAcceptorConfig) {
            TcpAcceptorConfig tcpAcceptorConfig = (TcpAcceptorConfig) config;

            if (tcpAcceptorConfig.getLabel() == null)
                tcpAcceptorConfig.setLabel(String.format("TpcAcceptor_%d", ThreadLocalRandom.current().nextInt(10000, 20000)));


            TcpAcceptor tcpAcceptor = new TcpAcceptor(tcpAcceptorConfig);
            tcpAcceptor.setNodeLabel(nodeLabel);
            acceptorsPool.put(tcpAcceptorConfig.getLabel(),tcpAcceptor); //Внести акцептор в пул акцепторов
            log(String.format("<blue>[%s] Создан новый акцетор TCP с меткой %s<nc>", nodeLabel, config.getLabel()), 3, nodeLabel);
            tcpAcceptor.run();
        }
    }

    public void stopAllAcceptors() {
        acceptorsPool.entrySet().forEach(stringAcceptorEntry -> stringAcceptorEntry.getValue().stop());
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

}
