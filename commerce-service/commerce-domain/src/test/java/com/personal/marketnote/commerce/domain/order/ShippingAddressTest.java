package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
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
                    DeliveryRequestType.LEAVE_AT_DOOR,
                    null
            );

            // then
            assertThat(shippingAddress.getRecipientName()).isEqualTo("홍길동");
            assertThat(shippingAddress.getRecipientPhoneNumber()).isEqualTo("01012345678");
            assertThat(shippingAddress.getZipCode()).isEqualTo("12345");
            assertThat(shippingAddress.getAddress()).isEqualTo("서울시 강남구");
            assertThat(shippingAddress.getAddressDetail()).isEqualTo("테헤란로 123");
            assertThat(shippingAddress.getDeliveryRequestType()).isEqualTo(DeliveryRequestType.LEAVE_AT_DOOR);
            assertThat(shippingAddress.getDeliveryRequestMessage()).isNull();
        }

        @Test
        @DisplayName("배송 요청이 null이어도 정상 생성된다")
        void shouldCreateWithNullDeliveryRequest() {
            // given & when
            ShippingAddress shippingAddress = ShippingAddress.of(
                    "홍길동",
                    "01012345678",
                    "12345",
                    "서울시 강남구",
                    "테헤란로 123",
                    null,
                    null
            );

            // then
            assertThat(shippingAddress.getRecipientName()).isEqualTo("홍길동");
            assertThat(shippingAddress.getDeliveryRequestType()).isNull();
            assertThat(shippingAddress.getDeliveryRequestMessage()).isNull();
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
                    "홍길동", "01012345678", "12345", "주소", "상세주소", null, null
            );

            // when & then
            assertThat(shippingAddress.hasRecipientName()).isTrue();
        }

        @Test
        @DisplayName("수령인명이 null이면 false를 반환한다")
        void shouldReturnFalseWhenRecipientNameIsNull() {
            // given
            ShippingAddress shippingAddress = ShippingAddress.of(
                    null, null, null, null, null, null, null
            );

            // when & then
            assertThat(shippingAddress.hasRecipientName()).isFalse();
        }
    }

    @Nested
    @DisplayName("배송 요청 제외 복사 (withoutDeliveryRequest)")
    class WithoutDeliveryRequestTest {

        @Test
        @DisplayName("배송 요청을 제외하고 나머지 필드가 그대로 복사된다")
        void shouldCopyAllFieldsExceptDeliveryRequest() {
            // given
            ShippingAddress original = ShippingAddress.of(
                    "홍길동",
                    "01012345678",
                    "12345",
                    "서울시 강남구",
                    "테헤란로 123",
                    DeliveryRequestType.LEAVE_AT_DOOR,
                    null
            );

            // when
            ShippingAddress copied = original.withoutDeliveryRequest();

            // then
            assertThat(copied.getRecipientName()).isEqualTo("홍길동");
            assertThat(copied.getRecipientPhoneNumber()).isEqualTo("01012345678");
            assertThat(copied.getZipCode()).isEqualTo("12345");
            assertThat(copied.getAddress()).isEqualTo("서울시 강남구");
            assertThat(copied.getAddressDetail()).isEqualTo("테헤란로 123");
            assertThat(copied.getDeliveryRequestType()).isNull();
            assertThat(copied.getDeliveryRequestMessage()).isNull();
        }

        @Test
        @DisplayName("원본 객체의 배송 요청은 변경되지 않는다")
        void shouldNotModifyOriginalDeliveryRequest() {
            // given
            ShippingAddress original = ShippingAddress.of(
                    "홍길동", "01012345678", "12345", "주소", "상세주소",
                    DeliveryRequestType.CUSTOM, "요청사항"
            );

            // when
            original.withoutDeliveryRequest();

            // then
            assertThat(original.getDeliveryRequestType()).isEqualTo(DeliveryRequestType.CUSTOM);
            assertThat(original.getDeliveryRequestMessage()).isEqualTo("요청사항");
        }
    }
}
