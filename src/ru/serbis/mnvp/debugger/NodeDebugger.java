package ru.serbis.mnvp.debugger;

import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.np.NetworkProcessor;
import ru.serbis.mnvp.np.translations.PreqTranslation;
import ru.serbis.mnvp.structs.general.Packet;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер отладчика узла
 *
 * Данный объект является пулом синглетонов с номерной регистрацией
 *
 */
public class NodeDebugger implements Log {
    /** Метка узла */
    private String nodeLabel;
    /** Форметтер даты */
    private SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss dd.MM.yyyy");
    /** Перехватчик пакетов */
    private IncomingPacketInterceptor incomingPacketInterceptor;

    private static Map<String, NodeDebugger> instancesPool = new HashMap<>();

    /**
     * Регестрирует новый синглетон с заданным номером
     *
     * @param label метка синглетона
     */
    public static void registerInstance(String label) {
        instancesPool.put(label, new NodeDebugger());
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
    public static NodeDebugger getInstance(String label) {
        return instancesPool.get(label);
    }

    public void setConfig(NodeDebuggerConfig config) {
        incomingPacketInterceptor = config.getIncomingPacketInterceptor();
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public boolean intercrptIncomingPacket(Packet packet) {
        return incomingPacketInterceptor == null || incomingPacketInterceptor.intercept(packet);
    }

    /**
     * Дубликатор метода sendPreqRequest из сетевого процессора. Позволяет
     * врнучную инициировать произвольный preq запрос
     *
     * @param dest сетевой адрес искомого узла
     * @param finisherCallback обртный вызов, который будет совершен после
     *                         заврешния трансляции
     */
    public void sendPreqRequest(int dest, PreqTranslation.TranslationFinisher finisherCallback) {
        NetworkProcessor.getInstance(nodeLabel).sendPreqRequest(dest, finisherCallback);
    }
}
