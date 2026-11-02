package com.personal.marketnote.fulfillment.adapter.out.notification.slack;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FulfillmentSlackProperties.class)
public class FulfillmentSlackConfiguration {
}
