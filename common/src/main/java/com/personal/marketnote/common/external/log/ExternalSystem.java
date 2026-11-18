package com.personal.marketnote.common.external.log;

/**
 * 외부 시스템 호출 식별자.
 *
 * <p>{@link ExternalCallLogger}에서 메트릭 태그(`metricTag`)와 SLF4J 로거 이름(`loggerName`)을
 * 개입화한다. 시스템별로 패키지/카테고리 로거 레벨을 토글할 수 있도록 `external.<system>` 네임스페이스를 사용한다.
 */
public enum ExternalSystem {

    KCP("kcp"),
    FASSTO("fassto"),
    GOOGLE_OAUTH("google_oauth"),
    KAKAO_OAUTH("kakao_oauth"),
    ALIGO("aligo"),
    S3("s3");

    private static final String LOGGER_PREFIX = "external.";

    private final String metricTag;
    private final String loggerName;

    ExternalSystem(String metricTag) {
        this.metricTag = metricTag;
        this.loggerName = LOGGER_PREFIX + metricTag;
    }

    public String metricTag() {
        return metricTag;
    }

    public String loggerName() {
        return loggerName;
    }
}
