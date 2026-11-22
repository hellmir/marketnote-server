package com.personal.marketnote.notification.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = "aligo")
public class AligoProperties {

    private String baseUrl = "https://apis.aligo.in";
    private String apiKey = "dev-aligo-api-key-change-me";
    private String userId = "dev-aligo-user-id-change-me";
    private String sender = "dev-aligo-sender-change-me";
    private int timeout = 10000;
    private boolean testMode = true;
    private Retry retry = new Retry();
    private Api api = new Api();

    @Getter
    @Setter
    public static class Retry {
        private int maxAttempts = 3;
        private long initialBackoffMs = 1000;
    }

    /**
     * Aligo API 엔드포인트 정의. 신규 path 추가 시 필드명은 반드시 {@code *Path}로 끝나야 한다.
     * {@link com.personal.marketnote.notification.configuration.AligoApiPropertiesValidator}가
     * reflection으로 sensitive-paths 정합을 검증한다.
     */
    @Getter
    @Setter
    public static class Api {
        private String alimtalkSendPath;
        private String brandTalkTemplateCreatePath;
        private String brandTalkTemplateListPath;
        private String profileAuthPath;
        private String profileAddPath;
        private String profileListPath;
        private String heartInfoPath;
        private String cancelPath;
        private String historyListPath;
        private String templateAddPath;
        private String templateModifyPath;
        private String templateListPath;
        private String templateRequestPath;
        private String templateDelPath;
        private String categoryPath;
        private String historyDetailPath;
        private String brandTalkSendPath;
        private Set<String> sensitivePaths = Set.of();
    }
}
