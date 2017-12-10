package ru.serbis.mnvp.debugger;

import ru.serbis.mnvp.structs.general.Packet;

public class IncomingPacketInterceptor {
    public interface PacketInterceptorCallback {
        boolean intercept(Packet packet);
    }

    private PacketInterceptorCallback packetInterceptorCallback;

    public boolean intercept(Packet packet) {
        return packetInterceptorCallback == null || packetInterceptorCallback.intercept(packet);

    }

    public void setPacketInterceptorCallback(PacketInterceptorCallback packetInterceptorCallback) {
        this.packetInterceptorCallback = packetInterceptorCallback;
    }
}
