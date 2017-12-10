package ru.serbis.mnvp.acceptors.tcp;

import ru.serbis.mnvp.acceptors.Acceptor;
import ru.serbis.mnvp.acceptors.AcceptorConfig;
import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.np.PacketPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Реализация акцептора TCP соединенй. Данный акцептор создает верверный сокет,
 * после чего создает группу потоков (по умолчаню один) которые начинают слушать
 * входящие подключения. Обнаружив последнее, поток порождает новый шлюз,
 * закрепляет его в системе шлюзования узла и вновь переходит в режим ожидания
 * новых соединений.
 */
public class TcpAcceptor extends Acceptor implements Log {
    /** Список запущенных потоков акцептора */
    private List<TcpAcceptThread> acceptThreadList = new ArrayList<>();
    /** Пул потоков ацепта входящих соединений */
    private ThreadPoolExecutor executor;
    /** Серверный соке */
    private ServerSocket serverSocket;

    /**
     * Конструктор суперкласса с параметром конфигурации акцепторп
     *
     * @param config конфигурация акцептора
     */
    public TcpAcceptor(AcceptorConfig config) {
        super(config);
    }

    /**
     * Выполяет запуск акцептора. Создает серверный сокет и запускает
     * группу потоков для получения входящих соединений.
     */
    @Override
    public void run() {
        if (super.getConfig() == null) {
            log(String.format("<yellow>[%s] Ошибка запуска ацептора. Не задана конфигурация", super.getNodeLabel()), 2, super.getNodeLabel());

            return;
        }

        TcpAcceptorConfig config = (TcpAcceptorConfig) super.getConfig();
        super.setLabel(config.getLabel());

        executor = new ThreadPoolExecutor(config.getThreadCount(), config.getThreadCount(), 10,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());


        try {
            serverSocket = new ServerSocket(config.getPort());
            serverSocket.setSoTimeout(config.getSoTimeout());
        } catch (IOException e) {
            log(String.format("<yellow>[%s->%s] Ошибка при создание ацептора TCP соединений. Невозможно создать серверный сокет", super.getNodeLabel(), super.getConfig().getLabel()), 2, super.getNodeLabel());
            e.printStackTrace();
        }

        for (int i = 0; i < config.getThreadCount(); i++) {
            TcpAcceptThread tcpAcceptThread = new TcpAcceptThread();
            tcpAcceptThread.init(serverSocket, String.format("%s->%s->AcceptThread_%d", super.getNodeLabel(), super.getLabel(), i), super.getNodeLabel());
            acceptThreadList.add(tcpAcceptThread);
            executor.execute(tcpAcceptThread);
        }
    }

    /**
     * Выполняет остановку акцептора. Останавливает группу потоков захвата
     * новых соединений.
     */
    @Override
    public void stop() {
        acceptThreadList.forEach(TcpAcceptThread::stop);
        acceptThreadList.forEach(tcpAcceptThread -> {
            while (!tcpAcceptThread.isStopped()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
