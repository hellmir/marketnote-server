package com.personal.marketnote.common.configuration.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka.slack")
public class KafkaSlackProperties {
    private String webhookUrl;
}
