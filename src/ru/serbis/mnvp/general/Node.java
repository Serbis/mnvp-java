package ru.serbis.mnvp.general;

import ru.serbis.mnvp.acceptors.AcceptorConfig;
import ru.serbis.mnvp.acceptors.AcceptorsController;
import ru.serbis.mnvp.debugger.NodeDebugger;
import ru.serbis.mnvp.exceptions.MnvpInitializeException;
import ru.serbis.mnvp.gateways.Gateway;
import ru.serbis.mnvp.gateways.GatewayConfig;
import ru.serbis.mnvp.gateways.GatewaysController;
import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.np.NetworkProcessor;
import ru.serbis.mnvp.np.PacketPool;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.ticks.FifteenThread;

import java.io.File;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Класс реализующий точку входы функциональности сервера. Запускает узел,
 * с заданной кофигурацией.
 *
 * ----------Основные определения--------------
 *
 * ---Объект Node - главный объект запускающий узел в работу. Главная точка
 *              входа в программу узла со стороны прикладного кода.
 *
 * ---Конфигурация узла - объект содержащий исчерпывающую конфигурацию,
 *              необходимую для жизненного цикла узла
 *
 * ---Акцетор - модуль принимающий запросы на входщие соединения. Например
 *              акцет сервеного сокета. Разница с последним заключена в том,
 *              что актцетор как правило создает пул потоков для данной задачи
 *              и выполняет побочные операции, вроде котроля состояния
 *              активности работающих поток, раширения пула и т. п.
 *
 * ---Конфигурация акцетора - класс который описывает все требуемые параметры
 *              для создания конкретного вида параметра
 *
 * ---Контроллер акцепторов - модуль занимаюйся проведением полного жизненного
 *              цикла акцетора. В нем создаеются, управялется и удаляется любые
 *              акцепторы узла. Основное его назначение, инкапсуляциия логики
 *              работы последних.
 */
public class Node implements Log {

    /** Конфигурация узла */
    private NodeConfig config;
    /** Уникальный идентификатор объекта узла (программного объекта а не сетевого узла) */
    private int nodeId;
    /** Тикер сети */
    private  Timer fifteenTimer;

    /**
     * Конструктор по умолчанию
     */
    public Node() {
    }

    /**
     * Конструктор с параметром конфигурации
     *
     * @param config конфигурация узла
     */
    public Node(NodeConfig config) {
        this.config = config;
    }

    /**
     * Устанавливает конфигурацию узла. данный метод должен быть вызавн до
     * запуска узла.
     *
     * @param config конфигурация узла
     */
    public void setConfing(NodeConfig config) {
        this.config = config;
    }


    public void start() {
        //Если не задан конфиг, то это ошибка инициализации узла
        if (config == null) {
            System.out.println("Критическая ошибка при запуске узла. Не задана основная конфигурация");
            throw new MnvpInitializeException("Критическая ошибка при запуске узла. Не задана основная конфигурация");
        }

        //Инициализация логгирвоания
        LogsController.registerInstance(config.getLabel());
        if (config.getLogFilePath() != null)
            LogsController.getInstance(config.getLabel()).setLogFile(new File(config.getLogFilePath()));
        LogsController.getInstance(config.getLabel()).setLogLevel(config.getLogLevel());

        log("-----------------------------------------------------------------------------------------------------\n\n", 0, config.getLabel());

        if (config.getLabel() == null)
            config.setLabel(String.format("Node_%d", ThreadLocalRandom.current().nextInt(10000, 20000)));

        if (config.getNetworkAddress() <= 0) {
            log("<red>Критическая ошибка при запуске узла. Не задан адрес узла или он имеет отрицательное значение", 0, config.getLabel());
            throw new MnvpInitializeException("Критическая ошибка при запуске узла. Не задан адрес узла или он имеет отрицательное значение");
        }

        //Создание объекта глабальных переменных
        NodeVars.registerInstance(config.getLabel());
        NodeVars.getInstance(config.getLabel()).setNetworkAddress(config.getNetworkAddress());

        //Определение отладочного режима
        if (config.isDebugMode())
            NodeVars.getInstance(config.getLabel()).setDebugMode(config.isDebugMode());

        NodeDebugger.registerInstance(config.getLabel());
        if (config.getNodeDebuggerConfig() != null)
            NodeDebugger.getInstance(config.getLabel()).setConfig(config.getNodeDebuggerConfig());
        NodeDebugger.getInstance(config.getLabel()).setNodeLabel(config.getLabel());


        PacketPool packetPool = new PacketPool(config.getLabel());
        AcceptorsController.registerInstance(config.getLabel());
        AcceptorsController.getInstance(config.getLabel()).setNodeLabel(config.getLabel());

        GatewaysController.registerInstance(config.getLabel());
        GatewaysController.getInstance(config.getLabel()).setNodeLabel(config.getLabel());
        GatewaysController.getInstance(config.getLabel()).setPacketPool(packetPool);



        //Запуск сетевого процессора
        NetworkProcessor.registerInstance(config.getLabel());
        NetworkProcessor.getInstance(config.getLabel()).setLabel(String.format("NetworkProcessor_%s", ThreadLocalRandom.current().nextInt(10000, 20000)));
        NetworkProcessor.getInstance(config.getLabel()).setNodeLabel(config.getLabel());
        NetworkProcessor.getInstance(config.getLabel()).setPacketPool(packetPool);
        NetworkProcessor.getInstance(config.getLabel()).run(config.getNetworkProcessorConfig());


        //Запуск акцепторов
        for (AcceptorConfig ac: config.getAcceptorConfigs()) {
            AcceptorsController.getInstance(config.getLabel()).createAcceptor(ac);
        }

        //Запуск шлюзов
        for (GatewayConfig gc: config.getGatewayConfigs()) {
            GatewaysController.getInstance(config.getLabel()).generateGateway(gc);
        }

        //Запуск тикера сети
        FifteenThread fifteenThread = new FifteenThread(String.format("%s->FifteenThread_%s", config.getLabel(), ThreadLocalRandom.current().nextInt(10000, 20000)), config.getLabel());
        fifteenTimer = new Timer(false);
        fifteenTimer.scheduleAtFixedRate(fifteenThread, 0, 15000);
    }

    /**
     * Производит остановку узла
     */
    public void stop() {
        //Остановить тикер
        fifteenTimer.cancel();

        //Остановить шлюзы
        GatewaysController.getInstance(config.getLabel()).stopAllGateways();

        //Основить акцепторы
        AcceptorsController.getInstance(config.getLabel()).stopAllAcceptors();

        //Останновить сетевой процессор
        NetworkProcessor.getInstance(config.getLabel()).stop();

        //Снять регистрацию объекта глобальных переменных
        NodeVars.unregisterInstance(config.getLabel());

        //Снаять регистрацию контроллера шлюзов
        GatewaysController.unregisterInstance(config.getLabel());

        //Снять регистрацию контроллера акцептора
        AcceptorsController.unregisterInstance(config.getLabel());

        log(String.format("<lblue>[%s] Узел успешно остановлен", config.getLabel()), 3, config.getLabel());
        LogsController.unregisterInstance(config.getLabel());
    }

    /**
     * Возвращает отладчик узла
     *
     * @return объект отладчика
     */
    public NodeDebugger getDebagger() {
        return NodeDebugger.getInstance(config.getLabel());
    }
}
