package com.personal.marketnote.common.domain.deliveryrequestmessage;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidDeliveryRequestMessageLengthException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.DeliveryRequestMessageNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeliveryRequestMessageTest {

    @Nested
    @DisplayName("of(String) 팩토리 메서드")
    class OfFactory {

        @Test
        @DisplayName("60자 이하 메시지로 생성 시 정상 생성된다")
        void shouldCreateWhenMessageIsWithinMaxLength() {
            String message = "부재 시 문 앞에 놓아주세요";

            DeliveryRequestMessage result = DeliveryRequestMessage.of(message);

            assertThat(result.getValue()).isEqualTo(message);
        }

        @Test
        @DisplayName("정확히 60자 메시지로 생성 시 정상 생성된다")
        void shouldCreateWhenMessageIsExactly60Chars() {
            String message = "a".repeat(60);

            DeliveryRequestMessage result = DeliveryRequestMessage.of(message);

            assertThat(result.getValue()).isEqualTo(message);
            assertThat(result.getValue().length()).isEqualTo(60);
        }

        @Test
        @DisplayName("61자 메시지로 생성 시 InvalidDeliveryRequestMessageLengthException이 발생한다")
        void shouldThrowExceptionWhenMessageExceeds60Chars() {
            String message = "a".repeat(61);

            assertThatThrownBy(() -> DeliveryRequestMessage.of(message))
                    .isInstanceOf(InvalidDeliveryRequestMessageLengthException.class);
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 DeliveryRequestMessageNoValueException이 발생한다")
        void shouldThrowExceptionWhenMessageIsEmpty() {
            assertThatThrownBy(() -> DeliveryRequestMessage.of(""))
                    .isInstanceOf(DeliveryRequestMessageNoValueException.class);
        }

        @Test
        @DisplayName("공백 문자열로 생성 시 DeliveryRequestMessageNoValueException이 발생한다")
        void shouldThrowExceptionWhenMessageIsBlank() {
            assertThatThrownBy(() -> DeliveryRequestMessage.of("   "))
                    .isInstanceOf(DeliveryRequestMessageNoValueException.class);
        }

        @Test
        @DisplayName("null로 생성 시 DeliveryRequestMessageNoValueException이 발생한다")
        void shouldThrowExceptionWhenMessageIsNull() {
            assertThatThrownBy(() -> DeliveryRequestMessage.of(null))
                    .isInstanceOf(DeliveryRequestMessageNoValueException.class);
        }
    }

    @Nested
    @DisplayName("equals / hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("같은 값의 DeliveryRequestMessage는 동등하다")
        void shouldBeEqualWhenSameValue() {
            DeliveryRequestMessage a = DeliveryRequestMessage.of("문 앞에 두세요");
            DeliveryRequestMessage b = DeliveryRequestMessage.of("문 앞에 두세요");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 값의 DeliveryRequestMessage는 동등하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            DeliveryRequestMessage a = DeliveryRequestMessage.of("문 앞에 두세요");
            DeliveryRequestMessage b = DeliveryRequestMessage.of("경비실에 맡겨주세요");

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("null과 비교하면 동등하지 않다")
        void shouldNotBeEqualToNull() {
            DeliveryRequestMessage message = DeliveryRequestMessage.of("문 앞");

            assertThat(message).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과 비교하면 동등하지 않다")
        void shouldNotBeEqualToDifferentType() {
            DeliveryRequestMessage message = DeliveryRequestMessage.of("문 앞");

            assertThat(message).isNotEqualTo("문 앞");
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("value를 포함한 문자열을 반환한다")
        void shouldReturnStringContainingValue() {
            DeliveryRequestMessage message = DeliveryRequestMessage.of("문 앞에 두세요");

            assertThat(message.toString()).contains("문 앞에 두세요");
        }
    }
}
