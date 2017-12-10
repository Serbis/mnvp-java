package ru.serbis.mnvp.np.translations;

import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.np.NetworkProcessor;
import ru.serbis.mnvp.structs.general.Packet;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Описывает трансляцию эхо запроса
 */
public class EchoTranslation extends Translation implements Runnable, Log {
    /**
     * Обратный вызов совершаемый при завершении трансляции. Служит для
     * уведомления вызывающей стороны о результатах выполенния трансляции.
     */
    public interface TranslationFinisher {
        void finish(Result result);
    }

    /**
     * Обратный вызов совершаемый при завершении трансляции. Служит для
     * уведомления сетевого процессора о том, что трансляцию можно удалить
     * из пула трансляций.
     */
    public interface TranslationCleaner {
        void clean(int translationId);
    }

    /** Метка узла */
    private String nodeLabel;
    /** Идентификатор трансляции */
    private int id;
    /** Коллбэк завершения трансляции */
    private TranslationFinisher translationFinisher;
    /** Коллбэк завершения трансляции для сетевого процессора */
    private TranslationCleaner translationCleaner;
    /** Статус трансляции*/
    private State state = State.NOT_STATED;
    /** Результат выполнения трансляции */
    private Result result = null;
    /** Стартовый пакет трансляции */
    private Packet startPacket;
    /** Метка времени начала операции */
    private long opTime;
    /** Результат выполнения PREQ запроса, если он имел место быть */
    private PreqTranslation.Result preqResult = null;
    /** Счетчик ошибко NETWORK_ERROR:0*/
    private int netErrorCouter = 0;

    /**
     * Конструктор трансляции
     *
     * @param startPacket стартовый пакет, который будет направлен целевому
     *                    узлу
     * @param translationFinisher обртные вызов по завершении трасляции
     */
    public EchoTranslation(Packet startPacket, String nodeLabel, TranslationFinisher translationFinisher, TranslationCleaner cleaner) {
        this.translationFinisher = translationFinisher;
        this.translationCleaner = cleaner;
        this.startPacket = startPacket;
        this.nodeLabel = nodeLabel;
    }

    @Override
    public void run() {
        switch (state) {
            //Транслия еще не начата
            case NOT_STATED:
                sendStartPacket();
                break;
            //Ожидание завершения динамической маршрутизации
            case WAIT_PREQ_FINISH:
                checkPreqFinish();
                break;
            //Ожидание ответного пакета
            case WAIT_ACKS:
                waitProcess();
                break;
            case FINISH:
                finish();
                break;
        }
    }

