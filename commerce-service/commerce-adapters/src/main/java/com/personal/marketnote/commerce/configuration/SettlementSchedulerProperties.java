package com.personal.marketnote.commerce.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "settlement.scheduler")
@Getter
@Setter
public class SettlementSchedulerProperties {
    private boolean enabled;
    private String cron = "0 0 2 1 * *";
    private Integer defaultPgFeeRate;
    private Integer defaultPlatformFeeRate;
}
