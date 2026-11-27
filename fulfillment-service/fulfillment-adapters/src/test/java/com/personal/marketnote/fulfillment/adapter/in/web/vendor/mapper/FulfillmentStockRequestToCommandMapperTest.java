package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FulfillmentStockRequestToCommandMapper 테스트")
class FulfillmentStockRequestToCommandMapperTest {

    @Test
    @DisplayName("mapToStockDetailCommand는 customerProductCode를 fasstoProductCode에도 채운다")
    void shouldFillFasstoProductCodeFromCustomerProductCode() {
        GetFulfillmentStockDetailCommand command = FulfillmentStockRequestToCommandMapper.mapToStockDetailCommand(
            "CUST001", "token", "GOD001", "Y"
        );

        assertThat(command.customerProductCode()).isEqualTo("GOD001");
        assertThat(command.fasstoProductCode()).isEqualTo("GOD001");
        assertThat(command.productKey()).isNull();
        assertThat(command.outOfStockYn()).isEqualTo("Y");
    }
}
