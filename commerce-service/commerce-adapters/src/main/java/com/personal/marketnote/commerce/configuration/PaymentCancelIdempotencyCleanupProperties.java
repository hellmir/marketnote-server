package com.personal.marketnote.commerce.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "commerce.payment.idempotency-cleanup")
@Validated
@Getter
@Setter
public class PaymentCancelIdempotencyCleanupProperties {

    @Min(1)
    private long ttlHours = 24;

    @NotBlank
    private String cron = "0 0 3 * * *";
}
