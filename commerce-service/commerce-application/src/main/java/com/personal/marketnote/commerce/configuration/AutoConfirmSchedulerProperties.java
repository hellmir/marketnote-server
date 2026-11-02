package com.personal.marketnote.commerce.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "order.auto-confirm.scheduler")
@Validated
@Getter
@Setter
public class AutoConfirmSchedulerProperties {
    private boolean enabled;

    @NotBlank
    private String cron = "0 59 23 * * ?";

    @Min(1)
    private long autoConfirmDays = 7;
}
