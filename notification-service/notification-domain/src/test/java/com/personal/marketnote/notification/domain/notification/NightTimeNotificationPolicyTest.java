package com.personal.marketnote.notification.domain.notification;

import com.personal.marketnote.notification.domain.template.NotificationCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NightTimeNotificationPolicyTest {

    @Nested
    @DisplayName("야간 시간 판별")
    class IsNightTime {

        @Test
        @DisplayName("21:00 정각이면 야간이다")
        void shouldBeNightTimeAt2100() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 21, 0, 0);

            assertThat(NightTimeNotificationPolicy.isNightTime(now)).isTrue();
        }

        @Test
        @DisplayName("23:59이면 야간이다")
        void shouldBeNightTimeAt2359() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 23, 59, 0);

            assertThat(NightTimeNotificationPolicy.isNightTime(now)).isTrue();
        }

        @Test
        @DisplayName("00:00이면 야간이다")
        void shouldBeNightTimeAt0000() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 10, 0, 0, 0);

            assertThat(NightTimeNotificationPolicy.isNightTime(now)).isTrue();
        }

        @Test
        @DisplayName("07:59이면 야간이다")
        void shouldBeNightTimeAt0759() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 10, 7, 59, 0);

            assertThat(NightTimeNotificationPolicy.isNightTime(now)).isTrue();
        }

        @Test
        @DisplayName("08:00 정각이면 야간이 아니다")
        void shouldNotBeNightTimeAt0800() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 10, 8, 0, 0);

            assertThat(NightTimeNotificationPolicy.isNightTime(now)).isFalse();
        }

        @Test
        @DisplayName("12:00이면 야간이 아니다")
        void shouldNotBeNightTimeAt1200() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 10, 12, 0, 0);

            assertThat(NightTimeNotificationPolicy.isNightTime(now)).isFalse();
        }

        @Test
        @DisplayName("20:59이면 야간이 아니다")
        void shouldNotBeNightTimeAt2059() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 20, 59, 0);

            assertThat(NightTimeNotificationPolicy.isNightTime(now)).isFalse();
        }
    }

    @Nested
    @DisplayName("다음 아침 08:00 계산")
    class CalculateNextMorning {

        @Test
        @DisplayName("21:00이면 다음날 08:00을 반환한다")
        void shouldReturnNextDayMorningAt2100() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 21, 0, 0);

            LocalDateTime result = NightTimeNotificationPolicy.calculateNextMorning(now);

            assertThat(result).isEqualTo(LocalDateTime.of(2026, 4, 10, 8, 0, 0));
        }

        @Test
        @DisplayName("23:59이면 다음날 08:00을 반환한다")
        void shouldReturnNextDayMorningAt2359() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 23, 59, 0);

            LocalDateTime result = NightTimeNotificationPolicy.calculateNextMorning(now);

            assertThat(result).isEqualTo(LocalDateTime.of(2026, 4, 10, 8, 0, 0));
        }

        @Test
        @DisplayName("00:00이면 당일 08:00을 반환한다")
        void shouldReturnSameDayMorningAt0000() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 10, 0, 0, 0);

            LocalDateTime result = NightTimeNotificationPolicy.calculateNextMorning(now);

            assertThat(result).isEqualTo(LocalDateTime.of(2026, 4, 10, 8, 0, 0));
        }

        @Test
        @DisplayName("07:59이면 당일 08:00을 반환한다")
        void shouldReturnSameDayMorningAt0759() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 10, 7, 59, 0);

            LocalDateTime result = NightTimeNotificationPolicy.calculateNextMorning(now);

            assertThat(result).isEqualTo(LocalDateTime.of(2026, 4, 10, 8, 0, 0));
        }
    }

    @Nested
    @DisplayName("예약 시간 결정")
    class ResolveScheduledAt {

        @Test
        @DisplayName("야간이고 PROMOTIONAL이면 다음 08:00을 반환한다")
        void shouldReturnNextMorningForPromotionalAtNight() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 22, 0, 0);

            LocalDateTime result = NightTimeNotificationPolicy.resolveScheduledAt(
                    NotificationCategory.PROMOTIONAL, now, null);

            assertThat(result).isEqualTo(LocalDateTime.of(2026, 4, 10, 8, 0, 0));
        }

        @Test
        @DisplayName("야간이 아니고 PROMOTIONAL이면 requestedScheduledAt을 그대로 반환한다")
        void shouldReturnRequestedForPromotionalAtDaytime() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 10, 0, 0);
            LocalDateTime requestedScheduledAt = LocalDateTime.of(2026, 4, 9, 15, 0, 0);

            LocalDateTime result = NightTimeNotificationPolicy.resolveScheduledAt(
                    NotificationCategory.PROMOTIONAL, now, requestedScheduledAt);

            assertThat(result).isEqualTo(requestedScheduledAt);
        }

        @Test
        @DisplayName("야간이고 MANDATORY이면 requestedScheduledAt을 그대로 반환한다")
        void shouldReturnRequestedForMandatoryAtNight() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 22, 0, 0);
            LocalDateTime requestedScheduledAt = LocalDateTime.of(2026, 4, 10, 9, 0, 0);

            LocalDateTime result = NightTimeNotificationPolicy.resolveScheduledAt(
                    NotificationCategory.MANDATORY, now, requestedScheduledAt);

            assertThat(result).isEqualTo(requestedScheduledAt);
        }

        @Test
        @DisplayName("야간이고 INFORMATIONAL이면 requestedScheduledAt을 그대로 반환한다")
        void shouldReturnRequestedForInformationalAtNight() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 22, 0, 0);

            LocalDateTime result = NightTimeNotificationPolicy.resolveScheduledAt(
                    NotificationCategory.INFORMATIONAL, now, null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("requestedScheduledAt이 null이고 야간이 아니면 null을 그대로 반환한다")
        void shouldReturnNullForNonNightWithNullScheduledAt() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 10, 0, 0);

            LocalDateTime result = NightTimeNotificationPolicy.resolveScheduledAt(
                    NotificationCategory.PROMOTIONAL, now, null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("requestedScheduledAt이 null이고 야간+PROMOTIONAL이면 다음 08:00을 반환한다")
        void shouldReturnNextMorningForNullScheduledAtAtNight() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 10, 3, 0, 0);

            LocalDateTime result = NightTimeNotificationPolicy.resolveScheduledAt(
                    NotificationCategory.PROMOTIONAL, now, null);

            assertThat(result).isEqualTo(LocalDateTime.of(2026, 4, 10, 8, 0, 0));
        }
    }
}
