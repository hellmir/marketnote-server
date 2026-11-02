package com.personal.marketnote.common.domain.phonenumber;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidPhoneNumberException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.PhoneNumberNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTest {

    @Nested
    @DisplayName("of(String) 팩토리 메서드")
    class OfFactory {

        @Test
        @DisplayName("유효한 전화번호(010-1234-5678)로 생성 시 정상 생성된다")
        void shouldCreatePhoneNumberWith010() {
            PhoneNumber phoneNumber = PhoneNumber.of("010-1234-5678");

            assertThat(phoneNumber.getValue()).isEqualTo("010-1234-5678");
        }

        @Test
        @DisplayName("유효한 전화번호(011-123-4567)로 생성 시 정상 생성된다")
        void shouldCreatePhoneNumberWith011ShortMiddle() {
            PhoneNumber phoneNumber = PhoneNumber.of("011-123-4567");

            assertThat(phoneNumber.getValue()).isEqualTo("011-123-4567");
        }

        @Test
        @DisplayName("하이픈 없는 번호(01012345678)로 생성 시 InvalidPhoneNumberException을 던진다")
        void shouldThrowWhenNoHyphen() {
            assertThatThrownBy(() -> PhoneNumber.of("01012345678"))
                    .isInstanceOf(InvalidPhoneNumberException.class);
        }

        @Test
        @DisplayName("비허용 앞자리(012-1234-5678)로 생성 시 InvalidPhoneNumberException을 던진다")
        void shouldThrowWhenInvalidPrefix() {
            assertThatThrownBy(() -> PhoneNumber.of("012-1234-5678"))
                    .isInstanceOf(InvalidPhoneNumberException.class);
        }

        @Test
        @DisplayName("null로 생성 시 PhoneNumberNoValueException을 던진다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> PhoneNumber.of(null))
                    .isInstanceOf(PhoneNumberNoValueException.class);
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 PhoneNumberNoValueException을 던진다")
        void shouldThrowWhenEmpty() {
            assertThatThrownBy(() -> PhoneNumber.of(""))
                    .isInstanceOf(PhoneNumberNoValueException.class);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualityContract {

        @Test
        @DisplayName("같은 값이면 동등하다")
        void shouldBeEqualWhenSameValue() {
            PhoneNumber a = PhoneNumber.of("010-1234-5678");
            PhoneNumber b = PhoneNumber.of("010-1234-5678");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 동등하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            PhoneNumber a = PhoneNumber.of("010-1234-5678");
            PhoneNumber b = PhoneNumber.of("010-1234-5679");

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("toString PII 마스킹")
    class ToStringMasking {

        @Test
        @DisplayName("toString은 원본 값을 노출하지 않는다")
        void shouldNotLeakRawValueInToString() {
            PhoneNumber phoneNumber = PhoneNumber.of("010-1234-5678");

            assertThat(phoneNumber.toString()).doesNotContain("010-1234-5678");
            assertThat(phoneNumber.toString()).contains("***");
        }
    }
}
