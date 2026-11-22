package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.domain.vendorcommunication.fassto.FasstoCustomerGoodsCodeEncoder;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.stock.FulfillmentStockDetailQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FasstoStockCommandToRequestMapper 테스트")
class FasstoStockCommandToRequestMapperTest {

    @Nested
    @DisplayName("mapToDetailQuery - 재고 상세 조회 productKey 우선/fallback")
    class MapToDetailQuery {

        @Test
        @DisplayName("caller가 채운 fasstoProductCode가 그대로 cstGodCd로 송신된다")
        void shouldSendCallerProvidedFasstoProductCode() {
            UUID productKey = UUID.randomUUID();
            String encoded = FasstoCustomerGoodsCodeEncoder.encode(productKey);
            GetFulfillmentStockDetailCommand command = GetFulfillmentStockDetailCommand.forProductKey(
                    "CUST001", "token", productKey, encoded, "N"
            );

            FulfillmentStockDetailQuery query = FasstoStockCommandToRequestMapper.mapToDetailQuery(command);

            assertThat(query.getCstGodCd()).isEqualTo(encoded).hasSize(22);
            assertThat(query.getCstGodCd()).isNotEqualTo(productKey.toString());
        }

        @Test
        @DisplayName("productKey가 null이면 customerProductCode가 그대로 송신된다 (인코딩 우회)")
        void shouldFallbackToCustomerProductCodeWhenProductKeyAbsent() {
            GetFulfillmentStockDetailCommand command = GetFulfillmentStockDetailCommand.of(
                    "CUST001", "token", "GOD001", "N"
            );

            FulfillmentStockDetailQuery query = FasstoStockCommandToRequestMapper.mapToDetailQuery(command);

            assertThat(query.getCstGodCd()).isEqualTo("GOD001");
        }
    }
}
