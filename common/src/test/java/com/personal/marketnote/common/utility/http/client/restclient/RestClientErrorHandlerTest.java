package com.personal.marketnote.common.utility.http.client.restclient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RestClientErrorHandlerTest {

    @Test
    @DisplayName("noOpErrorHandler는 null이 아닌 핸들러를 반환한다")
    void shouldReturnNonNullHandler() {
        // when
        RestClient.ResponseSpec.ErrorHandler handler = RestClientErrorHandler.noOpErrorHandler();

        // then
        assertThat(handler).isNotNull();
    }

    @Test
    @DisplayName("4xx 상태 코드는 에러로 판단한다")
    void shouldDetect4xxAsError() {
        // when & then
        assertThat(RestClientErrorHandler.isError(HttpStatusCode.valueOf(400))).isTrue();
        assertThat(RestClientErrorHandler.isError(HttpStatusCode.valueOf(404))).isTrue();
        assertThat(RestClientErrorHandler.isError(HttpStatusCode.valueOf(422))).isTrue();
    }

    @Test
    @DisplayName("5xx 상태 코드는 에러로 판단한다")
    void shouldDetect5xxAsError() {
        // when & then
        assertThat(RestClientErrorHandler.isError(HttpStatusCode.valueOf(500))).isTrue();
        assertThat(RestClientErrorHandler.isError(HttpStatusCode.valueOf(502))).isTrue();
        assertThat(RestClientErrorHandler.isError(HttpStatusCode.valueOf(503))).isTrue();
    }

    @Test
    @DisplayName("2xx 상태 코드는 에러가 아니다")
    void shouldNotDetect2xxAsError() {
        // when & then
        assertThat(RestClientErrorHandler.isError(HttpStatusCode.valueOf(200))).isFalse();
        assertThat(RestClientErrorHandler.isError(HttpStatusCode.valueOf(201))).isFalse();
        assertThat(RestClientErrorHandler.isError(HttpStatusCode.valueOf(204))).isFalse();
    }

    @Test
    @DisplayName("3xx 상태 코드는 에러가 아니다")
    void shouldNotDetect3xxAsError() {
        // when & then
        assertThat(RestClientErrorHandler.isError(HttpStatusCode.valueOf(301))).isFalse();
        assertThat(RestClientErrorHandler.isError(HttpStatusCode.valueOf(302))).isFalse();
    }

    @Test
    @DisplayName("noOpErrorHandler는 RestClient.Builder에 정상 적용된다")
    void shouldBeApplicableToRestClientBuilder() {
        // when & then
        assertThatCode(() -> RestClient.builder()
                .defaultStatusHandler(RestClientErrorHandler::isError, RestClientErrorHandler.noOpErrorHandler())
                .build()
        ).doesNotThrowAnyException();
    }
}
