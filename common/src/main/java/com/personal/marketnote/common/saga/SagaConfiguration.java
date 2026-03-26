package com.personal.marketnote.common.saga;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SagaProperties.class)
@EnableScheduling
public class SagaConfiguration {
}
