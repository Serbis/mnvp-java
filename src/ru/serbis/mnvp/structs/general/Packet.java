package ru.serbis.mnvp.structs.general;

import ru.serbis.mnvp.gateways.Gateway;

import java.util.Arrays;

/**
 * Представление структуры пакета сети
 */
public class Packet {
    private int version = 1;
    private int msgId;
    private byte type;
    private short seq = 0;
    private int source;
    private int dest;
    private short ttl = 10;
    private short startTtl = 10;
    private byte flags = 0x00;
    private short length;
    private byte[] body = {};
    private long ts = 0;
    private String gatewayLabel;

    public Packet() {
    }

    public Packet(Packet other) {
        this.version = other.version;
        this.msgId = other.msgId;
        this.type = other.type;
        this.seq = other.seq;
        this.source = other.source;
        this.dest = other.dest;
        this.ttl = other.ttl;
        this.startTtl = other.startTtl;
        this.flags = other.flags;
        this.length = other.length;
        this.body = other.body;
        this.ts = other.ts;
        this.gatewayLabel = other.gatewayLabel;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public short getSeq() {
        return seq;
    }

    public void setSeq(short seq) {
        this.seq = seq;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getDest() {
        return dest;
    }

    public void setDest(int dest) {
        this.dest = dest;
    }

    public short getTtl() {
        return ttl;
    }

    public void setTtl(short ttl) {
        this.ttl = ttl;
    }

    public short getStartTtl() {
        return startTtl;
    }

    public void setStartTtl(short startTtl) {
        this.startTtl = startTtl;
    }

    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public short getLength() {
        return length;
    }

    public void setLength(short length) {
        this.length = length;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getGatewayLabel() {
        return gatewayLabel;
    }

    public void setGatewayLabel(String gatewayLabel) {
        this.gatewayLabel = gatewayLabel;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "version=" + version +
                ", msgId=" + msgId +
                ", type=" + type +
                ", seq=" + seq +
                ", source=" + source +
                ", dest=" + dest +
                ", ttl=" + ttl +
                ", startTtl=" + startTtl +
                ", flags=" + flags +
                ", length=" + length +
                ", body=" + Arrays.toString(body) +
                ", ts=" + ts +
                ", gatewayLabel='" + gatewayLabel + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Packet packet = (Packet) o;

        if (version != packet.version) return false;
        if (msgId != packet.msgId) return false;
        if (type != packet.type) return false;
        if (seq != packet.seq) return false;
        if (source != packet.source) return false;
        if (dest != packet.dest) return false;
        if (ttl != packet.ttl) return false;
        if (startTtl != packet.startTtl) return false;
        if (flags != packet.flags) return false;
        if (length != packet.length) return false;
        if (ts != packet.ts) return false;
        if (!Arrays.equals(body, packet.body)) return false;
        return gatewayLabel != null ? gatewayLabel.equals(packet.gatewayLabel) : packet.gatewayLabel == null;
    }



    public enum Type {
        HELLO, NETWORK_ERROR, PREQ, BINARY, ECHO
    }
}
