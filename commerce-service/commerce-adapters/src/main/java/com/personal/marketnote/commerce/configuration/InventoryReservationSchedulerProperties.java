package com.personal.marketnote.commerce.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "inventory.reservation.scheduler")
@Getter
@Setter
public class InventoryReservationSchedulerProperties {
    private boolean enabled;
    private long timeoutMinutes = 10;
    private long checkIntervalMs = 60000;
}
