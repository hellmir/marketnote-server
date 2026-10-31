package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.shop.FulfillmentShopMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.shop.FulfillmentShopQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentShopsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentShopCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FasstoShopCommandToRequestMapper 테스트")
class FasstoShopCommandToRequestMapperTest {

    @Nested
    @DisplayName("mapToRegisterRequest - 쇼핑몰 등록")
    class MapToRegisterRequest {

        @Test
        @DisplayName("쇼핑몰 필드가 FulfillmentShopMapper로 매핑되고 shopCd는 'registration'으로 고정된다")
        void shouldMapAllFields() {
            // given
            RegisterFulfillmentShopCommand command = RegisterFulfillmentShopCommand.of(
                    "CUST001",
                    "token",
                    "쇼핑몰명",
                    "CSTSHOP001",
                    "20260101",
                    "20261231",
                    "06000",
                    "서울시 강남구",
                    "101호",
                    "김대표",
                    "123-45-67890",
                    "02-1234-5678",
                    "하차방식",
                    "검수방식",
                    "Y",
                    "일반",
                    "담당자",
                    "직책",
                    "010-1111-2222",
                    "Y"
            );

            // when
            FulfillmentShopMapper mapper = FasstoShopCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            assertThat(mapper.getCustomerCode()).isEqualTo("CUST001");
            assertThat(mapper.getAccessToken()).isEqualTo("token");
            Map<String, Object> payload = mapper.toPayload();
            assertThat(payload)
                    .containsEntry("shopCd", "registration")
                    .containsEntry("shopNm", "쇼핑몰명")
                    .containsEntry("cstShopCd", "CSTSHOP001")
                    .containsEntry("dealStrDt", "20260101")
                    .containsEntry("dealEndDt", "20261231")
                    .containsEntry("zipNo", "06000")
                    .containsEntry("addr1", "서울시 강남구")
                    .containsEntry("addr2", "101호")
                    .containsEntry("ceoNm", "김대표")
                    .containsEntry("busNo", "123-45-67890")
                    .containsEntry("telNo", "02-1234-5678")
                    .containsEntry("unloadWay", "하차방식")
                    .containsEntry("checkWay", "검수방식")
                    .containsEntry("standYn", "Y")
                    .containsEntry("formType", "일반")
                    .containsEntry("empNm", "담당자")
                    .containsEntry("empPosit", "직책")
                    .containsEntry("empTelNo", "010-1111-2222")
                    .containsEntry("useYn", "Y");
        }

        @Test
        @DisplayName("선택 필드가 null이면 payload에서 제외된다")
        void shouldOmitNullOptionalFields() {
            // given
            RegisterFulfillmentShopCommand command = RegisterFulfillmentShopCommand.of(
                    "CUST001",
                    "token",
                    "쇼핑몰명",
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null
            );

            // when
            FulfillmentShopMapper mapper = FasstoShopCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            Map<String, Object> payload = mapper.toPayload();
            assertThat(payload).containsOnlyKeys("shopCd", "shopNm");
        }
    }

    @Nested
    @DisplayName("mapToUpdateRequest - 쇼핑몰 수정")
    class MapToUpdateRequest {

        @Test
        @DisplayName("shopCode가 shopCd로 매핑된다")
        void shouldMapShopCode() {
            // given
            UpdateFulfillmentShopCommand command = UpdateFulfillmentShopCommand.of(
                    "CUST001",
                    "token",
                    "SHOP001",
                    "쇼핑몰명",
                    "CSTSHOP001",
                    "20260101",
                    "20261231",
                    "06000",
                    "서울시 강남구",
                    "101호",
                    "김대표",
                    "123-45-67890",
                    "02-1234-5678",
                    "하차방식",
                    "검수방식",
                    "Y",
                    "일반",
                    "담당자",
                    "직책",
                    "010-1111-2222",
                    "Y"
            );

            // when
            FulfillmentShopMapper mapper = FasstoShopCommandToRequestMapper.mapToUpdateRequest(command);

            // then
            assertThat(mapper.toPayload()).containsEntry("shopCd", "SHOP001");
        }
    }

    @Nested
    @DisplayName("mapToShopsQuery - 쇼핑몰 목록 조회")
    class MapToShopsQuery {

        @Test
        @DisplayName("customerCode와 accessToken이 FulfillmentShopQuery로 매핑된다")
        void shouldMapBasicFields() {
            // given
            GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "token");

            // when
            FulfillmentShopQuery query = FasstoShopCommandToRequestMapper.mapToShopsQuery(command);

            // then
            assertThat(query.getCustomerCode()).isEqualTo("CUST001");
            assertThat(query.getAccessToken()).isEqualTo("token");
        }
    }
}
