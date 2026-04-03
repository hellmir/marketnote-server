package com.personal.marketnote.product.adapter.in.web.product.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.in.web.cart.request.GetMyOrderingProductsRequest;
import com.personal.marketnote.product.adapter.in.web.category.request.RegisterProductCategoriesRequest;
import com.personal.marketnote.product.adapter.in.web.option.request.UpdateProductOptionsRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.*;
import com.personal.marketnote.product.port.in.command.*;

import java.util.List;

public class ProductRequestToCommandMapper {
    public static RegisterProductCommand mapToCommand(RegisterProductRequest registerProductRequest) {
        return RegisterProductCommand.builder()
                .sellerId(registerProductRequest.sellerId())
                .name(registerProductRequest.name())
                .brandName(registerProductRequest.brandName())
                .detail(registerProductRequest.detail())
                .price(registerProductRequest.price())
                .discountPrice(registerProductRequest.discountPrice())
                .accumulatedPoint(registerProductRequest.accumulatedPoint())
                .isFindAllOptions(registerProductRequest.isFindAllOptions())
                .tags(registerProductRequest.tags())
                .fulfillmentVendorGoods(mapToCommand(registerProductRequest.fulfillmentVendorGoods()))
                .build();
    }

    private static FulfillmentVendorGoodsOptionCommand mapToCommand(RegisterProductFulfillmentVendorGoodsRequest request) {
        if (FormatValidator.hasNoValue(request)) {
            return null;
        }

        return FulfillmentVendorGoodsOptionCommand.builder()
                .goodsType(request.getGodType())
                .giftDivision(request.getGiftDiv())
                .goodsOptionCode1(request.getGodOptCd1())
                .goodsOptionCode2(request.getGodOptCd2())
                .invoiceGoodsNameEnabled(request.getInvGodNmUseYn())
                .invoiceGoodsName(request.getInvGodNm())
                .supplierCode(request.getSupCd())
                .categoryCode(request.getCateCd())
                .seasonCode(request.getSeasonCd())
                .genderCode(request.getGenderCd())
                .manufactureYear(request.getMakeYr())
                .unitPrice(request.getGodPr())
                .supplyPrice(request.getInPr())
                .salePrice(request.getSalPr())
                .handlingTemperature(request.getDealTemp())
                .pickingFacility(request.getPickFac())
                .goodsBarcode(request.getGodBarcd())
                .boxWeight(request.getBoxWeight())
                .origin(request.getOrigin())
                .expirationDateManagementEnabled(request.getDistTermMgtYn())
                .shelfLifeDays(request.getUseTermDay())
                .outboundAvailableDays(request.getOutCanDay())
                .inboundAvailableDays(request.getInCanDay())
                .outboundBoxType(request.getBoxDiv())
                .cushioningEnabled(request.getBufGodYn())
                .loadingDirection(request.getLoadingDirection())
                .subsidiaryMaterialCode(request.getSubMate())
                .enabled(request.getUseYn())
                .safetyStock(request.getSafetyStock())
                .feeApplied(request.getFeeYn())
                .saleUnitQuantity(request.getSaleUnitQty())
                .customerGoodsImageUrl(request.getCstGodImgUrl())
                .externalGoodsImageUrl(request.getExternalGodImgUrl())
                .build();
    }

