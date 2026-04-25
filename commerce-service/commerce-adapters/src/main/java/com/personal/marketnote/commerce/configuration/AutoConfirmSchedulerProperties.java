package com.personal.marketnote.commerce.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "order.auto-confirm.scheduler")
@Getter
@Setter
public class AutoConfirmSchedulerProperties {
    private boolean enabled;
    private String cron = "0 59 23 * * ?";
    private long autoConfirmDays = 7;
}
