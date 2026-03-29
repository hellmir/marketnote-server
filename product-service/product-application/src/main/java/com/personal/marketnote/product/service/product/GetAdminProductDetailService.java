package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.in.result.fulfillment.FulfillmentVendorGoodsElementInfoResult;
import com.personal.marketnote.product.port.in.result.fulfillment.FulfillmentVendorGoodsInfoResult;
import com.personal.marketnote.product.port.in.result.fulfillment.GetFulfillmentVendorGoodsElementsResult;
import com.personal.marketnote.product.port.in.result.fulfillment.GetFulfillmentVendorGoodsResult;
import com.personal.marketnote.product.port.in.result.product.GetAdminProductDetailResult;
import com.personal.marketnote.product.port.in.result.product.GetProductInfoWithOptionsResult;
import com.personal.marketnote.product.port.in.usecase.product.GetAdminProductDetailUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.fulfillment.GetFulfillmentVendorGoodsElementsPort;
import com.personal.marketnote.product.port.out.fulfillment.GetFulfillmentVendorGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetAdminProductDetailService implements GetAdminProductDetailUseCase {
    private static final Executor VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final GetProductUseCase getProductUseCase;
    private final GetFulfillmentVendorGoodsPort getFulfillmentVendorGoodsPort;
    private final GetFulfillmentVendorGoodsElementsPort getFulfillmentVendorGoodsElementsPort;

    @Override
    public GetAdminProductDetailResult getAdminProductDetail(Long id, List<Long> selectedOptionIds) {
        List<Long> options = FormatValidator.hasValue(selectedOptionIds)
                ? selectedOptionIds
                : List.of();

        GetProductInfoWithOptionsResult productInfo = getProductUseCase.getProductInfoIncludingInactive(id, options);

        // 두 Port 모두 HTTP 클라이언트(Fassto API)이므로 @Transactional 컨텍스트 전파 불필요. DB 조회로 변경 시 재검토 필요.
        CompletableFuture<GetFulfillmentVendorGoodsResult> goodsFuture =
                CompletableFuture.supplyAsync(
                        () -> getFulfillmentVendorGoodsPort.getFulfillmentVendorGoods(String.valueOf(id)),
                        VIRTUAL_EXECUTOR
                );
        CompletableFuture<GetFulfillmentVendorGoodsElementsResult> elementsFuture =
                CompletableFuture.supplyAsync(
                        () -> getFulfillmentVendorGoodsElementsPort.getFulfillmentVendorGoodsElements(),
                        VIRTUAL_EXECUTOR
                );

        GetFulfillmentVendorGoodsResult goodsResult = joinFuture(goodsFuture);
        GetFulfillmentVendorGoodsElementsResult elementsResult = joinFuture(elementsFuture);

        FulfillmentVendorGoodsInfoResult goodsInfo = resolveGoodsInfo(id, goodsResult);
        FulfillmentVendorGoodsElementInfoResult elementInfo = resolveElementInfo(id, elementsResult);

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

    private FulfillmentVendorGoodsElementInfoResult resolveElementInfo(
            Long id,
            GetFulfillmentVendorGoodsElementsResult elementsResult
    ) {
        if (FormatValidator.hasNoValue(id)) {
            return null;
        }
        Map<String, FulfillmentVendorGoodsElementInfoResult> elementsByCstGodCd = mapElementsByCstGodCd(elementsResult);
        return elementsByCstGodCd.get(String.valueOf(id));
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
            if (FormatValidator.hasNoValue(item) || FormatValidator.hasNoValue(item.cstGodCd())) {
                continue;
            }
            goodsByCstGodCd.putIfAbsent(item.cstGodCd(), item);
        }
        return goodsByCstGodCd;
    }

    private Map<String, FulfillmentVendorGoodsElementInfoResult> mapElementsByCstGodCd(
            GetFulfillmentVendorGoodsElementsResult elementsResult
    ) {
        if (FormatValidator.hasNoValue(elementsResult)
                || FormatValidator.hasNoValue(elementsResult.elements())) {
            return Map.of();
        }

        Map<String, FulfillmentVendorGoodsElementInfoResult> elementsByCstGodCd = new HashMap<>();
        for (FulfillmentVendorGoodsElementInfoResult element : elementsResult.elements()) {
            if (FormatValidator.hasNoValue(element) || FormatValidator.hasNoValue(element.cstGodCd())) {
                continue;
            }
            elementsByCstGodCd.putIfAbsent(element.cstGodCd(), element);
        }
        return elementsByCstGodCd;
    }

    private <T> T joinFuture(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException ce) {
            Throwable cause = ce.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw ce;
        }
    }
}
