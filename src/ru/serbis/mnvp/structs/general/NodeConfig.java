package ru.serbis.mnvp.structs.general;

import ru.serbis.mnvp.acceptors.AcceptorConfig;
import ru.serbis.mnvp.debugger.NodeDebuggerConfig;
import ru.serbis.mnvp.gateways.GatewayConfig;
import ru.serbis.mnvp.np.NetworkProcessor;
import ru.serbis.mnvp.np.NetworkProcessorConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Общая конфиуграция узла
 */
public class NodeConfig {

    /** Список конфигураций акцепторов */
    private List<AcceptorConfig> acceptorConfigs = new ArrayList<>();
    /** Список конфигураций шлюзов */
    private List<GatewayConfig> gatewayConfigs = new ArrayList<>();
    /** Конфигурация сетевого процессора */
    private NetworkProcessorConfig networkProcessorConfig = new NetworkProcessorConfig();
    /** Конфигурация отладчика узла */
    private NodeDebuggerConfig nodeDebuggerConfig;
    /** Метка узла */
    private String label;
    /** Сетевой адрес узла */
    private int networkAddress;
    /** Файл лога */
    private String logFilePath;
    /** Уровень логгирования узла */
    private int logLevel = 99;
    /** Флаг включающий отладочный режим узла */
    private boolean debugMode = false;

    public NodeConfig() {
    }

    public NodeConfig(NodeConfig other) {
        this.acceptorConfigs = other.acceptorConfigs;
    }

    public List<AcceptorConfig> getAcceptorConfigs() {
        return acceptorConfigs;
    }

    public void setAcceptorConfigs(List<AcceptorConfig> acceptorConfigs) {
        this.acceptorConfigs = acceptorConfigs;
    }

    public List<GatewayConfig> getGatewayConfigs() {
        return gatewayConfigs;
    }

    public void setGatewayConfigs(List<GatewayConfig> gatewayConfigs) {
        this.gatewayConfigs = gatewayConfigs;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getNetworkAddress() {
        return networkAddress;
    }

    public void setNetworkAddress(int networkAddress) {
        this.networkAddress = networkAddress;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public NetworkProcessorConfig getNetworkProcessorConfig() {
        return networkProcessorConfig;
    }

    public void setNetworkProcessorConfig(NetworkProcessorConfig networkProcessorConfig) {
        this.networkProcessorConfig = networkProcessorConfig;
    }

    public NodeDebuggerConfig getNodeDebuggerConfig() {
        return nodeDebuggerConfig;
    }

    public void setNodeDebuggerConfig(NodeDebuggerConfig nodeDebuggerConfig) {
        this.nodeDebuggerConfig = nodeDebuggerConfig;
    }


    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public String toString() {
        return "NodeConfig{" +
                "acceptorConfigs=" + acceptorConfigs +
                ", gatewayConfigs=" + gatewayConfigs +
                ", networkProcessorConfig=" + networkProcessorConfig +
                ", nodeDebuggerConfig=" + nodeDebuggerConfig +
                ", label='" + label + '\'' +
                ", networkAddress=" + networkAddress +
                ", logFilePath='" + logFilePath + '\'' +
                ", logLevel=" + logLevel +
                ", debugMode=" + debugMode +
                '}';
    }
}
