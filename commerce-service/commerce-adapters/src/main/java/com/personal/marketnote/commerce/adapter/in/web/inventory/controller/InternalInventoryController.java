package com.personal.marketnote.commerce.adapter.in.web.inventory.controller;

import com.personal.marketnote.commerce.adapter.in.web.inventory.mapper.InventoryRequestToCommandMapper;
import com.personal.marketnote.commerce.adapter.in.web.inventory.request.SyncFulfillmentVendorInventoryRequest;
import com.personal.marketnote.commerce.adapter.in.web.inventory.response.GetInventoriesResponse;
import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.port.in.result.inventory.GetInventoriesResult;
import com.personal.marketnote.commerce.port.in.usecase.inventory.GetInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RegisterInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.inventory.SyncFulfillmentVendorInventoryUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.in.request.RegisterInventoryRequest;
import com.personal.marketnote.common.utility.FormatValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 내부 재고 컨트롤러 (서비스 간 통신용)
 *
 * @Author 성효빈
 * @Date 2026-03-18
 * @Description HMAC 인증 기반 서비스 간 통신용 재고 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/internal/inventories")
@Tag(
        name = "내부 재고 API",
        description = "서비스 간 통신용 재고 API"
)
@RequiredArgsConstructor
public class InternalInventoryController {
    private final RegisterInventoryUseCase registerInventoryUseCase;
    private final GetInventoryUseCase getInventoryUseCase;
    private final SyncFulfillmentVendorInventoryUseCase syncFulfillmentVendorInventoryUseCase;

    /**
     * 재고 도메인 등록 (서비스 간 통신용)
     *
     * @param request 재고 도메인 등록 요청
     */
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> registerInventory(
            @Valid @RequestBody RegisterInventoryRequest request
    ) {
        registerInventoryUseCase.registerInventory(
                InventoryRequestToCommandMapper.mapToCommand(request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "재고 도메인 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 상품 재고 목록 조회 (서비스 간 통신용)
     *
     * @param pricePolicyIds 가격 정책 ID 목록
     * @param productIds     상품 ID 목록 (선택, pricePolicyIds와 동일 순서로 매핑)
     */
    @GetMapping
    public ResponseEntity<BaseResponse<GetInventoriesResponse>> getInventories(
            @Valid @RequestParam List<Long> pricePolicyIds,
            @RequestParam(required = false) List<Long> productIds
    ) {
        Set<Inventory> inventories = resolveInventories(pricePolicyIds, productIds);

        GetInventoriesResult getInventoriesResult = GetInventoriesResult.from(inventories);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetInventoriesResponse.from(getInventoriesResult),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 재고 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (서비스 간 통신용) 풀필먼트 벤더 재고 동기화
     *
     * @param request 풀필먼트 벤더 재고 동기화 요청
     */
    @PostMapping("/fulfillment/vendors/stocks/sync")
    public ResponseEntity<BaseResponse<Void>> syncFulfillmentVendorInventories(
            @Valid @RequestBody SyncFulfillmentVendorInventoryRequest request
    ) {
        syncFulfillmentVendorInventoryUseCase.syncInventories(
                InventoryRequestToCommandMapper.mapToCommand(request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "풀필먼트 벤더 재고 동기화 성공"
                ),
                HttpStatus.OK
        );
    }

    private Set<Inventory> resolveInventories(List<Long> pricePolicyIds, List<Long> productIds) {
        if (FormatValidator.hasValue(productIds) && productIds.size() == pricePolicyIds.size()) {
            Map<Long, Long> productIdsByPricePolicyId = new HashMap<>();
            for (int i = 0; i < pricePolicyIds.size(); i++) {
                productIdsByPricePolicyId.put(pricePolicyIds.get(i), productIds.get(i));
            }
            return getInventoryUseCase.getOrCreateInventories(productIdsByPricePolicyId);
        }
        return getInventoryUseCase.getInventories(pricePolicyIds);
    }
}
