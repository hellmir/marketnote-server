package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.domain.vendorcommunication.fassto.FasstoCustomerGoodsCodeEncoder;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery.FulfillmentDirectReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryItemCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FasstoDirectReturnDeliveryCommandToRequestMapper 테스트")
class FasstoDirectReturnDeliveryCommandToRequestMapperTest {

    private static RegisterFulfillmentDirectReturnDeliveryItemCommand buildItem(
            List<RegisterFulfillmentDeliveryGoodsCommand> products
    ) {
        return RegisterFulfillmentDirectReturnDeliveryItemCommand.builder()
                .orderDate("20260420")
                .supplierCode("SUP001")
                .originalCourierCode("CJ")
                .originalInvoiceNumber("INV001")
                .returnReceiveMethod("DIRECT")
                .recipientName("홍길동")
                .returnCourierCompany("CJ")
                .returnInvoiceNumber("RTN001")
                .returnType("01")
                .returnReason("단순변심")
                .returnDetailReason("사이즈 불일치")
                .remark("직접 반품")
                .products(products)
                .build();
    }

    @Nested
    @DisplayName("mapToRegisterRequest - 직접 반품 등록 productKey 우선/fallback")
    class MapToRegisterRequest {

        @Test
        @DisplayName("productKey가 있으면 22자 Base62 cstGodCd가 송신된다")
        void shouldEncodeProductKeyWhenPresent() {
            UUID productKey = UUID.randomUUID();
            RegisterFulfillmentDeliveryGoodsCommand goods = RegisterFulfillmentDeliveryGoodsCommand.of(
                    productKey, "GOD001", "20261231", 1
            );
            RegisterFulfillmentDirectReturnDeliveryCommand command = RegisterFulfillmentDirectReturnDeliveryCommand.of(
                    "CUST001", "token", List.of(buildItem(List.of(goods)))
            );

            FulfillmentDirectReturnDeliveryMapper mapper =
                    FasstoDirectReturnDeliveryCommandToRequestMapper.mapToRegisterRequest(command);

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
            RegisterFulfillmentDirectReturnDeliveryCommand command = RegisterFulfillmentDirectReturnDeliveryCommand.of(
                    "CUST001", "token", List.of(buildItem(List.of(goods)))
            );

            FulfillmentDirectReturnDeliveryMapper mapper =
                    FasstoDirectReturnDeliveryCommandToRequestMapper.mapToRegisterRequest(command);

            assertThat(mapper.toPayload().toString()).contains("GOD001");
        }
    }
}
