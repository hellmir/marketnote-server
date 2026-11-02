package com.personal.marketnote.common.saga;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "saga.slack")
public class SagaSlackProperties {
    private String webhookUrl;
}
