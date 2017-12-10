package ru.serbis.mnvp.mix;

import ru.serbis.mnvp.general.NodeVars;
import ru.serbis.mnvp.structs.general.Packet;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Утилитные методы для работы с пакетами
 */
public interface PacketUtils {

    /**
     * Производит парсинг заголовка пакета буфера данных. Условием работы
     * данного является то, что последний байт в буфере должен являть меткой
     * SN2 а первый меткой SN1.
     *
     * @param packet пакет в который будет вставлен заголовк
     * @param buffer буфер с сырыми данными
     * @return модифициарванный пакет
     */
    default Packet parsePacketHeader(Packet packet, List<Byte> buffer) {
        int bs = buffer.size();

        byte[] arr = { buffer.get(bs - 27), buffer.get(bs - 26), buffer.get(bs - 25), buffer.get(bs - 24) };
        ByteBuffer wrapped = ByteBuffer.wrap(arr);
        packet.setVersion(wrapped.getInt());

        arr = new byte[] { buffer.get(bs - 23), buffer.get(bs - 22), buffer.get(bs - 21), buffer.get(bs - 20) };
        wrapped = ByteBuffer.wrap(arr);
        packet.setMsgId(wrapped.getInt());

        packet.setType(buffer.get(bs - 19));

        arr = new byte[] {buffer.get(bs - 18), buffer.get(bs - 17)};
        wrapped = ByteBuffer.wrap(arr);
        packet.setSeq(wrapped.getShort());

        arr = new byte[] {buffer.get(bs - 16), buffer.get(bs - 15), buffer.get(bs - 14), buffer.get(bs - 13)};
        wrapped = ByteBuffer.wrap(arr);
        packet.setSource(wrapped.getInt());

        arr = new byte[] {buffer.get(bs - 12), buffer.get(bs - 11), buffer.get(bs - 10), buffer.get(bs - 9)};
        wrapped = ByteBuffer.wrap(arr);
        packet.setDest(wrapped.getInt());

        arr = new byte[] {buffer.get(bs - 8), buffer.get(bs - 7)};
        wrapped = ByteBuffer.wrap(arr);
        packet.setTtl(wrapped.getShort());

        arr = new byte[] {buffer.get(bs - 6), buffer.get(bs - 5)};
        wrapped = ByteBuffer.wrap(arr);
        packet.setStartTtl(wrapped.getShort());

        packet.setFlags(buffer.get(bs - 4));

        arr = new byte[] {buffer.get(bs - 3), buffer.get(bs - 2)};
        wrapped = ByteBuffer.wrap(arr);
        packet.setLength(wrapped.getShort());

        return packet;
    }

    /**
     * Создает бинарное представление пакета
     *
     * @param packet пакет
     * @return бинарный массив
     */
    default byte[] packetToByteArray(Packet packet) {
        byte bin[] = new byte[29 + packet.getBody().length - 1]; //Сериализовать пакет в бинарное представление
        ByteBuffer buffer = ByteBuffer.allocate(4);

        bin[0] = 0x1f;  //SM1

        buffer.putInt(packet.getVersion()); //VERSION
        bin[1] = buffer.get(0);
        bin[2] = buffer.get(1);
        bin[3] = buffer.get(2);
        bin[4] = buffer.get(3);

        buffer = ByteBuffer.allocate(4);
        buffer.putInt(packet.getMsgId()); //MSGID
        bin[5] = buffer.get(0);
        bin[6] = buffer.get(1);
        bin[7] = buffer.get(2);
        bin[8] = buffer.get(3);

        bin[9] = packet.getType(); //TYPE

        buffer = ByteBuffer.allocate(2);
        buffer.putShort(packet.getSeq()); //MSGID
        bin[10] = buffer.get(0); //SEQ
        bin[11] = buffer.get(1);

        buffer = ByteBuffer.allocate(4); //SOURCE
        buffer.putInt(packet.getSource());
        bin[12] = buffer.get(0);
        bin[13] = buffer.get(1);
        bin[14] = buffer.get(2);
        bin[15] = buffer.get(3);

        buffer = ByteBuffer.allocate(4); //DEST
        buffer.putInt(packet.getDest());
        bin[16] = buffer.get(0);
        bin[17] = buffer.get(1);
        bin[18] = buffer.get(2);
        bin[19] = buffer.get(3);


        buffer = ByteBuffer.allocate(2); //TTL
        buffer.putShort(packet.getTtl());
        bin[20] = buffer.get(0);
        bin[21] = buffer.get(1);


        buffer = ByteBuffer.allocate(2); //STARTTTL
        buffer.putShort(packet.getStartTtl());
        bin[22] = buffer.get(0);
        bin[23] = buffer.get(1);

        bin[24] = packet.getFlags(); //FLAGS

        buffer = ByteBuffer.allocate(2); //LENGHT
        buffer.putShort(packet.getLength());
        bin[25] = buffer.get(0);
        bin[26] = buffer.get(1);

        bin[27] = 0x3f; //SM2

        if (packet.getBody().length > 0)
            System.arraycopy(packet.getBody(), 0, bin, 28, packet.getBody().length);

        return bin;
    }

    /**
     * Создает новый hello пакет
     *
     * @param msgId идентификтор трансляции
     * @param source адрес узла источника
     * @return пакет
     */
    default Packet createHelloPacket(int msgId, int source) {
        Packet helloPacket = new Packet();
        helloPacket.setMsgId(msgId);
        helloPacket.setType((byte) Packet.Type.HELLO.ordinal());
        helloPacket.setSource(source);
        helloPacket.setTtl((short) 1);
        helloPacket.setStartTtl((short) 1);
        helloPacket.setLength((short) 1);
        byte body[] = {0};
        helloPacket.setBody(body);

        return helloPacket;
    }

    /**
     * Создает новый preq пакет
     *
     * @param msgId идентификатор трансляции
     * @param source адрес узла источника
     * @param dest адрес узла назначения
     * @return пакет
     */
    default Packet createPreqPacket(int msgId, int source, int dest) {
        Packet preqPacket = new Packet();
        preqPacket.setMsgId(msgId);
        preqPacket.setType((byte) Packet.Type.PREQ.ordinal());
        preqPacket.setSource(source);
        preqPacket.setDest(dest);
        preqPacket.setTtl((short) 8);
        preqPacket.setStartTtl((short) 8);
        preqPacket.setLength((short) 1);
        preqPacket.setBody("0".getBytes());


        return preqPacket;
    }


}
