package com.personal.marketnote.common.external.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExternalSystem 테스트")
class ExternalSystemTest {

    static Stream<Arguments> systems() {
        return Stream.of(
                Arguments.of(ExternalSystem.KCP, "kcp", "external.kcp"),
                Arguments.of(ExternalSystem.FASSTO, "fassto", "external.fassto"),
                Arguments.of(ExternalSystem.GOOGLE_OAUTH, "google_oauth", "external.google_oauth"),
                Arguments.of(ExternalSystem.KAKAO_OAUTH, "kakao_oauth", "external.kakao_oauth"),
                Arguments.of(ExternalSystem.ALIGO, "aligo", "external.aligo"),
                Arguments.of(ExternalSystem.S3, "s3", "external.s3"),
                Arguments.of(ExternalSystem.USER, "user", "external.user")
        );
    }

    @ParameterizedTest(name = "{0} → metricTag={1}, loggerName={2}")
    @MethodSource("systems")
    @DisplayName("각 외부 시스템 enum은 metricTag와 loggerName을 노출한다")
    void shouldExposeMetricTagAndLoggerName(ExternalSystem system, String expectedMetricTag, String expectedLoggerName) {
        assertThat(system.metricTag()).isEqualTo(expectedMetricTag);
        assertThat(system.loggerName()).isEqualTo(expectedLoggerName);
    }

    @Test
    @DisplayName("정의된 외부 시스템은 7개이다")
    void shouldHaveSevenDefinedSystems() {
        assertThat(ExternalSystem.values()).hasSize(7);
    }
}
