package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.domain.vendorcommunication.fassto.FasstoCustomerGoodsCodeEncoder;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.warehousing.FulfillmentWarehousingAbnormalImageQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.warehousing.FulfillmentWarehousingMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FasstoWarehousingCommandToRequestMapper 테스트")
class FasstoWarehousingCommandToRequestMapperTest {

    @Nested
    @DisplayName("mapToRegisterRequest - caller가 채운 fasstoProductCode 송신")
    class MapToRegisterRequest {

        @Test
        @DisplayName("caller가 채운 fasstoProductCode가 그대로 cstGodCd로 송신된다")
        void shouldSendCallerProvidedFasstoProductCode() {
            // given
            UUID productKey = UUID.randomUUID();
            String encoded = FasstoCustomerGoodsCodeEncoder.encode(productKey);
            RegisterFulfillmentWarehousingGoodsCommand goods = RegisterFulfillmentWarehousingGoodsCommand.of(
                    productKey, "GOD001", encoded, "20260420", 10
            );
            RegisterFulfillmentWarehousingItemCommand item = RegisterFulfillmentWarehousingItemCommand.builder()
                    .orderDate("20260420")
                    .orderNumber("ORD001")
                    .warehousingMethod("NORMAL")
                    .products(List.of(goods))
                    .build();
            RegisterFulfillmentWarehousingCommand command = RegisterFulfillmentWarehousingCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentWarehousingMapper mapper = FasstoWarehousingCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            assertThat(mapper.toPayload().toString()).contains(encoded);
            assertThat(mapper.toPayload().toString()).doesNotContain("GOD001");
            assertThat(mapper.toPayload().toString()).doesNotContain(productKey.toString());
        }
    }

    @Nested
    @DisplayName("mapToUpdateRequest - caller가 채운 fasstoProductCode 송신")
    class MapToUpdateRequest {

        @Test
        @DisplayName("caller가 채운 fasstoProductCode가 그대로 cstGodCd로 송신된다")
        void shouldSendCallerProvidedFasstoProductCode() {
            // given
            UUID productKey = UUID.randomUUID();
            String encoded = FasstoCustomerGoodsCodeEncoder.encode(productKey);
            UpdateFulfillmentWarehousingGoodsCommand goods = UpdateFulfillmentWarehousingGoodsCommand.of(
                    productKey, "GOD001", encoded, "20260420", 10
            );
            UpdateFulfillmentWarehousingItemCommand item = UpdateFulfillmentWarehousingItemCommand.builder()
                    .orderDate("20260420")
                    .orderNumber("ORD001")
                    .warehousingMethod("NORMAL")
                    .slipNumber("SLIP001")
                    .products(List.of(goods))
                    .build();
            UpdateFulfillmentWarehousingCommand command = UpdateFulfillmentWarehousingCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentWarehousingMapper mapper = FasstoWarehousingCommandToRequestMapper.mapToUpdateRequest(command);

            // then
            assertThat(mapper.toPayload().toString()).contains(encoded);
            assertThat(mapper.toPayload().toString()).doesNotContain("GOD001");
            assertThat(mapper.toPayload().toString()).doesNotContain(productKey.toString());
        }
    }

    @Nested
    @DisplayName("mapToAbnormalImageQuery - caller가 채운 fasstoProductCode 송신")
    class MapToAbnormalImageQuery {

        @Test
        @DisplayName("caller가 채운 fasstoProductCode가 그대로 godCd로 송신된다")
        void shouldSendCallerProvidedFasstoProductCode() {
            // given
            UUID productKey = UUID.randomUUID();
            String encoded = FasstoCustomerGoodsCodeEncoder.encode(productKey);
            GetFulfillmentWarehousingAbnormalImageCommand command =
                    GetFulfillmentWarehousingAbnormalImageCommand.builder()
                            .accessToken("token")
                            .slipNumber("SLIP001")
                            .productKey(productKey)
                            .productCode("GOD001")
                            .fasstoProductCode(encoded)
                            .goodsSerialNo("SN001")
                            .fileSeq("1")
                            .imageNumber("IMG001")
                            .build();

            // when
            FulfillmentWarehousingAbnormalImageQuery query =
                    FasstoWarehousingCommandToRequestMapper.mapToAbnormalImageQuery(command);

            // then
            assertThat(query.getGodCd()).isEqualTo(encoded);
        }
    }
}
