package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.in.result.fulfillment.FulfillmentVendorGoodsElementInfoResult;
import com.personal.marketnote.product.port.in.result.fulfillment.FulfillmentVendorGoodsInfoResult;
import com.personal.marketnote.product.port.in.result.fulfillment.GetFulfillmentVendorGoodsResult;
import com.personal.marketnote.product.port.in.result.product.GetAdminProductDetailResult;
import com.personal.marketnote.product.port.in.result.product.GetProductInfoWithOptionsResult;
import com.personal.marketnote.product.port.in.usecase.product.GetAdminProductDetailUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.fulfillment.GetFulfillmentVendorGoodsElementPort;
import com.personal.marketnote.product.port.out.fulfillment.GetFulfillmentVendorGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetAdminProductDetailService implements GetAdminProductDetailUseCase {

    private final GetProductUseCase getProductUseCase;
    private final GetFulfillmentVendorGoodsPort getFulfillmentVendorGoodsPort;
    private final GetFulfillmentVendorGoodsElementPort getFulfillmentVendorGoodsElementPort;

    @Override
    public GetAdminProductDetailResult getAdminProductDetail(Long id, List<Long> selectedOptionIds) {
        List<Long> options = FormatValidator.hasValue(selectedOptionIds)
                ? selectedOptionIds
                : List.of();

        GetProductInfoWithOptionsResult productInfo = getProductUseCase.getProductInfoIncludingInactive(id, options);

        GetFulfillmentVendorGoodsResult goodsResult =
                getFulfillmentVendorGoodsPort.getFulfillmentVendorGoods(String.valueOf(id));
        FulfillmentVendorGoodsInfoResult goodsInfo = resolveGoodsInfo(id, goodsResult);

        FulfillmentVendorGoodsElementInfoResult elementInfo = resolveElementInfo(id);

        return GetAdminProductDetailResult.of(productInfo, goodsInfo, elementInfo);
    }

    private FulfillmentVendorGoodsInfoResult resolveGoodsInfo(
            Long id,
            GetFulfillmentVendorGoodsResult goodsResult
    ) {
        if (FormatValidator.hasNoValue(id)) {
            return null;
        }
        Map<String, FulfillmentVendorGoodsInfoResult> goodsByCstGodCd = mapGoodsByCstGodCd(goodsResult);
        return goodsByCstGodCd.get(String.valueOf(id));
    }

    private FulfillmentVendorGoodsElementInfoResult resolveElementInfo(Long id) {
        if (FormatValidator.hasNoValue(id)) {
            return null;
        }
        return getFulfillmentVendorGoodsElementPort.getFulfillmentVendorGoodsElement(String.valueOf(id))
                .orElse(null);
    }

    private Map<String, FulfillmentVendorGoodsInfoResult> mapGoodsByCstGodCd(
            GetFulfillmentVendorGoodsResult goodsResult
    ) {
        if (FormatValidator.hasNoValue(goodsResult)
                || FormatValidator.hasNoValue(goodsResult.goods())) {
            return Map.of();
        }

        Map<String, FulfillmentVendorGoodsInfoResult> goodsByCstGodCd = new HashMap<>();
        for (FulfillmentVendorGoodsInfoResult item : goodsResult.goods()) {
            if (FormatValidator.hasNoValue(item) || FormatValidator.hasNoValue(item.customerGoodsCode())) {
                continue;
            }
            goodsByCstGodCd.putIfAbsent(item.customerGoodsCode(), item);
        }
        return goodsByCstGodCd;
    }
}
