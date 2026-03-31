package com.personal.marketnote.reward.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vendor.offerwall.adpopcorn.hash-key")
public class AdpopcornHashKeyProperties {
    private String android;
    private String ios;
}
