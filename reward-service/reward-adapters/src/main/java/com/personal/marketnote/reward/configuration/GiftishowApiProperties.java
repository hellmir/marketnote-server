package com.personal.marketnote.reward.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

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
    private String phoneNo;
    private String mmsMsg;
    private String mmsTitle;
    private String gubun;
    private String userId;
    private Api api = new Api();

    /**
     * Giftishow API 엔드포인트 정의. 신규 path 추가 시 필드명은 반드시 {@code *Path}로 끝나야 한다.
     * {@link GiftishowApiPropertiesValidator}가 reflection으로 sensitive-paths 정합을 검증한다.
     */
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
        private Set<String> sensitivePaths = Set.of();
    }
}