    /**
     * Производит запус трансляции
     */
    public void start() {
        log(String.format("[%s->echoTranslation_%d] <blue>Запущена ECHO трансляция к узлу %d<nc>", super.getNodeLabel(), super.getId(), startPacket.getDest()), 3, super.getNodeLabel());
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Выполняет отправку стартового пакета
     */
    private void sendStartPacket() {
        //Выполнить отправку пакета
        NetworkProcessor.PacketSendResult rs = NetworkProcessor.getInstance(nodeLabel).sendPacket(startPacket);
        //Если пакет был корректно отправлен
        if (rs == NetworkProcessor.PacketSendResult.OK) {
            log(String.format("[%s->echoTranslation_%d] <blue>Отправлен ECHO пакет к узлу %d<nc>", super.getNodeLabel(), super.getId(), startPacket.getDest()), 3, super.getNodeLabel());
            //Устновить статус FP_SEND
            state = State.WAIT_ACKS;
            //Установить метку времени отправки пакета в сеть
            opTime = new Date().getTime();

        //При отправке пакета, не был найден маршрут по таблице маршрутизации
        } else if (rs == NetworkProcessor.PacketSendResult.ROUTE_NOT_FOUND) {
            log(String.format("[%s->echoTranslation_%d] <blue>При отправке ECHO пакет к узлу %d не удалось найти маршрут<nc>", super.getNodeLabel(), super.getId(), startPacket.getDest()), 3, super.getNodeLabel());
            //Если preq ранее не выполнялся, запустить динамическую маршрутизацию
            if (preqResult != null) {
                state = State.WAIT_PREQ_FINISH;
                runPreq();
            //Если же preq уже ранее был выполнен, значит ошибка поиска маршрута
            } else {
                result = Result.ROUTE_NOT_FOUND;
                state = State.FINISH;
            }
        //При попытке отправки покета произошла внутренняя ошибка
        } else if (rs == NetworkProcessor.PacketSendResult.INTERNAL_ERROR) {
            log(String.format("[%s->echoTranslation_%d] <blue>При отправке ECHO пакет к узлу %d возникла внутренняя ошибка<nc>", super.getNodeLabel(), super.getId(), startPacket.getDest()), 3, super.getNodeLabel());
            result = Result.INTERNAL_ERROR;
            state = State.FINISH;
        }
    }

    /**
     * Запуска динамический поиск маршрута
     */
    private void runPreq() {
        log(String.format("[%s->echoTranslation_%d] <blue>Запущена динамическая маршрутизация к узлу %d<nc>", super.getNodeLabel(), super.getId(), startPacket.getDest()), 3, super.getNodeLabel());
        NetworkProcessor.getInstance(nodeLabel).sendPreqRequest(startPacket.getDest(), result -> preqResult = result);
    }

    /**
     * Проверяет завершился ли динамический поиск маршрута, и в зависимости от
     * резульата либо отпавляет пакет, либо устанавливает результат трансляции
     * в ROUTE_NOT_FOUND
     */
    private void checkPreqFinish() {
        if (preqResult == null)
            return;

        switch (preqResult) {
            case FOUND:
                log(String.format("[%s->echoTranslation_%d] <blue>Динамичкая маршрутизация к узлу %d успешно зевершена<nc>", super.getNodeLabel(), super.getId(), startPacket.getDest()), 3, super.getNodeLabel());
                sendStartPacket();

                break;
            case NOT_FOUND:
                log(String.format("[%s->echoTranslation_%d] <blue>Динамичкая маршрутизация к узлу %d завершилась неудачей<nc>", super.getNodeLabel(), super.getId(), startPacket.getDest()), 3, super.getNodeLabel());
                result = Result.ROUTE_NOT_FOUND;
                state = State.FINISH;

                break;
            default:
                log(String.format("[%s->echoTranslation_%d] <blue>Динамичкая маршрутизация к узлу %d завершилась внутренней ошибкой<nc>", super.getNodeLabel(), super.getId(), startPacket.getDest()), 3, super.getNodeLabel());
                result = Result.INTERNAL_ERROR;
                state = State.FINISH;

                break;
        }
    }

    /**
     * Обрабатывает ожидание входящего пакета
     */
    private void waitProcess() {
        if (opTime < new Date().getTime() - 5000) {
            log(String.format("[%s->echoTranslation_%d] <blue>Таймаут ожадиня ответного ECHO пакета от узла %d<nc>", super.getNodeLabel(), super.getId(), startPacket.getDest()), 3, super.getNodeLabel());
            result = Result.TIMEOUT;
            state = State.FINISH;
        }
    }

    /**
     * Обрабатывает входящий пакет. После начала трансляции, любой входящий
     * пакет, с MSGID равным идентификатору трансляции, поступает в данный
     * обработчик. Последний выполняет логику смены статусов трансляции и
     * в случае необходимости отправку допольнительных пакетов.
     *
     * @param packet входящий пакет
     */
    public void processIncomingPacket(Packet packet) {
        switch (packet.getType()) {
            case 4: //ECHO
                //Если флаг ack установлен
                if (packet.getFlags() == 0x01) {
                    log(String.format("[%s->echoTranslation_%d] <blue>Трансляцией получен ответный ECHO пакет<nc>", super.getNodeLabel(), super.getId()), 3, super.getNodeLabel());
                    result = Result.OK;
                    state = State.FINISH;
                } else {
                    log(String.format("[%s->echoTranslation_%d] <blue>Трансляцией получен неожиданный пакет -> %s<nc>", super.getNodeLabel(), super.getId(), packet), 3, super.getNodeLabel());
                }

                break;
            case 1: //NETWORK_ERROR
                ByteBuffer bf = ByteBuffer.allocate(4);
                bf.put(packet.getBody());

                int errCode = bf.getInt();

                if (errCode == 0) {
                    log(String.format("[%s->echoTranslation_%d] <blue>Трансляцией получен пакет с NETWORK_ERROR:0 %d<nc>", super.getNodeLabel(), super.getId(), errCode), 3, super.getNodeLabel());
                    netErrorCouter++;
                    if (netErrorCouter >= 3) {
                        log(String.format("[%s->echoTranslation_%d] <blue>Обнаружена сетевая аномалия. При успешных процедурах динмаической маршрутизации, сеть более трех раз вернула ошибку NETWORK_ERROR:0<nc>", super.getNodeLabel(), super.getId()), 3, super.getNodeLabel());
                        result = Result.UNKNOWN_NETWORK_ERROR;
                        state = State.FINISH;
                    } else {
                        preqResult = null;
                        state = State.WAIT_PREQ_FINISH;
                        runPreq();
                    }
                } else {
                    log(String.format("[%s->echoTranslation_%d] <blue>Трансляцией получен пакет с NETWORK_ERROR с ножиданным кодом ожибки %d<nc>", super.getNodeLabel(), super.getId(), errCode), 3, super.getNodeLabel());
                    result = Result.UNKNOWN_NETWORK_ERROR;
                    state = State.FINISH;
                }

                break;
            default:
                log(String.format("[%s->echoTranslation_%d] <blue>Трансляцией получен неожиданный пакет -> %s<nc>", super.getNodeLabel(), super.getId(), packet), 3, super.getNodeLabel());

                break;
        }

    }

    /**
     * Обрабатывает завершене трансляции. Совершает обратный вызов финишера,
     * после чего завершает поток трансляции.
     */
    private void finish() {
        if (translationFinisher != null) {
            translationFinisher.finish(result);
        }

        if (translationCleaner != null) {
            translationCleaner.clean(super.getId());
        }

        super.setAlive(false);
        log(String.format("[%s->echoTranslation_%d] <blue>Звершена ECHO трансляция к узлу %d с результатом - %s<nc>", super.getNodeLabel(), super.getId(), startPacket.getDest(), result), 3, super.getNodeLabel());
    }

    /** Текущий этап трансляции */
    public enum State {
        NOT_STATED, WAIT_ACKS, WAIT_PREQ_FINISH, FINISH
    }

    public enum Result {
        OK, TIMEOUT, ROUTE_NOT_FOUND, INTERNAL_ERROR, UNKNOWN_NETWORK_ERROR
    }

}
