package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.domain.vendorcommunication.fassto.FasstoCustomerGoodsCodeEncoder;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery.FulfillmentReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryItemCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FasstoReturnDeliveryCommandToRequestMapper 테스트")
class FasstoReturnDeliveryCommandToRequestMapperTest {

    private static RegisterFulfillmentReturnDeliveryItemCommand buildItem(List<RegisterFulfillmentDeliveryGoodsCommand> products) {
        return RegisterFulfillmentReturnDeliveryItemCommand.builder()
                .orderDate("20260420")
                .orderNumber("ORD001")
                .courierCode("CJ")
                .invoiceNumber("INV001")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .recipientAddress("서울시 강남구")
                .returnReceiverName("김철수")
                .returnReceiverPhoneNumber("010-8765-4321")
                .returnZipCode("06000")
                .returnAddress1("서울시 송파구")
                .returnAddress2("101동 1001호")
                .returnType("01")
                .returnReason("단순변심")
                .returnDetailReason("색상이 마음에 들지 않음")
                .returnShippingRequest("문 앞에 두세요")
                .products(products)
                .build();
    }

    @Nested
    @DisplayName("mapToRegisterRequest - 반품 등록 productKey 우선/fallback")
    class MapToRegisterRequest {

        @Test
        @DisplayName("productKey가 있으면 22자 Base62 cstGodCd가 송신된다")
        void shouldEncodeProductKeyWhenPresent() {
            UUID productKey = UUID.randomUUID();
            RegisterFulfillmentDeliveryGoodsCommand goods = RegisterFulfillmentDeliveryGoodsCommand.of(
                    productKey, "GOD001", "20261231", 1
            );
            RegisterFulfillmentReturnDeliveryCommand command = RegisterFulfillmentReturnDeliveryCommand.of(
                    "CUST001", "token", List.of(buildItem(List.of(goods)))
            );

            FulfillmentReturnDeliveryMapper mapper = FasstoReturnDeliveryCommandToRequestMapper.mapToRegisterRequest(command);

            String encoded = FasstoCustomerGoodsCodeEncoder.encode(productKey);
            String payload = mapper.toPayload().toString();
            assertThat(payload).contains(encoded);
            assertThat(payload).doesNotContain("GOD001");
            assertThat(payload).doesNotContain(productKey.toString());
        }

        @Test
        @DisplayName("productKey가 null이면 productCode가 그대로 송신된다 (인코딩 우회)")
        void shouldFallbackToProductCodeWhenProductKeyAbsent() {
            RegisterFulfillmentDeliveryGoodsCommand goods = RegisterFulfillmentDeliveryGoodsCommand.of(
                    "GOD001", "20261231", 1
            );
            RegisterFulfillmentReturnDeliveryCommand command = RegisterFulfillmentReturnDeliveryCommand.of(
                    "CUST001", "token", List.of(buildItem(List.of(goods)))
            );

            FulfillmentReturnDeliveryMapper mapper = FasstoReturnDeliveryCommandToRequestMapper.mapToRegisterRequest(command);

            assertThat(mapper.toPayload().toString()).contains("GOD001");
        }
    }
}
