package com.personal.marketnote.fulfillment.mapper;

import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistrationCreateState;
import com.personal.marketnote.fulfillment.domain.goods.FulfillmentGoodsRegistrationCreateState;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsItemCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FulfillmentCommandToStateMapper 테스트")
class FulfillmentCommandToStateMapperTest {

    @Nested
    @DisplayName("mapToGoodsRegistrationCreateState")
    class MapToGoodsRegistrationCreateState {

        @Test
        @DisplayName("command의 첫 goods productKey가 state에 매핑된다")
        void shouldMapProductKeyFromFirstGoods() {
            // given
            UUID productKey = UUID.randomUUID();
            RegisterFulfillmentGoodsItemCommand item = RegisterFulfillmentGoodsItemCommand.builder()
                    .productKey(productKey)
                    .productName("테스트 상품")
                    .productType("1")
                    .giftDivision("01")
                    .build();
            RegisterFulfillmentGoodsCommand command = RegisterFulfillmentGoodsCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentGoodsRegistrationCreateState state =
                    FulfillmentCommandToStateMapper.mapToGoodsRegistrationCreateState(command);

            // then
            assertThat(state.getProductKey()).isEqualTo(productKey);
        }

        @Test
        @DisplayName("command의 goods가 여러 개이면 첫 번째 goods의 productKey가 매핑된다")
        void shouldMapFirstGoodsProductKeyWhenMultipleGoods() {
            // given
            UUID firstProductKey = UUID.randomUUID();
            UUID secondProductKey = UUID.randomUUID();
            RegisterFulfillmentGoodsItemCommand firstItem = RegisterFulfillmentGoodsItemCommand.builder()
                    .productKey(firstProductKey)
                    .productName("상품1")
                    .productType("1")
                    .giftDivision("01")
                    .build();
            RegisterFulfillmentGoodsItemCommand secondItem = RegisterFulfillmentGoodsItemCommand.builder()
                    .productKey(secondProductKey)
                    .productName("상품2")
                    .productType("1")
                    .giftDivision("01")
                    .build();
            RegisterFulfillmentGoodsCommand command = RegisterFulfillmentGoodsCommand.of(
                    "CUST001", "token", List.of(firstItem, secondItem)
            );

            // when
            FulfillmentGoodsRegistrationCreateState state =
                    FulfillmentCommandToStateMapper.mapToGoodsRegistrationCreateState(command);

            // then
            assertThat(state.getProductKey()).isEqualTo(firstProductKey);
        }
    }

    @Nested
    @DisplayName("mapToDeliveryRegistrationCreateState")
    class MapToDeliveryRegistrationCreateState {

        @Test
        @DisplayName("command의 첫 deliveryRequest orderNumber가 orderId로 파싱되어 state에 매핑된다")
        void shouldParseOrderNumberToOrderId() {
            // given
            RegisterFulfillmentDeliveryItemCommand item = RegisterFulfillmentDeliveryItemCommand.builder()
                    .orderNumber("12345")
                    .recipientName("홍길동")
                    .build();
            RegisterFulfillmentDeliveryCommand command = RegisterFulfillmentDeliveryCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentDeliveryRegistrationCreateState state =
                    FulfillmentCommandToStateMapper.mapToDeliveryRegistrationCreateState(command);

            // then
            assertThat(state.getOrderId()).isEqualTo(12345L);
        }
    }
}
