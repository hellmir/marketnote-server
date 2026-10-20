package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.warehousing.FulfillmentWarehousingAbnormalImageQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.warehousing.FulfillmentWarehousingMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingAbnormalImageCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentWarehousingGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentWarehousingItemCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FasstoWarehousingCommandToRequestMapper 테스트")
class FasstoWarehousingCommandToRequestMapperTest {

    @Nested
    @DisplayName("mapToRegisterRequest - productKey 우선/fallback")
    class MapToRegisterRequest {

        @Test
        @DisplayName("productKey가 있으면 UUID가 cstGodCd로 전송된다")
        void shouldSendProductKeyWhenPresent() {
            // given
            UUID productKey = UUID.randomUUID();
            RegisterFulfillmentWarehousingGoodsCommand goods = RegisterFulfillmentWarehousingGoodsCommand.of(
                    productKey, "GOD001", "20260420", 10
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
            assertThat(mapper.toPayload().toString()).contains(productKey.toString());
            assertThat(mapper.toPayload().toString()).doesNotContain("GOD001");
        }

        @Test
        @DisplayName("productKey가 null이면 productCode가 cstGodCd로 전송된다")
        void shouldFallbackToProductCodeWhenProductKeyAbsent() {
            // given
            RegisterFulfillmentWarehousingGoodsCommand goods = RegisterFulfillmentWarehousingGoodsCommand.of(
                    "GOD001", "20260420", 10
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
            assertThat(mapper.toPayload().toString()).contains("GOD001");
        }
    }

    @Nested
    @DisplayName("mapToUpdateRequest - productKey 우선/fallback")
    class MapToUpdateRequest {

        @Test
        @DisplayName("productKey가 있으면 UUID가 cstGodCd로 전송된다")
        void shouldSendProductKeyWhenPresent() {
            // given
            UUID productKey = UUID.randomUUID();
            UpdateFulfillmentWarehousingGoodsCommand goods = UpdateFulfillmentWarehousingGoodsCommand.of(
                    productKey, "GOD001", "20260420", 10
            );
            UpdateFulfillmentWarehousingItemCommand item = UpdateFulfillmentWarehousingItemCommand.of(
                    "20260420", "ORD001", "NORMAL", "SLIP001", null, null, null, null, null, null, null, List.of(goods)
            );
            UpdateFulfillmentWarehousingCommand command = UpdateFulfillmentWarehousingCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentWarehousingMapper mapper = FasstoWarehousingCommandToRequestMapper.mapToUpdateRequest(command);

            // then
            assertThat(mapper.toPayload().toString()).contains(productKey.toString());
            assertThat(mapper.toPayload().toString()).doesNotContain("GOD001");
        }

        @Test
        @DisplayName("productKey가 null이면 productCode가 cstGodCd로 전송된다")
        void shouldFallbackToProductCodeWhenProductKeyAbsent() {
            // given
            UpdateFulfillmentWarehousingGoodsCommand goods = UpdateFulfillmentWarehousingGoodsCommand.of(
                    "GOD001", "20260420", 10
            );
            UpdateFulfillmentWarehousingItemCommand item = UpdateFulfillmentWarehousingItemCommand.of(
                    "20260420", "ORD001", "NORMAL", "SLIP001", null, null, null, null, null, null, null, List.of(goods)
            );
            UpdateFulfillmentWarehousingCommand command = UpdateFulfillmentWarehousingCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentWarehousingMapper mapper = FasstoWarehousingCommandToRequestMapper.mapToUpdateRequest(command);

            // then
            assertThat(mapper.toPayload().toString()).contains("GOD001");
        }
    }

    @Nested
    @DisplayName("mapToAbnormalImageQuery - productKey 우선/fallback")
    class MapToAbnormalImageQuery {

        @Test
        @DisplayName("productKey가 있으면 UUID가 godCd로 전송된다")
        void shouldSendProductKeyWhenPresent() {
            // given
            UUID productKey = UUID.randomUUID();
            GetFulfillmentWarehousingAbnormalImageCommand command =
                    GetFulfillmentWarehousingAbnormalImageCommand.of(
                            "token", "SLIP001", productKey, "GOD001", "SN001", "1", "IMG001"
                    );

            // when
            FulfillmentWarehousingAbnormalImageQuery query =
                    FasstoWarehousingCommandToRequestMapper.mapToAbnormalImageQuery(command);

            // then
            assertThat(query.getGodCd()).isEqualTo(productKey.toString());
        }

        @Test
        @DisplayName("productKey가 null이면 productCode가 godCd로 전송된다")
        void shouldFallbackToProductCodeWhenProductKeyAbsent() {
            // given
            GetFulfillmentWarehousingAbnormalImageCommand command =
                    GetFulfillmentWarehousingAbnormalImageCommand.of(
                            "token", "SLIP001", "GOD001", "SN001", "1", "IMG001"
                    );

            // when
            FulfillmentWarehousingAbnormalImageQuery query =
                    FasstoWarehousingCommandToRequestMapper.mapToAbnormalImageQuery(command);

            // then
            assertThat(query.getGodCd()).isEqualTo("GOD001");
        }
    }
}
