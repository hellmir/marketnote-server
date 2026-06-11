package com.personal.marketnote.notification.domain.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationCategoryTest {

    @Nested
    @DisplayName("MANDATORY 카테고리")
    class Mandatory {

        @Test
        @DisplayName("수신 동의가 불필요하다")
        void shouldNotRequireConsent() {
            assertThat(NotificationCategory.MANDATORY.requiresConsent()).isFalse();
        }

        @Test
        @DisplayName("야간 발송 제한이 없다")
        void shouldNotHaveNightRestriction() {
            assertThat(NotificationCategory.MANDATORY.hasNightRestriction()).isFalse();
        }

        @Test
        @DisplayName("광고 표기가 불필요하다")
        void shouldNotRequireAdLabel() {
            assertThat(NotificationCategory.MANDATORY.requiresAdLabel()).isFalse();
        }
    }

    @Nested
    @DisplayName("INFORMATIONAL 카테고리")
    class Informational {

        @Test
        @DisplayName("수신 동의가 불필요하다")
        void shouldNotRequireConsent() {
            assertThat(NotificationCategory.INFORMATIONAL.requiresConsent()).isFalse();
        }

        @Test
        @DisplayName("야간 발송 제한이 없다")
        void shouldNotHaveNightRestriction() {
            assertThat(NotificationCategory.INFORMATIONAL.hasNightRestriction()).isFalse();
        }

        @Test
        @DisplayName("광고 표기가 불필요하다")
        void shouldNotRequireAdLabel() {
            assertThat(NotificationCategory.INFORMATIONAL.requiresAdLabel()).isFalse();
        }
    }

    @Nested
    @DisplayName("PROMOTIONAL 카테고리")
    class Promotional {

        @Test
        @DisplayName("수신 동의가 필요하다")
        void shouldRequireConsent() {
            assertThat(NotificationCategory.PROMOTIONAL.requiresConsent()).isTrue();
        }

        @Test
        @DisplayName("야간 발송 제한이 있다")
        void shouldHaveNightRestriction() {
            assertThat(NotificationCategory.PROMOTIONAL.hasNightRestriction()).isTrue();
        }

        @Test
        @DisplayName("광고 표기가 필요하다")
        void shouldRequireAdLabel() {
            assertThat(NotificationCategory.PROMOTIONAL.requiresAdLabel()).isTrue();
        }
    }
}
