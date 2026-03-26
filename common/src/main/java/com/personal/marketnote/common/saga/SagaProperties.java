package com.personal.marketnote.common.saga;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "saga")
@Getter
@Setter
public class SagaProperties {
    private boolean enabled = false;
    private long timeoutMs = 60000;
    private long compensationTimeoutMs = 120000;
    private long checkIntervalMs = 30000;
}
