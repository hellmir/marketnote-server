package com.personal.marketnote.fulfillment.adapter.out.notification.slack;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fulfillment.slack")
public class FulfillmentSlackProperties {
    private String webhookUrl;
}
