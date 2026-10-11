package com.personal.marketnote.notification.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aligo")
public class AligoProperties {

    private String baseUrl = "https://apis.aligo.in";
    private String apiKey = "dev-aligo-api-key-change-me";
    private String userId = "dev-aligo-user-id-change-me";
    private int timeout = 10000;
    private boolean testMode = true;
    private Retry retry = new Retry();

    @Getter
    @Setter
    public static class Retry {
        private int maxAttempts = 3;
        private long initialBackoffMs = 1000;
    }
}
