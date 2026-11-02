package com.personal.marketnote.common.configuration;

import com.personal.marketnote.common.utility.http.client.restclient.RestClientLoggingInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestClientCustomizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RestClientConfigTest {

    @Test
    @DisplayName("RestClientCustomizer 빈이 정상적으로 생성된다")
    void shouldCreateRestClientCustomizer() {
        // given
        RestClientConfig config = new RestClientConfig(5000, 10000);

        // when
        RestClientCustomizer customizer = config.restClientCustomizer(new RestClientLoggingInterceptor());

        // then
        assertThat(customizer).isNotNull();
    }

    @Test
    @DisplayName("RestClientCustomizer가 RestClient.Builder에 정상 적용된다")
    void shouldApplyCustomizerToRestClientBuilder() {
        // given
        RestClientConfig config = new RestClientConfig(3000, 8000);
        RestClientCustomizer customizer = config.restClientCustomizer(new RestClientLoggingInterceptor());

        // when & then
        assertThatCode(() -> {
            org.springframework.web.client.RestClient.Builder builder = org.springframework.web.client.RestClient.builder();
            customizer.customize(builder);
            builder.build();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("RestClientLoggingInterceptor 빈이 정상적으로 생성된다")
    void shouldCreateRestClientLoggingInterceptor() {
        // given
        RestClientConfig config = new RestClientConfig(5000, 10000);

        // when
        RestClientLoggingInterceptor interceptor = config.restClientLoggingInterceptor();

        // then
        assertThat(interceptor).isNotNull();
    }
}
