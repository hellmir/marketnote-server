package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeOrderStatusCommandTest {

    @Nested
    @DisplayName("역할 기반 검증 메서드")
    class RoleBasedValidationTest {

        @Test
        @DisplayName("role이 BUYER이면 isBuyerRole()은 true를 반환한다")
        void isBuyerRole_buyer_returnsTrue() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.CANCEL_REQUESTED)
                    .role("BUYER")
                    .build();

            assertThat(command.isBuyerRole()).isTrue();
        }

        @Test
        @DisplayName("role이 ADMIN이면 isBuyerRole()은 false를 반환한다")
        void isBuyerRole_admin_returnsFalse() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PREPARING)
                    .role("ADMIN")
                    .build();

            assertThat(command.isBuyerRole()).isFalse();
        }

        @Test
        @DisplayName("role이 SELLER이면 isBuyerRole()은 false를 반환한다")
        void isBuyerRole_seller_returnsFalse() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PREPARING)
                    .role("SELLER")
                    .build();

            assertThat(command.isBuyerRole()).isFalse();
        }

        @Test
        @DisplayName("role이 null이면 isBuyerRole()은 false를 반환한다 (서비스 내부 호출)")
        void isBuyerRole_null_returnsFalse() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            assertThat(command.isBuyerRole()).isFalse();
        }

        @Test
        @DisplayName("role이 null이면 isInternalCall()은 true를 반환한다")
        void isInternalCall_nullRole_returnsTrue() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            assertThat(command.isInternalCall()).isTrue();
        }

        @Test
        @DisplayName("role이 존재하면 isInternalCall()은 false를 반환한다")
        void isInternalCall_withRole_returnsFalse() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.CANCEL_REQUESTED)
                    .role("BUYER")
                    .build();

            assertThat(command.isInternalCall()).isFalse();
        }
    }

    @Nested
    @DisplayName("buyerId 필드 검증")
    class BuyerIdFieldTest {

        @Test
        @DisplayName("buyerId를 포함하여 빌드하면 buyerId가 설정된다")
        void buildWithBuyerId_setsBuyerId() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.CANCEL_REQUESTED)
                    .role("BUYER")
                    .buyerId(100L)
                    .build();

            assertThat(command.buyerId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("buyerId 없이 빌드하면 buyerId가 null이다 (서비스 내부 호출)")
        void buildWithoutBuyerId_buyerIdIsNull() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            assertThat(command.buyerId()).isNull();
        }
    }

    @Nested
    @DisplayName("기존 기능 호환성 검증")
    class BackwardCompatibilityTest {

        @Test
        @DisplayName("role 없이 빌드해도 정상 동작한다 (기존 서비스 내부 호출 호환)")
        void buildWithoutRole_succeeds() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            assertThat(command.id()).isEqualTo(1L);
            assertThat(command.orderStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(command.role()).isNull();
            assertThat(command.buyerId()).isNull();
        }

        @Test
        @DisplayName("isPartialProductChange는 pricePolicyIds가 있으면 true를 반환한다")
        void isPartialProductChange_withPricePolicyIds_returnsTrue() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .pricePolicyIds(List.of(100L, 200L))
                    .orderStatus(OrderStatus.CONFIRMED)
                    .role("BUYER")
                    .build();

            assertThat(command.isPartialProductChange()).isTrue();
        }

        @Test
        @DisplayName("isPartialProductChange는 pricePolicyIds가 null이면 false를 반환한다")
        void isPartialProductChange_nullPricePolicyIds_returnsFalse() {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(1L)
                    .orderStatus(OrderStatus.CANCEL_REQUESTED)
                    .role("BUYER")
                    .build();

            assertThat(command.isPartialProductChange()).isFalse();
        }
    }
}
