package com.personal.marketnote.common.outbox;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outbox")
@Getter
@Setter
public class OutboxProperties {
    private boolean enabled = false;
    private long pollingIntervalMs = 3000;
    private int batchSize = 100;
    private int maxRetries = 5;
    private int retentionDays = 7;
    private String cleanupCron = "0 0 3 * * ?";
}
