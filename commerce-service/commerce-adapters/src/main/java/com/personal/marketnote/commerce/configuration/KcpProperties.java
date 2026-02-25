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
    private String retUrl;

    @Getter
    @Setter
    public static class Api {
        private String tradeRegisterUrl;
        private String paymentApprovalUrl;
        private String paymentCancelUrl;
    }
}
