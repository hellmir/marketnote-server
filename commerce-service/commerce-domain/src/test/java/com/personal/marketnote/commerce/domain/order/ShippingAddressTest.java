package com.personal.marketnote.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShippingAddressTest {

    @Nested
    @DisplayName("ShippingAddress 생성 (of)")
    class OfTest {

        @Test
        @DisplayName("모든 필드가 정상적으로 설정된다")
        void shouldCreateWithAllFields() {
            // given & when
            ShippingAddress shippingAddress = ShippingAddress.of(
                    "홍길동",
                    "01012345678",
                    "12345",
                    "서울시 강남구",
                    "테헤란로 123",
                    "문 앞에 놓아주세요"
            );

            // then
            assertThat(shippingAddress.getRecipientName()).isEqualTo("홍길동");
            assertThat(shippingAddress.getRecipientPhoneNumber()).isEqualTo("01012345678");
            assertThat(shippingAddress.getZipCode()).isEqualTo("12345");
            assertThat(shippingAddress.getAddress()).isEqualTo("서울시 강남구");
            assertThat(shippingAddress.getAddressDetail()).isEqualTo("테헤란로 123");
            assertThat(shippingAddress.getRequestMessage()).isEqualTo("문 앞에 놓아주세요");
        }

        @Test
        @DisplayName("요청사항이 null이어도 정상 생성된다")
        void shouldCreateWithNullRequestMessage() {
            // given & when
            ShippingAddress shippingAddress = ShippingAddress.of(
                    "홍길동",
                    "01012345678",
                    "12345",
                    "서울시 강남구",
                    "테헤란로 123",
                    null
            );

            // then
            assertThat(shippingAddress.getRecipientName()).isEqualTo("홍길동");
            assertThat(shippingAddress.getRequestMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("수령인명 존재 여부 확인 (hasRecipientName)")
    class HasRecipientNameTest {

        @Test
        @DisplayName("수령인명이 있으면 true를 반환한다")
        void shouldReturnTrueWhenRecipientNameExists() {
            // given
            ShippingAddress shippingAddress = ShippingAddress.of(
                    "홍길동", "01012345678", "12345", "주소", "상세주소", null
            );

            // when & then
            assertThat(shippingAddress.hasRecipientName()).isTrue();
        }

        @Test
        @DisplayName("수령인명이 null이면 false를 반환한다")
        void shouldReturnFalseWhenRecipientNameIsNull() {
            // given
            ShippingAddress shippingAddress = ShippingAddress.of(
                    null, null, null, null, null, null
            );

            // when & then
            assertThat(shippingAddress.hasRecipientName()).isFalse();
        }
    }

    @Nested
    @DisplayName("요청사항 제외 복사 (withoutRequestMessage)")
    class WithoutRequestMessageTest {

        @Test
        @DisplayName("요청사항을 제외하고 나머지 필드가 그대로 복사된다")
        void shouldCopyAllFieldsExceptRequestMessage() {
            // given
            ShippingAddress original = ShippingAddress.of(
                    "홍길동",
                    "01012345678",
                    "12345",
                    "서울시 강남구",
                    "테헤란로 123",
                    "문 앞에 놓아주세요"
            );

            // when
            ShippingAddress copied = original.withoutRequestMessage();

            // then
            assertThat(copied.getRecipientName()).isEqualTo("홍길동");
            assertThat(copied.getRecipientPhoneNumber()).isEqualTo("01012345678");
            assertThat(copied.getZipCode()).isEqualTo("12345");
            assertThat(copied.getAddress()).isEqualTo("서울시 강남구");
            assertThat(copied.getAddressDetail()).isEqualTo("테헤란로 123");
            assertThat(copied.getRequestMessage()).isNull();
        }

        @Test
        @DisplayName("원본 객체의 요청사항은 변경되지 않는다")
        void shouldNotModifyOriginalRequestMessage() {
            // given
            ShippingAddress original = ShippingAddress.of(
                    "홍길동", "01012345678", "12345", "주소", "상세주소", "요청사항"
            );

            // when
            original.withoutRequestMessage();

            // then
            assertThat(original.getRequestMessage()).isEqualTo("요청사항");
        }
    }
}
