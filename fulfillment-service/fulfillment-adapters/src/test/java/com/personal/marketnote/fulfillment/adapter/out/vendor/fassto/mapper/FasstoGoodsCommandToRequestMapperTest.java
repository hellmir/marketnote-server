package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods.FulfillmentGoodsDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods.FulfillmentGoodsElementQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods.FulfillmentGoodsMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods.FulfillmentGoodsQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsElementsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsItemCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FasstoGoodsCommandToRequestMapper 테스트")
class FasstoGoodsCommandToRequestMapperTest {

    private static RegisterFulfillmentGoodsItemCommand buildRegisterItem(UUID productKey, String productCode) {
        return RegisterFulfillmentGoodsItemCommand.builder()
                .productKey(productKey)
                .productCode(productCode)
                .productName("상품명")
                .productType("TYPE01")
                .giftDivision("N")
                .productOptionCode1("OPT1")
                .productOptionCode2("OPT2")
                .inventoryProductNameUseYn("Y")
                .inventoryProductName("재고명")
                .supplierCode("SUP001")
                .categoryCode("CAT01")
                .seasonCode("SE01")
                .genderCode("G01")
                .manufactureYear("2026")
                .productPrice("10000")
                .inboundPrice("8000")
                .salePrice("9000")
                .dealTemperature("상온")
                .pickFactor("1")
                .productBarcode("BAR001")
                .boxWeight("1.0")
                .origin("KR")
                .expirationManagementYn("N")
                .shelfLifeDays("365")
                .outboundCancelDays("30")
                .inboundCancelDays("30")
                .boxDivision("BOX01")
                .bufferProductYn("N")
                .loadingDirection("UP")
                .subMaterial("NONE")
                .useYn("Y")
                .safetyStock("10")
                .feeYn("Y")
                .saleUnitQuantity("1")
                .customerProductImageUrl("http://example.com/img1.jpg")
                .externalProductImageUrl("http://example.com/img2.jpg")
                .build();
    }

    private static UpdateFulfillmentGoodsItemCommand buildUpdateItem(UUID productKey, String productCode) {
        return UpdateFulfillmentGoodsItemCommand.of(
                productKey,
                productCode,
                "상품명",
                "TYPE01",
                "N",
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null
        );
    }

    @Nested
    @DisplayName("mapToRegisterRequest - 상품 등록")
    class MapToRegisterRequest {

        @Test
        @DisplayName("productKey가 있으면 UUID가 cstGodCd로 매핑된다")
        void shouldPreferProductKey() {
            // given
            UUID productKey = UUID.randomUUID();
            RegisterFulfillmentGoodsItemCommand item = buildRegisterItem(productKey, "GOD001");
            RegisterFulfillmentGoodsCommand command = RegisterFulfillmentGoodsCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentGoodsMapper mapper = FasstoGoodsCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            assertThat(mapper.getProductKey()).isEqualTo(productKey.toString());
            String payload = mapper.toPayload().toString();
            assertThat(payload).contains(productKey.toString());
            assertThat(payload).doesNotContain("GOD001");
        }

        @Test
        @DisplayName("productKey가 null이면 productCode가 cstGodCd로 폴백된다")
        void shouldFallbackToProductCode() {
            // given
            RegisterFulfillmentGoodsItemCommand item = buildRegisterItem(null, "GOD001");
            RegisterFulfillmentGoodsCommand command = RegisterFulfillmentGoodsCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentGoodsMapper mapper = FasstoGoodsCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            assertThat(mapper.getProductKey()).isEqualTo("GOD001");
        }

        @Test
        @DisplayName("상품 기본 필드와 옵션 필드가 payload로 매핑된다")
        void shouldMapAllItemFields() {
            // given
            UUID productKey = UUID.randomUUID();
            RegisterFulfillmentGoodsItemCommand item = buildRegisterItem(productKey, null);
            RegisterFulfillmentGoodsCommand command = RegisterFulfillmentGoodsCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentGoodsMapper mapper = FasstoGoodsCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            Map<String, Object> payload = mapper.toPayload().getFirst();
            assertThat(payload)
                    .containsEntry("cstGodCd", productKey.toString())
                    .containsEntry("godNm", "상품명")
                    .containsEntry("godType", "TYPE01")
                    .containsEntry("giftDiv", "N")
                    .containsEntry("godOptCd1", "OPT1")
                    .containsEntry("godOptCd2", "OPT2")
                    .containsEntry("invGodNmUseYn", "Y")
                    .containsEntry("invGodNm", "재고명")
                    .containsEntry("supCd", "SUP001")
                    .containsEntry("cateCd", "CAT01")
                    .containsEntry("seasonCd", "SE01")
                    .containsEntry("genderCd", "G01")
                    .containsEntry("makeYr", "2026")
                    .containsEntry("godPr", "10000")
                    .containsEntry("inPr", "8000")
                    .containsEntry("salPr", "9000")
                    .containsEntry("dealTemp", "상온")
                    .containsEntry("pickFac", "1")
                    .containsEntry("godBarcd", "BAR001")
                    .containsEntry("boxWeight", "1.0")
                    .containsEntry("origin", "KR")
                    .containsEntry("distTermMgtYn", "N")
                    .containsEntry("useTermDay", "365")
                    .containsEntry("outCanDay", "30")
                    .containsEntry("inCanDay", "30")
                    .containsEntry("boxDiv", "BOX01")
                    .containsEntry("bufGodYn", "N")
                    .containsEntry("loadingDirection", "UP")
                    .containsEntry("subMate", "NONE")
                    .containsEntry("useYn", "Y")
                    .containsEntry("safetyStock", "10")
                    .containsEntry("feeYn", "Y")
                    .containsEntry("saleUnitQty", "1")
                    .containsEntry("cstGodImgUrl", "http://example.com/img1.jpg")
                    .containsEntry("externalGodImgUrl", "http://example.com/img2.jpg");
        }

