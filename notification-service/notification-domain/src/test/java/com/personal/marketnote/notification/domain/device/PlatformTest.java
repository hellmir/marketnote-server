package com.personal.marketnote.notification.domain.device;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlatformTest {

    @Test
    @DisplayName("ANDROID 문자열로 Platform을 생성한다")
    void shouldCreateAndroidPlatform() {
        Platform platform = Platform.from("ANDROID");

        assertThat(platform).isEqualTo(Platform.ANDROID);
    }

    @Test
    @DisplayName("IOS 문자열로 Platform을 생성한다")
    void shouldCreateIosPlatform() {
        Platform platform = Platform.from("IOS");

        assertThat(platform).isEqualTo(Platform.IOS);
    }

    @Test
    @DisplayName("소문자 android 문자열로 Platform을 생성한다")
    void shouldCreatePlatformFromLowercase() {
        Platform platform = Platform.from("android");

        assertThat(platform).isEqualTo(Platform.ANDROID);
    }

    @Test
    @DisplayName("알 수 없는 Platform 문자열이면 예외를 던진다")
    void shouldThrowExceptionWhenUnknownPlatform() {
        assertThatThrownBy(() -> Platform.from("WEB"))
                .isInstanceOf(InvalidPlatformException.class);
    }

    @Test
    @DisplayName("null 문자열이면 예외를 던진다")
    void shouldThrowExceptionWhenNull() {
        assertThatThrownBy(() -> Platform.from(null))
                .isInstanceOf(InvalidPlatformException.class);
    }

    @Test
    @DisplayName("빈 문자열이면 예외를 던진다")
    void shouldThrowExceptionWhenEmpty() {
        assertThatThrownBy(() -> Platform.from(""))
                .isInstanceOf(InvalidPlatformException.class);
    }
}
