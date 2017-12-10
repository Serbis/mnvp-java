package ru.serbis.mnvp.gateways;

/**
 * Суперкласс конфигурации создаваемого шлюза. Данная конфигурация используется
 * в следущих случаях.
 *
 * 1. При ручном создании шлюза при инициализации или в процессе работы узла
 * 2. При порождени шлюза акцепторами
 */
public class GatewayConfig {
    private String label;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