    private static FulfillmentVendorGoodsOptionCommand mapToCommand(UpdateProductFulfillmentVendorGoodsRequest request) {
        if (FormatValidator.hasNoValue(request)) {
            return null;
        }

        return FulfillmentVendorGoodsOptionCommand.builder()
                .goodsType(request.getGodType())
                .giftDivision(request.getGiftDiv())
                .goodsOptionCode1(request.getGodOptCd1())
                .goodsOptionCode2(request.getGodOptCd2())
                .invoiceGoodsNameEnabled(request.getInvGodNmUseYn())
                .invoiceGoodsName(request.getInvGodNm())
                .supplierCode(request.getSupCd())
                .categoryCode(request.getCateCd())
                .seasonCode(request.getSeasonCd())
                .genderCode(request.getGenderCd())
                .manufactureYear(request.getMakeYr())
                .unitPrice(request.getGodPr())
                .supplyPrice(request.getInPr())
                .salePrice(request.getSalPr())
                .handlingTemperature(request.getDealTemp())
                .pickingFacility(request.getPickFac())
                .goodsBarcode(request.getGodBarcd())
                .boxWeight(request.getBoxWeight())
                .origin(request.getOrigin())
                .expirationDateManagementEnabled(request.getDistTermMgtYn())
                .shelfLifeDays(request.getUseTermDay())
                .outboundAvailableDays(request.getOutCanDay())
                .inboundAvailableDays(request.getInCanDay())
                .outboundBoxType(request.getBoxDiv())
                .cushioningEnabled(request.getBufGodYn())
                .loadingDirection(request.getLoadingDirection())
                .subsidiaryMaterialCode(request.getSubMate())
                .enabled(request.getUseYn())
                .safetyStock(request.getSafetyStock())
                .feeApplied(request.getFeeYn())
                .saleUnitQuantity(request.getSaleUnitQty())
                .customerGoodsImageUrl(request.getCstGodImgUrl())
                .externalGoodsImageUrl(request.getExternalGodImgUrl())
                .build();
    }

    public static RegisterProductCategoriesCommand mapToCommand(
            Long productId, RegisterProductCategoriesRequest registerProductCategoriesRequest
    ) {
        return RegisterProductCategoriesCommand.of(
                productId, registerProductCategoriesRequest.getCategoryIds()
        );
    }

    public static RegisterProductOptionsCommand mapToCommand(
            Long productId, UpdateProductOptionsRequest request
    ) {
        List<RegisterProductOptionsCommand.OptionItem> optionItems = request.getOptions().stream()
                .map(o -> new RegisterProductOptionsCommand.OptionItem(o.getContent()))
                .toList();
        return RegisterProductOptionsCommand.of(productId, request.getCategoryName(), optionItems);
    }

    public static UpdateProductOptionsCommand mapToUpdateCommand(
            Long productId, Long optionCategoryId, UpdateProductOptionsRequest request
    ) {
        List<RegisterProductOptionsCommand.OptionItem> optionItems = request.getOptions().stream()
                .map(o -> new RegisterProductOptionsCommand.OptionItem(o.getContent()))
                .toList();
        return UpdateProductOptionsCommand.of(
                productId, optionCategoryId, request.getCategoryName(), optionItems
        );
    }

    public static UpdateProductCommand mapToCommand(
            Long id, UpdateProductRequest request
    ) {
        return UpdateProductCommand.builder()
                .id(id)
                .name(request.name())
                .brandName(request.brandName())
                .detail(request.detail())
                .isFindAllOptions(request.isFindAllOptions())
                .tags(request.tags())
                .fulfillmentVendorGoods(mapToCommand(request.fulfillmentVendorGoods()))
                .build();
    }

    public static ReorderProductTagsCommand mapToCommand(Long productId, ReorderProductTagsRequest request) {
        List<ReorderProductTagsCommand.TagOrderItem> tagOrders = request.tagOrders().stream()
                .map(item -> new ReorderProductTagsCommand.TagOrderItem(item.tagId(), item.orderNum()))
                .toList();
        return ReorderProductTagsCommand.builder()
                .productId(productId)
                .tagOrders(tagOrders)
                .build();
    }

    public static GetMyOrderingProductsQuery mapToCommand(
            GetMyOrderingProductsRequest getMyOrderingProductsRequest
    ) {
        return GetMyOrderingProductsQuery.from(
                getMyOrderingProductsRequest.orderingItemRequests()
                        .stream()
                        .map(request -> OrderingItemQuery.of(
                                request.pricePolicyId(), request.sharerKey(), request.quantity(), request.imageUrl())
                        )
                        .toList()
        );
    }
}
