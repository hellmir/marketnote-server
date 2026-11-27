package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.returndelivery.FulfillmentDirectReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.fassto.FasstoCustomerGoodsCodeEncoder;
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
    @DisplayName("mapToRegisterRequest - caller가 채운 fasstoProductCode 송신")
    class MapToRegisterRequest {

        @Test
        @DisplayName("caller가 채운 fasstoProductCode가 cstGodCd로 그대로 송신된다")
        void shouldSendCallerProvidedFasstoProductCode() {
            UUID productKey = UUID.randomUUID();
            String encoded = FasstoCustomerGoodsCodeEncoder.encode(productKey);
            RegisterFulfillmentDeliveryGoodsCommand goods = RegisterFulfillmentDeliveryGoodsCommand.of(
                productKey, "GOD001", encoded, "20261231", 1
            );
            RegisterFulfillmentDirectReturnDeliveryCommand command = RegisterFulfillmentDirectReturnDeliveryCommand.of(
                "CUST001", "token", List.of(buildItem(List.of(goods)))
            );

            FulfillmentDirectReturnDeliveryMapper mapper =
                FasstoDirectReturnDeliveryCommandToRequestMapper.mapToRegisterRequest(command);

            String payload = mapper.toPayload().toString();
            assertThat(payload).contains(encoded);
            assertThat(payload).doesNotContain("GOD001");
            assertThat(payload).doesNotContain(productKey.toString());
        }
    }
}
