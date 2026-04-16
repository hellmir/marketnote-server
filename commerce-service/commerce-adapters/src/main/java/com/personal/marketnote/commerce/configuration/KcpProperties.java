package com.personal.marketnote.commerce.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kcp")
@Getter
@Setter
public class KcpProperties {
    private String siteCd;
    private String certInfoPath;
    private String privateKeyPath;
    private Api api = new Api();
    private Retry retry = new Retry();
    private String retUrl;

    @Getter
    @Setter
    public static class Api {
        private String tradeRegisterUrl;
        private String paymentApprovalUrl;
        private String paymentCancelUrl;
        private String batchKeyIssuanceUrl;
    }

    @Getter
    @Setter
    public static class Retry {
        private int maxAttempts = 3;
        private long initialDelayMs = 1000L;
        private long backoffMultiplier = 2;
        private int readTimeoutMaxAttempts = 2;
    }
}
