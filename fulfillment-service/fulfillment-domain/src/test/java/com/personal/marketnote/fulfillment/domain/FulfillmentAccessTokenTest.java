package com.personal.marketnote.fulfillment.domain;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.ParsingLocalDateTimeException;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FulfillmentAccessTokenTest {

    @Nested
    @DisplayName("of() 생성")
    class CreateTest {

        @Test
        @DisplayName("유효한 value와 expreDatetime으로 FulfillmentAccessToken을 생성한다")
        void of_withValidParams_createsToken() {
            FulfillmentAccessToken token = FulfillmentAccessToken.of("valid-token", "20260101120000");

            assertThat(token).isNotNull();
        }

        @Test
        @DisplayName("value가 null이면 FulfillmentQueryParameterNoValueException이 발생한다")
        void of_withNullValue_throwsException() {
            assertThatThrownBy(() -> FulfillmentAccessToken.of(null, "20260101120000"))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("expreDatetime 형식이 잘못되면 ParsingLocalDateTimeException이 발생한다")
        void of_withInvalidDatetimeFormat_throwsException() {
            assertThatThrownBy(() -> FulfillmentAccessToken.of("valid-token", "invalid-format"))
                    .isInstanceOf(ParsingLocalDateTimeException.class);
        }
    }

    @Nested
    @DisplayName("isExpired() 만료 판정")
    class IsExpiredTest {

        @Test
        @DisplayName("now가 expiresAt 이후이면 isExpired가 true를 반환한다")
        void isExpired_whenNowIsAfterExpiresAt_returnsTrue() {
            FulfillmentAccessToken token = FulfillmentAccessToken.of("valid-token", "20260101120000");
            LocalDateTime afterExpiry = LocalDateTime.of(2026, 1, 1, 13, 0, 0);

            assertThat(token.isExpired(afterExpiry)).isTrue();
        }

        @Test
        @DisplayName("now가 expiresAt 이전이면 isExpired가 false를 반환한다")
        void isExpired_whenNowIsBeforeExpiresAt_returnsFalse() {
            FulfillmentAccessToken token = FulfillmentAccessToken.of("valid-token", "20260101120000");
            LocalDateTime beforeExpiry = LocalDateTime.of(2026, 1, 1, 11, 0, 0);

            assertThat(token.isExpired(beforeExpiry)).isFalse();
        }
    }

    @Nested
    @DisplayName("isExpiringSoon() 선제 만료 판정")
    class IsExpiringSoonTest {

        @Test
        @DisplayName("now가 expiresAt - buffer 이후이면 isExpiringSoon이 true를 반환한다")
        void isExpiringSoon_whenWithinBuffer_returnsTrue() {
            FulfillmentAccessToken token = FulfillmentAccessToken.of("valid-token", "20260101120000");
            LocalDateTime withinBuffer = LocalDateTime.of(2026, 1, 1, 11, 59, 30);

            assertThat(token.isExpiringSoon(withinBuffer, 60L)).isTrue();
        }

        @Test
        @DisplayName("now가 expiresAt 이후이면 isExpiringSoon이 true를 반환한다")
        void isExpiringSoon_whenAlreadyExpired_returnsTrue() {
            FulfillmentAccessToken token = FulfillmentAccessToken.of("valid-token", "20260101120000");
            LocalDateTime afterExpiry = LocalDateTime.of(2026, 1, 1, 13, 0, 0);

            assertThat(token.isExpiringSoon(afterExpiry, 60L)).isTrue();
        }

        @Test
        @DisplayName("now가 expiresAt - buffer 이전이면 isExpiringSoon이 false를 반환한다")
        void isExpiringSoon_whenOutsideBuffer_returnsFalse() {
            FulfillmentAccessToken token = FulfillmentAccessToken.of("valid-token", "20260101120000");
            LocalDateTime beforeBuffer = LocalDateTime.of(2026, 1, 1, 11, 58, 0);

            assertThat(token.isExpiringSoon(beforeBuffer, 60L)).isFalse();
        }

        @Test
        @DisplayName("now가 null이면 isExpiringSoon이 true를 반환한다")
        void isExpiringSoon_whenNowIsNull_returnsTrue() {
            FulfillmentAccessToken token = FulfillmentAccessToken.of("valid-token", "20260101120000");

            assertThat(token.isExpiringSoon(null, 60L)).isTrue();
        }
    }
}
