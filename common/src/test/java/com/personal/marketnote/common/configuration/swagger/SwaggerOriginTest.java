package com.personal.marketnote.common.configuration.swagger;

import com.personal.marketnote.common.exception.InvalidSwaggerOriginException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwaggerOriginTest {

    @Test
    @DisplayName("https URL을 scheme과 baseHost로 분리한다")
    void parsesHttpsUrl() {
        SwaggerOrigin origin = SwaggerOrigin.parse("https://marketnote.store");

        assertThat(origin.scheme()).isEqualTo("https");
        assertThat(origin.baseHost()).isEqualTo("marketnote.store");
    }

    @Test
    @DisplayName("port가 포함된 http URL은 baseHost에 port를 포함한다")
    void parsesHttpUrlWithPort() {
        SwaggerOrigin origin = SwaggerOrigin.parse("http://localhost:3000");

        assertThat(origin.scheme()).isEqualTo("http");
        assertThat(origin.baseHost()).isEqualTo("localhost:3000");
    }

    @Test
    @DisplayName("origin이 null이면 InvalidSwaggerOriginException을 던진다")
    void throwsWhenOriginIsNull() {
        assertThatThrownBy(() -> SwaggerOrigin.parse(null))
                .isInstanceOf(InvalidSwaggerOriginException.class);
    }

    @Test
    @DisplayName("origin이 빈 문자열이면 InvalidSwaggerOriginException을 던진다")
    void throwsWhenOriginIsBlank() {
        assertThatThrownBy(() -> SwaggerOrigin.parse(""))
                .isInstanceOf(InvalidSwaggerOriginException.class);
    }

    @Test
    @DisplayName("scheme이 없는 origin은 InvalidSwaggerOriginException을 던진다")
    void throwsWhenSchemeIsMissing() {
        assertThatThrownBy(() -> SwaggerOrigin.parse("marketnote.store"))
                .isInstanceOf(InvalidSwaggerOriginException.class);
    }

    @Test
    @DisplayName("javascript scheme은 허용하지 않고 InvalidSwaggerOriginException을 던진다")
    void throwsWhenJavascriptScheme() {
        assertThatThrownBy(() -> SwaggerOrigin.parse("javascript://attacker.com/xss"))
                .isInstanceOf(InvalidSwaggerOriginException.class);
    }

    @Test
    @DisplayName("file scheme은 허용하지 않고 InvalidSwaggerOriginException을 던진다")
    void throwsWhenFileScheme() {
        assertThatThrownBy(() -> SwaggerOrigin.parse("file:///etc/passwd"))
                .isInstanceOf(InvalidSwaggerOriginException.class);
    }

    @Test
    @DisplayName("data scheme은 허용하지 않고 InvalidSwaggerOriginException을 던진다")
    void throwsWhenDataScheme() {
        assertThatThrownBy(() -> SwaggerOrigin.parse("data:text/html,<script>alert(1)</script>"))
                .isInstanceOf(InvalidSwaggerOriginException.class);
    }

    @Test
    @DisplayName("host가 없는 origin은 InvalidSwaggerOriginException을 던진다")
    void throwsWhenHostIsMissing() {
        assertThatThrownBy(() -> SwaggerOrigin.parse("https://"))
                .isInstanceOf(InvalidSwaggerOriginException.class);
    }

    @Test
    @DisplayName("URI 파싱이 실패하는 형식은 InvalidSwaggerOriginException을 던진다")
    void throwsWhenUriIsMalformed() {
        assertThatThrownBy(() -> SwaggerOrigin.parse("https://invalid host"))
                .isInstanceOf(InvalidSwaggerOriginException.class);
    }

    @Test
    @DisplayName("canonical constructor 직접 호출로 허용되지 않는 scheme을 전달하면 InvalidSwaggerOriginException을 던진다")
    void compactConstructorRejectsDisallowedScheme() {
        assertThatThrownBy(() -> new SwaggerOrigin("javascript", "evil.com"))
                .isInstanceOf(InvalidSwaggerOriginException.class);
    }

    @Test
    @DisplayName("canonical constructor 직접 호출로 빈 baseHost를 전달하면 InvalidSwaggerOriginException을 던진다")
    void compactConstructorRejectsBlankHost() {
        assertThatThrownBy(() -> new SwaggerOrigin("https", ""))
                .isInstanceOf(InvalidSwaggerOriginException.class);
    }

    @Test
    @DisplayName("canonical constructor 직접 호출로 null scheme을 전달하면 InvalidSwaggerOriginException을 던진다")
    void compactConstructorRejectsNullScheme() {
        assertThatThrownBy(() -> new SwaggerOrigin(null, "marketnote.store"))
                .isInstanceOf(InvalidSwaggerOriginException.class);
    }
}
