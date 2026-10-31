package com.personal.marketnote.product.adapter.in.web.product.mapper;

import com.personal.marketnote.product.adapter.in.web.cart.request.GetMyOrderingProductsRequest;
import com.personal.marketnote.product.adapter.in.web.cart.request.OrderingItemRequest;
import com.personal.marketnote.product.adapter.in.web.category.request.RegisterProductCategoriesRequest;
import com.personal.marketnote.product.adapter.in.web.option.request.RegisterProductOptionRequest;
import com.personal.marketnote.product.adapter.in.web.option.request.UpdateProductOptionsRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.RegisterProductFulfillmentVendorGoodsRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.RegisterProductRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.ReorderProductTagsRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.UpdateProductFulfillmentVendorGoodsRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.UpdateProductRequest;
import com.personal.marketnote.product.port.in.command.FulfillmentVendorGoodsOptionCommand;
import com.personal.marketnote.product.port.in.command.GetMyOrderingProductsQuery;
import com.personal.marketnote.product.port.in.command.RegisterProductCategoriesCommand;
import com.personal.marketnote.product.port.in.command.RegisterProductCommand;
import com.personal.marketnote.product.port.in.command.RegisterProductOptionsCommand;
import com.personal.marketnote.product.port.in.command.ReorderProductTagsCommand;
import com.personal.marketnote.product.port.in.command.UpdateProductCommand;
import com.personal.marketnote.product.port.in.command.UpdateProductOptionsCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRequestToCommandMapperTest {

    @Nested
    @DisplayName("RegisterProductRequest → RegisterProductCommand")
    class RegisterProductRequestMap {

        @Test
        @DisplayName("필드를 그대로 매핑하고 fulfillmentVendorGoods가 null이면 null로 매핑한다")
        void mapsFieldsIncludingNullFulfillment() {
            RegisterProductRequest request = new RegisterProductRequest(
                    10L,
                    "상품명",
                    "브랜드",
                    "상세",
                    10_000L,
                    8_000L,
                    100L,
                    true,
                    List.of("tag1", "tag2"),
                    null
            );

            RegisterProductCommand command = ProductRequestToCommandMapper.mapToCommand(request);

            assertThat(command.sellerId()).isEqualTo(10L);
            assertThat(command.name()).isEqualTo("상품명");
            assertThat(command.brandName()).isEqualTo("브랜드");
            assertThat(command.detail()).isEqualTo("상세");
            assertThat(command.price()).isEqualTo(10_000L);
            assertThat(command.discountPrice()).isEqualTo(8_000L);
            assertThat(command.accumulatedPoint()).isEqualTo(100L);
            assertThat(command.isFindAllOptions()).isTrue();
            assertThat(command.tags()).containsExactly("tag1", "tag2");
            assertThat(command.fulfillmentVendorGoods()).isNull();
        }
    }

    @Nested
    @DisplayName("RegisterProductCategoriesRequest → RegisterProductCategoriesCommand")
    class RegisterProductCategoriesRequestMap {

        @Test
        @DisplayName("productId와 categoryIds를 매핑한다")
        void mapsProductIdAndCategoryIds() {
            RegisterProductCategoriesRequest request = new RegisterProductCategoriesRequest();
            ReflectionTestUtils.setField(request, "categoryIds", List.of(1L, 2L));

            RegisterProductCategoriesCommand command = ProductRequestToCommandMapper.mapToCommand(100L, request);

            assertThat(command.productId()).isEqualTo(100L);
            assertThat(command.categoryIds()).containsExactly(1L, 2L);
        }
    }

    @Nested
    @DisplayName("UpdateProductOptionsRequest → RegisterProductOptionsCommand")
    class RegisterProductOptionsCommandMap {

        @Test
        @DisplayName("options를 OptionItem 리스트로 매핑한다")
        void mapsOptionsToOptionItems() {
            UpdateProductOptionsRequest request = new UpdateProductOptionsRequest();
            RegisterProductOptionRequest option = new RegisterProductOptionRequest();
            ReflectionTestUtils.setField(option, "content", "레드");
            ReflectionTestUtils.setField(request, "categoryName", "색상");
            ReflectionTestUtils.setField(request, "options", List.of(option));

            RegisterProductOptionsCommand command = ProductRequestToCommandMapper.mapToCommand(100L, request);

            assertThat(command.productId()).isEqualTo(100L);
            assertThat(command.categoryName()).isEqualTo("색상");
            assertThat(command.options()).hasSize(1);
            assertThat(command.options().getFirst().content()).isEqualTo("레드");
        }
    }

    @Nested
    @DisplayName("UpdateProductOptionsRequest → UpdateProductOptionsCommand")
    class UpdateProductOptionsCommandMap {

        @Test
        @DisplayName("productId/optionCategoryId/categoryName/options를 매핑한다")
        void mapsUpdateCommand() {
            UpdateProductOptionsRequest request = new UpdateProductOptionsRequest();
            RegisterProductOptionRequest option = new RegisterProductOptionRequest();
            ReflectionTestUtils.setField(option, "content", "블루");
            ReflectionTestUtils.setField(request, "categoryName", "색상");
            ReflectionTestUtils.setField(request, "options", List.of(option));

            UpdateProductOptionsCommand command = ProductRequestToCommandMapper.mapToUpdateCommand(100L, 7L, request);

            assertThat(command.productId()).isEqualTo(100L);
            assertThat(command.optionCategoryId()).isEqualTo(7L);
            assertThat(command.categoryName()).isEqualTo("색상");
            assertThat(command.options()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("UpdateProductRequest → UpdateProductCommand")
    class UpdateProductCommandMap {

        @Test
        @DisplayName("id와 요청 필드를 매핑한다")
        void mapsUpdateProductCommand() {
            UpdateProductRequest request = new UpdateProductRequest(
                    "상품명",
                    "브랜드",
                    "상세",
                    false,
                    List.of("tag1"),
                    null
            );

            UpdateProductCommand command = ProductRequestToCommandMapper.mapToCommand(100L, request);

            assertThat(command.id()).isEqualTo(100L);
            assertThat(command.name()).isEqualTo("상품명");
            assertThat(command.brandName()).isEqualTo("브랜드");
            assertThat(command.detail()).isEqualTo("상세");
            assertThat(command.isFindAllOptions()).isFalse();
            assertThat(command.tags()).containsExactly("tag1");
            assertThat(command.fulfillmentVendorGoods()).isNull();
        }
    }

    @Nested
    @DisplayName("ReorderProductTagsRequest → ReorderProductTagsCommand")
    class ReorderCommandMap {

        @Test
        @DisplayName("productId와 tagOrders를 매핑한다")
        void mapsReorderCommand() {
            ReorderProductTagsRequest request = new ReorderProductTagsRequest(
                    List.of(new ReorderProductTagsRequest.TagOrderItem(1L, 1L),
                            new ReorderProductTagsRequest.TagOrderItem(2L, 2L))
            );

            ReorderProductTagsCommand command = ProductRequestToCommandMapper.mapToCommand(100L, request);

            assertThat(command.productId()).isEqualTo(100L);
            assertThat(command.tagOrders()).hasSize(2);
            assertThat(command.tagOrders().get(0).tagId()).isEqualTo(1L);
            assertThat(command.tagOrders().get(0).orderNum()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("GetMyOrderingProductsRequest → GetMyOrderingProductsQuery")
    class GetMyOrderingProductsMap {

        @Test
        @DisplayName("orderingItemRequests를 OrderingItemQuery 리스트로 매핑한다")
        void mapsOrderingItems() {
            UUID sharerKey = UUID.randomUUID();
            GetMyOrderingProductsRequest request = new GetMyOrderingProductsRequest(
                    List.of(new OrderingItemRequest(
                            500L, sharerKey, (short) 2, "https://cdn.example.com/img.png"
                    ))
            );

            GetMyOrderingProductsQuery query = ProductRequestToCommandMapper.mapToCommand(request);

            assertThat(query.orderingItemQueries()).hasSize(1);
            assertThat(query.orderingItemQueries().getFirst().pricePolicyId()).isEqualTo(500L);
            assertThat(query.orderingItemQueries().getFirst().sharerKey()).isEqualTo(sharerKey);
            assertThat(query.orderingItemQueries().getFirst().quantity()).isEqualTo((short) 2);
            assertThat(query.orderingItemQueries().getFirst().imageUrl()).isEqualTo("https://cdn.example.com/img.png");
        }
    }

    @Nested
    @DisplayName("RegisterProductFulfillmentVendorGoodsRequest 35개 필드 매핑")
    class RegisterFulfillmentVendorGoodsFieldMap {

        @Test
        @DisplayName("RegisterProductRequest의 fulfillmentVendorGoods 35개 필드를 모두 매핑한다")
        void mapsAllFulfillmentVendorGoodsFields() {
            RegisterProductFulfillmentVendorGoodsRequest fulfillment = new RegisterProductFulfillmentVendorGoodsRequest();
            setAllRegisterFulfillmentFields(fulfillment);
            RegisterProductRequest request = new RegisterProductRequest(
                    10L, "상품명", "브랜드", "상세",
                    10_000L, 8_000L, 100L, true,
                    List.of("tag1"),
                    fulfillment
            );

            RegisterProductCommand command = ProductRequestToCommandMapper.mapToCommand(request);

            FulfillmentVendorGoodsOptionCommand vendor = command.fulfillmentVendorGoods();
            assertThat(vendor).isNotNull();
            assertThat(vendor.goodsType()).isEqualTo("1");
            assertThat(vendor.giftDivision()).isEqualTo("01");
            assertThat(vendor.goodsOptionCode1()).isEqualTo("OPT1");
            assertThat(vendor.goodsOptionCode2()).isEqualTo("OPT2");
            assertThat(vendor.invoiceGoodsNameEnabled()).isEqualTo("Y");
            assertThat(vendor.invoiceGoodsName()).isEqualTo("송장상품명");
            assertThat(vendor.supplierCode()).isEqualTo("SUP001");
            assertThat(vendor.categoryCode()).isEqualTo("CAT001");
            assertThat(vendor.seasonCode()).isEqualTo("0");
            assertThat(vendor.genderCode()).isEqualTo("A");
            assertThat(vendor.manufactureYear()).isEqualTo("2025");
            assertThat(vendor.unitPrice()).isEqualTo("1000");
            assertThat(vendor.supplyPrice()).isEqualTo("800");
            assertThat(vendor.salePrice()).isEqualTo("1200");
            assertThat(vendor.handlingTemperature()).isEqualTo("상온");
            assertThat(vendor.pickingFacility()).isEqualTo("설비A");
            assertThat(vendor.goodsBarcode()).isEqualTo("BAR001");
            assertThat(vendor.boxWeight()).isEqualTo("500");
            assertThat(vendor.origin()).isEqualTo("대한민국");
            assertThat(vendor.expirationDateManagementEnabled()).isEqualTo("Y");
            assertThat(vendor.shelfLifeDays()).isEqualTo("30");
            assertThat(vendor.outboundAvailableDays()).isEqualTo("7");
            assertThat(vendor.inboundAvailableDays()).isEqualTo("3");
            assertThat(vendor.outboundBoxType()).isEqualTo("BOX1");
            assertThat(vendor.cushioningEnabled()).isEqualTo("N");
            assertThat(vendor.loadingDirection()).isEqualTo("NONE");
            assertThat(vendor.subsidiaryMaterialCode()).isEqualTo("01");
            assertThat(vendor.enabled()).isEqualTo("Y");
            assertThat(vendor.safetyStock()).isEqualTo("10");
            assertThat(vendor.feeApplied()).isEqualTo("Y");
            assertThat(vendor.saleUnitQuantity()).isEqualTo("1");
            assertThat(vendor.customerGoodsImageUrl()).isEqualTo("https://cdn.example.com/custom.png");
            assertThat(vendor.externalGoodsImageUrl()).isEqualTo("https://cdn.example.com/external.png");
        }

        @Test
        @DisplayName("RegisterProductFulfillmentVendorGoodsRequest의 필드가 모두 null이면 command 필드도 모두 null로 매핑된다")
        void mapsNullFieldsWhenRequestFieldsAreNull() {
            RegisterProductFulfillmentVendorGoodsRequest fulfillment = new RegisterProductFulfillmentVendorGoodsRequest();
            RegisterProductRequest request = new RegisterProductRequest(
                    10L, "상품명", "브랜드", "상세",
                    10_000L, 8_000L, 100L, true,
                    List.of("tag1"),
                    fulfillment
            );

            RegisterProductCommand command = ProductRequestToCommandMapper.mapToCommand(request);

            FulfillmentVendorGoodsOptionCommand vendor = command.fulfillmentVendorGoods();
            assertThat(vendor).isNotNull();
            assertThat(vendor.goodsType()).isNull();
            assertThat(vendor.giftDivision()).isNull();
            assertThat(vendor.externalGoodsImageUrl()).isNull();
        }

        private void setAllRegisterFulfillmentFields(RegisterProductFulfillmentVendorGoodsRequest request) {
            ReflectionTestUtils.setField(request, "godType", "1");
            ReflectionTestUtils.setField(request, "giftDiv", "01");
            ReflectionTestUtils.setField(request, "godOptCd1", "OPT1");
            ReflectionTestUtils.setField(request, "godOptCd2", "OPT2");
            ReflectionTestUtils.setField(request, "invGodNmUseYn", "Y");
            ReflectionTestUtils.setField(request, "invGodNm", "송장상품명");
            ReflectionTestUtils.setField(request, "supCd", "SUP001");
            ReflectionTestUtils.setField(request, "cateCd", "CAT001");
            ReflectionTestUtils.setField(request, "seasonCd", "0");
            ReflectionTestUtils.setField(request, "genderCd", "A");
            ReflectionTestUtils.setField(request, "makeYr", "2025");
            ReflectionTestUtils.setField(request, "godPr", "1000");
            ReflectionTestUtils.setField(request, "inPr", "800");
            ReflectionTestUtils.setField(request, "salPr", "1200");
            ReflectionTestUtils.setField(request, "dealTemp", "상온");
            ReflectionTestUtils.setField(request, "pickFac", "설비A");
            ReflectionTestUtils.setField(request, "godBarcd", "BAR001");
            ReflectionTestUtils.setField(request, "boxWeight", "500");
            ReflectionTestUtils.setField(request, "origin", "대한민국");
            ReflectionTestUtils.setField(request, "distTermMgtYn", "Y");
            ReflectionTestUtils.setField(request, "useTermDay", "30");
            ReflectionTestUtils.setField(request, "outCanDay", "7");
            ReflectionTestUtils.setField(request, "inCanDay", "3");
            ReflectionTestUtils.setField(request, "boxDiv", "BOX1");
            ReflectionTestUtils.setField(request, "bufGodYn", "N");
            ReflectionTestUtils.setField(request, "loadingDirection", "NONE");
            ReflectionTestUtils.setField(request, "subMate", "01");
            ReflectionTestUtils.setField(request, "useYn", "Y");
            ReflectionTestUtils.setField(request, "safetyStock", "10");
            ReflectionTestUtils.setField(request, "feeYn", "Y");
            ReflectionTestUtils.setField(request, "saleUnitQty", "1");
            ReflectionTestUtils.setField(request, "cstGodImgUrl", "https://cdn.example.com/custom.png");
            ReflectionTestUtils.setField(request, "externalGodImgUrl", "https://cdn.example.com/external.png");
        }
    }

    @Nested
    @DisplayName("UpdateProductFulfillmentVendorGoodsRequest 35개 필드 매핑")
    class UpdateFulfillmentVendorGoodsFieldMap {

        @Test
        @DisplayName("UpdateProductRequest의 fulfillmentVendorGoods 35개 필드를 모두 매핑한다")
        void mapsAllFulfillmentVendorGoodsFields() {
            UpdateProductFulfillmentVendorGoodsRequest fulfillment = new UpdateProductFulfillmentVendorGoodsRequest();
            setAllUpdateFulfillmentFields(fulfillment);
            UpdateProductRequest request = new UpdateProductRequest(
                    "상품명", "브랜드", "상세",
                    true, List.of("tag1"),
                    fulfillment
            );

            UpdateProductCommand command = ProductRequestToCommandMapper.mapToCommand(100L, request);

            FulfillmentVendorGoodsOptionCommand vendor = command.fulfillmentVendorGoods();
            assertThat(vendor).isNotNull();
            assertThat(vendor.goodsType()).isEqualTo("2");
            assertThat(vendor.giftDivision()).isEqualTo("02");
            assertThat(vendor.goodsOptionCode1()).isEqualTo("UP_OPT1");
            assertThat(vendor.goodsOptionCode2()).isEqualTo("UP_OPT2");
            assertThat(vendor.invoiceGoodsNameEnabled()).isEqualTo("N");
            assertThat(vendor.invoiceGoodsName()).isEqualTo("수정송장명");
            assertThat(vendor.supplierCode()).isEqualTo("SUP999");
            assertThat(vendor.categoryCode()).isEqualTo("CAT999");
            assertThat(vendor.seasonCode()).isEqualTo("1");
            assertThat(vendor.genderCode()).isEqualTo("M");
            assertThat(vendor.manufactureYear()).isEqualTo("2026");
            assertThat(vendor.unitPrice()).isEqualTo("2000");
            assertThat(vendor.supplyPrice()).isEqualTo("1500");
            assertThat(vendor.salePrice()).isEqualTo("2400");
            assertThat(vendor.handlingTemperature()).isEqualTo("냉장");
            assertThat(vendor.pickingFacility()).isEqualTo("설비B");
            assertThat(vendor.goodsBarcode()).isEqualTo("BAR999");
            assertThat(vendor.boxWeight()).isEqualTo("1000");
            assertThat(vendor.origin()).isEqualTo("미국");
            assertThat(vendor.expirationDateManagementEnabled()).isEqualTo("N");
            assertThat(vendor.shelfLifeDays()).isEqualTo("60");
            assertThat(vendor.outboundAvailableDays()).isEqualTo("14");
            assertThat(vendor.inboundAvailableDays()).isEqualTo("5");
            assertThat(vendor.outboundBoxType()).isEqualTo("BOX2");
            assertThat(vendor.cushioningEnabled()).isEqualTo("Y");
            assertThat(vendor.loadingDirection()).isEqualTo("UP");
            assertThat(vendor.subsidiaryMaterialCode()).isEqualTo("02");
            assertThat(vendor.enabled()).isEqualTo("N");
            assertThat(vendor.safetyStock()).isEqualTo("20");
            assertThat(vendor.feeApplied()).isEqualTo("N");
            assertThat(vendor.saleUnitQuantity()).isEqualTo("2");
            assertThat(vendor.customerGoodsImageUrl()).isEqualTo("https://cdn.example.com/custom-up.png");
            assertThat(vendor.externalGoodsImageUrl()).isEqualTo("https://cdn.example.com/external-up.png");
        }

        @Test
        @DisplayName("UpdateProductRequest의 fulfillmentVendorGoods가 null이면 command도 null로 매핑한다(NPE 없음)")
        void returnsNullCommandWhenRequestIsNull() {
            UpdateProductRequest request = new UpdateProductRequest(
                    "상품명", "브랜드", "상세",
                    true, List.of("tag1"),
                    null
            );

            UpdateProductCommand command = ProductRequestToCommandMapper.mapToCommand(100L, request);

            assertThat(command.fulfillmentVendorGoods()).isNull();
        }

        private void setAllUpdateFulfillmentFields(UpdateProductFulfillmentVendorGoodsRequest request) {
            ReflectionTestUtils.setField(request, "godType", "2");
            ReflectionTestUtils.setField(request, "giftDiv", "02");
            ReflectionTestUtils.setField(request, "godOptCd1", "UP_OPT1");
            ReflectionTestUtils.setField(request, "godOptCd2", "UP_OPT2");
            ReflectionTestUtils.setField(request, "invGodNmUseYn", "N");
            ReflectionTestUtils.setField(request, "invGodNm", "수정송장명");
            ReflectionTestUtils.setField(request, "supCd", "SUP999");
            ReflectionTestUtils.setField(request, "cateCd", "CAT999");
            ReflectionTestUtils.setField(request, "seasonCd", "1");
            ReflectionTestUtils.setField(request, "genderCd", "M");
            ReflectionTestUtils.setField(request, "makeYr", "2026");
            ReflectionTestUtils.setField(request, "godPr", "2000");
            ReflectionTestUtils.setField(request, "inPr", "1500");
            ReflectionTestUtils.setField(request, "salPr", "2400");
            ReflectionTestUtils.setField(request, "dealTemp", "냉장");
            ReflectionTestUtils.setField(request, "pickFac", "설비B");
            ReflectionTestUtils.setField(request, "godBarcd", "BAR999");
            ReflectionTestUtils.setField(request, "boxWeight", "1000");
            ReflectionTestUtils.setField(request, "origin", "미국");
            ReflectionTestUtils.setField(request, "distTermMgtYn", "N");
            ReflectionTestUtils.setField(request, "useTermDay", "60");
            ReflectionTestUtils.setField(request, "outCanDay", "14");
            ReflectionTestUtils.setField(request, "inCanDay", "5");
            ReflectionTestUtils.setField(request, "boxDiv", "BOX2");
            ReflectionTestUtils.setField(request, "bufGodYn", "Y");
            ReflectionTestUtils.setField(request, "loadingDirection", "UP");
            ReflectionTestUtils.setField(request, "subMate", "02");
            ReflectionTestUtils.setField(request, "useYn", "N");
            ReflectionTestUtils.setField(request, "safetyStock", "20");
            ReflectionTestUtils.setField(request, "feeYn", "N");
            ReflectionTestUtils.setField(request, "saleUnitQty", "2");
            ReflectionTestUtils.setField(request, "cstGodImgUrl", "https://cdn.example.com/custom-up.png");
            ReflectionTestUtils.setField(request, "externalGodImgUrl", "https://cdn.example.com/external-up.png");
        }
    }

    @Nested
    @DisplayName("선택 필드 기본값 적용")
    class OptionalFieldDefaults {

        @Test
        @DisplayName("RegisterProductRequest에서 isFindAllOptions가 null이면 기본값 true가 적용된다")
        void registerRequestAppliesDefaultIsFindAllOptionsWhenNull() {
            RegisterProductRequest request = new RegisterProductRequest(
                    10L, "상품명", "브랜드", "상세",
                    10_000L, 8_000L, 100L,
                    null,
                    List.of("tag1"),
                    null
            );

            RegisterProductCommand command = ProductRequestToCommandMapper.mapToCommand(request);

            assertThat(command.isFindAllOptions()).isTrue();
        }

        @Test
        @DisplayName("UpdateProductRequest에서 isFindAllOptions가 null이면 기본값 true가 적용된다")
        void updateRequestAppliesDefaultIsFindAllOptionsWhenNull() {
            UpdateProductRequest request = new UpdateProductRequest(
                    "상품명", "브랜드", "상세",
                    null, List.of("tag1"),
                    null
            );

            UpdateProductCommand command = ProductRequestToCommandMapper.mapToCommand(100L, request);

            assertThat(command.isFindAllOptions()).isTrue();
        }
    }

    @Nested
    @DisplayName("빈 옵션 리스트 처리")
    class EmptyOptionListMap {

        @Test
        @DisplayName("UpdateProductOptionsRequest의 options가 빈 리스트면 RegisterProductOptionsCommand.options도 빈 리스트로 매핑된다")
        void mapsEmptyOptionsForRegisterCommand() {
            UpdateProductOptionsRequest request = new UpdateProductOptionsRequest();
            ReflectionTestUtils.setField(request, "categoryName", "색상");
            ReflectionTestUtils.setField(request, "options", List.<RegisterProductOptionRequest>of());

            RegisterProductOptionsCommand command = ProductRequestToCommandMapper.mapToCommand(100L, request);

            assertThat(command.productId()).isEqualTo(100L);
            assertThat(command.categoryName()).isEqualTo("색상");
            assertThat(command.options()).isEmpty();
        }

        @Test
        @DisplayName("UpdateProductOptionsRequest의 options가 빈 리스트면 UpdateProductOptionsCommand.options도 빈 리스트로 매핑된다")
        void mapsEmptyOptionsForUpdateCommand() {
            UpdateProductOptionsRequest request = new UpdateProductOptionsRequest();
            ReflectionTestUtils.setField(request, "categoryName", "색상");
            ReflectionTestUtils.setField(request, "options", List.<RegisterProductOptionRequest>of());

            UpdateProductOptionsCommand command = ProductRequestToCommandMapper.mapToUpdateCommand(100L, 7L, request);

            assertThat(command.productId()).isEqualTo(100L);
            assertThat(command.optionCategoryId()).isEqualTo(7L);
            assertThat(command.categoryName()).isEqualTo("색상");
            assertThat(command.options()).isEmpty();
        }
    }
}
