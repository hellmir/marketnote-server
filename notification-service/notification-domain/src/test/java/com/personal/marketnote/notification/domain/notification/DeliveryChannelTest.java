package com.personal.marketnote.notification.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryChannelTest {

    @Nested
    @DisplayName("PUSH_ONLY 채널")
    class PushOnly {

        @Test
        @DisplayName("푸시를 포함한다")
        void shouldHavePush() {
            assertThat(DeliveryChannel.PUSH_ONLY.hasPush()).isTrue();
        }

        @Test
        @DisplayName("인앱을 포함하지 않는다")
        void shouldNotHaveInApp() {
            assertThat(DeliveryChannel.PUSH_ONLY.hasInApp()).isFalse();
        }
    }

    @Nested
    @DisplayName("IN_APP_ONLY 채널")
    class InAppOnly {

        @Test
        @DisplayName("푸시를 포함하지 않는다")
        void shouldNotHavePush() {
            assertThat(DeliveryChannel.IN_APP_ONLY.hasPush()).isFalse();
        }

        @Test
        @DisplayName("인앱을 포함한다")
        void shouldHaveInApp() {
            assertThat(DeliveryChannel.IN_APP_ONLY.hasInApp()).isTrue();
        }
    }

    @Nested
    @DisplayName("PUSH_AND_IN_APP 채널")
    class PushAndInApp {

        @Test
        @DisplayName("푸시를 포함한다")
        void shouldHavePush() {
            assertThat(DeliveryChannel.PUSH_AND_IN_APP.hasPush()).isTrue();
        }

        @Test
        @DisplayName("인앱을 포함한다")
        void shouldHaveInApp() {
            assertThat(DeliveryChannel.PUSH_AND_IN_APP.hasInApp()).isTrue();
        }
    }
}
