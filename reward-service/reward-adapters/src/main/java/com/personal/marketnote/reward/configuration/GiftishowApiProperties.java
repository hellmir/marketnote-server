package com.personal.marketnote.reward.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vendor.giftishow")
@Getter
@Setter
public class GiftishowApiProperties {
    private String baseUrl;
    private String apiCode;
    private String authCode;
    private String authToken;
    private String devYn;
    private String callbackNo;
    private String gubun;
    private Api api = new Api();

    @Getter
    @Setter
    public static class Api {
        private String productListPath;
        private String productDetailPath;
        private String brandListPath;
        private String brandDetailPath;
        private String couponSendPath;
        private String couponDetailPath;
        private String couponCancelPath;
        private String couponResendPath;
        private String bizMoneyBalancePath;
        private String couponSendFailCancelPath;
    }
}
