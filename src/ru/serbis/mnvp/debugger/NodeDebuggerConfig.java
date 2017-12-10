package ru.serbis.mnvp.debugger;

/**
 * Конфигурация отлидчика узла
 */
public class NodeDebuggerConfig {
    /** Перехватчик пакетов */
    private IncomingPacketInterceptor incomingPacketInterceptor;

    public IncomingPacketInterceptor getIncomingPacketInterceptor() {
        return incomingPacketInterceptor;
    }

    public void setIncomingPacketInterceptor(IncomingPacketInterceptor incomingPacketInterceptor) {
        this.incomingPacketInterceptor = incomingPacketInterceptor;
    }
}
