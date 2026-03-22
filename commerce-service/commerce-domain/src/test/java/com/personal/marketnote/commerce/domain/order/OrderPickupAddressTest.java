package com.personal.marketnote.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderPickupAddressTest {

    @Nested
    @DisplayName("회수지 주소 적용 (applyPickupAddress)")
    class ApplyPickupAddressTest {

        @Test
        @DisplayName("회수지 주소가 입력되면 입력값이 그대로 적용된다")
        void shouldApplyProvidedPickupAddress() {
            // given
            Order order = createOrderWithDeliveryAddress();

            // when
            order.applyPickupAddress(
                    ShippingAddress.of(
                            "회수 수령인",
                            "01099998888",
                            "54321",
                            "회수지 주소",
                            "회수지 상세주소",
                            "부재시 경비실에 맡겨주세요"
                    )
            );

            // then
            assertThat(order.getPickupAddress().getRecipientName()).isEqualTo("회수 수령인");
            assertThat(order.getPickupAddress().getRecipientPhoneNumber()).isEqualTo("01099998888");
            assertThat(order.getPickupAddress().getZipCode()).isEqualTo("54321");
            assertThat(order.getPickupAddress().getAddress()).isEqualTo("회수지 주소");
            assertThat(order.getPickupAddress().getAddressDetail()).isEqualTo("회수지 상세주소");
            assertThat(order.getPickupAddress().getRequestMessage()).isEqualTo("부재시 경비실에 맡겨주세요");
        }

        @Test
        @DisplayName("회수지 주소가 미입력이면 배송지 주소가 기본값으로 복사된다")
        void shouldCopyDeliveryAddressWhenPickupAddressNotProvided() {
            // given
            Order order = createOrderWithDeliveryAddress();

            // when
            order.applyPickupAddress(null);

            // then
            assertThat(order.getPickupAddress().getRecipientName()).isEqualTo("배송 수령인");
            assertThat(order.getPickupAddress().getRecipientPhoneNumber()).isEqualTo("01012345678");
            assertThat(order.getPickupAddress().getZipCode()).isEqualTo("12345");
            assertThat(order.getPickupAddress().getAddress()).isEqualTo("서울시 강남구");
            assertThat(order.getPickupAddress().getAddressDetail()).isEqualTo("테헤란로 123");
        }

        @Test
        @DisplayName("회수지 미입력 시 배송 요청사항은 회수지 요청사항으로 복사되지 않는다")
        void shouldNotCopyDeliveryRequestMessageToPickupRequestMessage() {
            // given
            Order order = createOrderWithDeliveryAddress();

            // when
            order.applyPickupAddress(null);

            // then
            assertThat(order.getPickupAddress().getRequestMessage()).isNull();
        }

        @Test
        @DisplayName("회수지 주소 입력 시 배송지 주소는 변경되지 않는다")
        void shouldNotModifyDeliveryAddressWhenPickupAddressProvided() {
            // given
            Order order = createOrderWithDeliveryAddress();

            // when
            order.applyPickupAddress(
                    ShippingAddress.of(
                            "회수 수령인",
                            "01099998888",
                            "54321",
                            "회수지 주소",
                            "회수지 상세주소",
                            "회수 요청사항"
                    )
            );

            // then
            assertThat(order.getShippingAddress().getRecipientName()).isEqualTo("배송 수령인");
            assertThat(order.getShippingAddress().getRecipientPhoneNumber()).isEqualTo("01012345678");
            assertThat(order.getShippingAddress().getZipCode()).isEqualTo("12345");
            assertThat(order.getShippingAddress().getAddress()).isEqualTo("서울시 강남구");
            assertThat(order.getShippingAddress().getAddressDetail()).isEqualTo("테헤란로 123");
            assertThat(order.getShippingAddress().getRequestMessage()).isEqualTo("문 앞에 놓아주세요");
        }

        @Test
        @DisplayName("DB에서 복원한 주문의 회수지 주소가 정상적으로 매핑된다")
        void shouldRestorePickupAddressFromSnapshot() {
            // given & when
            Order order = Order.from(OrderSnapshotState.builder()
                    .id(1L)
                    .buyerId(1L)
                    .orderKey(UUID.randomUUID())
                    .orderNumber("ORD-1")
                    .orderStatus(OrderStatus.REFUND_REQUESTED)
                    .amount(OrderAmount.of(50000L, null, null, null, null))
                    .shippingAddress(ShippingAddress.of("배송 수령인", "01012345678", "12345", "서울시 강남구", "테헤란로 123", null))
                    .pickupAddress(ShippingAddress.of("회수 수령인", "01099998888", "54321", "회수지 주소", "회수지 상세주소", "회수 요청사항"))
                    .orderProductStates(List.of())
                    .createdAt(LocalDateTime.now())
                    .modifiedAt(LocalDateTime.now())
                    .build());

            // then
            assertThat(order.getPickupAddress().getRecipientName()).isEqualTo("회수 수령인");
            assertThat(order.getPickupAddress().getRecipientPhoneNumber()).isEqualTo("01099998888");
            assertThat(order.getPickupAddress().getZipCode()).isEqualTo("54321");
            assertThat(order.getPickupAddress().getAddress()).isEqualTo("회수지 주소");
            assertThat(order.getPickupAddress().getAddressDetail()).isEqualTo("회수지 상세주소");
            assertThat(order.getPickupAddress().getRequestMessage()).isEqualTo("회수 요청사항");
        }
    }

    private Order createOrderWithDeliveryAddress() {
        return Order.from(OrderSnapshotState.builder()
                .id(1L)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-1")
                .orderStatus(OrderStatus.DELIVERED)
                .amount(OrderAmount.of(50000L, null, null, null, null))
                .shippingAddress(ShippingAddress.of("배송 수령인", "01012345678", "12345", "서울시 강남구", "테헤란로 123", "문 앞에 놓아주세요"))
                .orderProductStates(List.of(
                        OrderProductSnapshotState.builder()
                                .orderId(1L)
                                .sellerId(10L)
                                .pricePolicyId(100L)
                                .quantity(1)
                                .unitAmount(50000L)
                                .orderStatus(OrderStatus.DELIVERED)
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
