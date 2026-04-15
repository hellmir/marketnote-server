package com.personal.marketnote.reward.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gifticon.pin")
@Getter
@Setter
public class GifticonPinProperties {
    private String encryptKey;
}
