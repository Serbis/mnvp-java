package ru.serbis.mnvp.gateways.tcp;

import ru.serbis.mnvp.mix.ArrayUtils;
import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.np.NetworkProcessor;
import ru.serbis.mnvp.np.PacketPool;
import ru.serbis.mnvp.structs.general.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Поток обработки входящего потока данных из TCP шлюза
 */
public class TcpGatewayThread implements Runnable, Log, PacketUtils, ArrayUtils {
    /** Метка полного останова потока */
    private boolean stopped = false;
    /** Текстовая метка узла */
    private String nodeLabel;
    /** Текстовая метка */
    private String label;
    /** Текстовая метка шлюза от которого была запущен поток */
    private String gatewayLabel;
    /** Флаг звершения потока */
    private boolean alive = true;
    /** Сокет соединения */
    private Socket socket;
    /** Экземплярр обрабатываемого пакета */
    private Packet packet;
    /** Режим парсинга тела пакета*/
    private boolean bodyMode;
    /** Позиция указателя записи при сборке тела пакета */
    private int bodyPos;
    /** Буфер данных входящего потока*/
    private List<Byte> buffer;

    private PacketPool packetPool;

    /**
     * Конструктор с параметром 1
     *
     * @param socket сокет tcp соединения
     */
    public TcpGatewayThread(Socket socket, String label, String gatewayLabel, String nodeLabel, PacketPool packetPool) {
        this.nodeLabel = nodeLabel;
        this.socket = socket;
        this.label = label;
        this.gatewayLabel = gatewayLabel;
        this.packetPool = packetPool;
    }

    /**
     * Обрабатывает входящий поток данных из TCP сокета, формирует из него
     * представления пакетов и передает на обработку сетевого процессора
     */
    @Override
    public void run() {
        log(String.format("<blue>[%s] Запущен поток обработки входищх данных tcp шлюза<nc>", label), 3, nodeLabel);

        InputStream is = null;
        OutputStream os = null;
        buffer = new ArrayList<>();
        while (alive) {
            try {
                is = socket.getInputStream();
                os = socket.getOutputStream();
                packet = new Packet();
                bodyMode = false;
                bodyPos = 0;

                //System.out.println("---> (" + new Date() + ") --- Вход в режим чтения буфера в потоке захвата с threadId " + threadId);
                for (int b = 0; ((b = is.read()) >= 0);) {
                    //System.out.println(threadId);
                    //System.out.println("0b" + b);
                    buffer.add((byte) b);
                    if (b == 0x3f) { //Если получена метка SN2
                       // System.out.println("Получена метка SN2");
                        if (buffer.size() >= 28) { //Если в буфере есть данные размером с заголовок или более
                            //System.out.println("Буфер имеет размер заголовка или более");
                            if (buffer.get(buffer.size() - 28) == 0x1f) { //Если в буфере на позиции -30 есть метка SN1
                                //System.out.println("Обнаружена метка SN1 на позиции -30");
                                packet = parsePacketHeader(packet, buffer);
                                bodyMode = true;
                                bodyPos = 0;
                                buffer.clear();
                            } else {
                                completeBody();
                            }
                        } else{
                            completeBody();
                        }
                    } else {
                        completeBody();
                    }
                }

                //System.out.println("---> (" + new Date() + ") --- Выход из в режима чтения буфера в потоке захвата с threadId " + threadId);
            } catch (SocketTimeoutException e) {
                //System.out.println("CAAAAAAAAAAAAAAAAAAAAATCH = 1");
                if (e.getMessage().contains("Read")) {
                    //log.debug(3, "0:99", "Таймаут ожидания данных потока обработки соединения. ID -> " + socket.hashCode(), "SocketReaderThread:/run");
                    //alive = false;
                } else {
                    //log.debug(3, "0:99", "Неизвестное исключение потока обработки соединения 1. ID -> " + socket.hashCode(), "SocketReaderThread:/run", e);
                    //alive = false;
                }
            } catch (IOException e) {
               // System.out.println("CAAAAAAAAAAAAAAAAAAAAATCH = 2");
                //log.debug(3, "0:99", "Неизвестное исключение потока обработки соединения 2. ID -> " + socket.hashCode(), "SocketReaderThread:/run", e);
                alive = false;
            }
        }

        try {
            is.close();
            os.close();
        } catch (Exception ignored) {}
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        stopped = true;
        log(String.format("<blue>[%s] Остановлен поток обработки входищх данных tcp шлюза<nc>", label), 3, nodeLabel);
    }

    /**
     * Ставит метку останова потока
     */
    public void stop() {
        alive = false;
    }

    public boolean isStopped() {
        return stopped;
    }

    private void completeBody() {
        if (bodyMode) {
            bodyPos++;
            if (bodyPos >= packet.getLength()) {
                bodyMode = false;
                packet.setBody(byteListToArray(buffer));
                packet.setGatewayLabel(gatewayLabel);
                packet.setTs((int) (Math.random() * 1000000));
                buffer.clear();
                bodyPos = 0;

                if (packet.getTtl() == 6 && nodeLabel.equals("node_B")) {
                    System.out.println("x");
                }
                if (packet.getTtl() == 5 && nodeLabel.equals("node_D")) {
                    System.out.println("x");
                }
                if (packet.getTtl() == 4 && nodeLabel.equals("node_H")) {
                    System.out.println("x");
                }
                if (packet.getTtl() == 3 && nodeLabel.equals("node_N")) {
                    System.out.println("x");
                }
                if (packet.getTtl() == 2 && nodeLabel.equals("node_R")) {
                    System.out.println("x");
                }
                log(String.format("<green>[%s->%s] Получен пакет данных через TCP шлюз -> %s<nc>", nodeLabel, label, packet.toString()), 3, nodeLabel);
                Packet newPacket = new Packet(packet);
                packetPool.put(newPacket);
                //try {
                    //NetworkProcessor.getInstance(nodeLabel).getPacketPoolSemaphore().acquire();
                   // NetworkProcessor.getInstance(nodeLabel).receivePacket(packet);
                //} catch (InterruptedException e) {
                //    e.printStackTrace();
               // }
            }
        }
    }

}