        @Test
        @DisplayName("여러 상품이 리스트로 매핑된다")
        void shouldMapMultipleItems() {
            // given
            UUID productKey1 = UUID.randomUUID();
            UUID productKey2 = UUID.randomUUID();
            RegisterFulfillmentGoodsItemCommand item1 = buildRegisterItem(productKey1, null);
            RegisterFulfillmentGoodsItemCommand item2 = buildRegisterItem(productKey2, null);
            RegisterFulfillmentGoodsCommand command = RegisterFulfillmentGoodsCommand.of(
                    "CUST001", "token", List.of(item1, item2)
            );

            // when
            FulfillmentGoodsMapper mapper = FasstoGoodsCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            assertThat(mapper.toPayload()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("mapToUpdateRequest - 상품 수정")
    class MapToUpdateRequest {

        @Test
        @DisplayName("productKey가 있으면 UUID가 cstGodCd로 매핑된다")
        void shouldPreferProductKey() {
            // given
            UUID productKey = UUID.randomUUID();
            UpdateFulfillmentGoodsItemCommand item = buildUpdateItem(productKey, "GOD001");
            UpdateFulfillmentGoodsCommand command = UpdateFulfillmentGoodsCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentGoodsMapper mapper = FasstoGoodsCommandToRequestMapper.mapToUpdateRequest(command);

            // then
            String payload = mapper.toPayload().toString();
            assertThat(payload).contains(productKey.toString());
            assertThat(payload).doesNotContain("GOD001");
        }

        @Test
        @DisplayName("productKey가 null이면 productCode가 cstGodCd로 폴백된다")
        void shouldFallbackToProductCode() {
            // given
            UpdateFulfillmentGoodsItemCommand item = buildUpdateItem(null, "GOD001");
            UpdateFulfillmentGoodsCommand command = UpdateFulfillmentGoodsCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentGoodsMapper mapper = FasstoGoodsCommandToRequestMapper.mapToUpdateRequest(command);

            // then
            assertThat(mapper.toPayload().toString()).contains("GOD001");
        }
    }

    @Nested
    @DisplayName("mapToGoodsQuery - 상품 목록 조회")
    class MapToGoodsQuery {

        @Test
        @DisplayName("customerCode와 accessToken이 FulfillmentGoodsQuery로 매핑된다")
        void shouldMapBasicFields() {
            // given
            GetFulfillmentGoodsCommand command = GetFulfillmentGoodsCommand.of("CUST001", "token");

            // when
            FulfillmentGoodsQuery query = FasstoGoodsCommandToRequestMapper.mapToGoodsQuery(command);

            // then
            assertThat(query.getCustomerCode()).isEqualTo("CUST001");
            assertThat(query.getAccessToken()).isEqualTo("token");
        }
    }

    @Nested
    @DisplayName("mapToGoodsDetailQuery - 상품 상세 조회")
    class MapToGoodsDetailQuery {

        @Test
        @DisplayName("productName이 godNm으로 매핑된다")
        void shouldMapProductName() {
            // given
            GetFulfillmentGoodsDetailCommand command = GetFulfillmentGoodsDetailCommand.of(
                    "CUST001", "token", "상품명"
            );

            // when
            FulfillmentGoodsDetailQuery query = FasstoGoodsCommandToRequestMapper.mapToGoodsDetailQuery(command);

            // then
            assertThat(query.getCustomerCode()).isEqualTo("CUST001");
            assertThat(query.getAccessToken()).isEqualTo("token");
            assertThat(query.getGodNm()).isEqualTo("상품명");
        }
    }

    @Nested
    @DisplayName("mapToGoodsElementsQuery - 상품 요소 조회")
    class MapToGoodsElementsQuery {

        @Test
        @DisplayName("customerCode와 accessToken이 FulfillmentGoodsElementQuery로 매핑된다")
        void shouldMapBasicFields() {
            // given
            GetFulfillmentGoodsElementsCommand command = GetFulfillmentGoodsElementsCommand.of("CUST001", "token");

            // when
            FulfillmentGoodsElementQuery query = FasstoGoodsCommandToRequestMapper.mapToGoodsElementsQuery(command);

            // then
            assertThat(query.getCustomerCode()).isEqualTo("CUST001");
            assertThat(query.getAccessToken()).isEqualTo("token");
        }
    }
}
