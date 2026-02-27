package com.personal.marketnote.commerce.adapter.in.web.inventory.controller;

import com.personal.marketnote.commerce.adapter.in.web.inventory.controller.apidocs.GetInventoriesApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.inventory.controller.apidocs.RegisterInventoryApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.inventory.controller.apidocs.SyncFulfillmentVendorInventoryApiDocs;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_OR_SELLER_POINTCUT;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 재고 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-06
 * @Description 재고 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/inventories")
@Tag(name = "재고 API", description = "재고 관련 API")
@RequiredArgsConstructor
public class InventoryController {
    private final RegisterInventoryUseCase registerInventoryUseCase;
    private final GetInventoryUseCase getInventoryUseCase;
    private final SyncFulfillmentVendorInventoryUseCase syncFulfillmentVendorInventoryUseCase;

    /**
     * 재고 도메인 등록
     *
     * @param request 재고 도메인 등록 요청
     * @Author 성효빈
     * @Date 2026-01-06
     * @Description 재고 도메인을 등록합니다.
     */
    @PostMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterInventoryApiDocs
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
     * 상품 재고 목록 조회
     *
     * @param pricePolicyIds 가격 정책 ID 목록
     * @param productIds     상품 ID 목록 (선택, pricePolicyIds와 동일 순서로 매핑)
     * @Author 성효빈
     * @Date 2026-01-07
     * @Description 상품 재고 목록을 조회합니다. productIds가 제공되면 존재하지 않는 재고를 자동 생성합니다.
     */
    @GetMapping
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @GetInventoriesApiDocs
    public ResponseEntity<BaseResponse<GetInventoriesResponse>> getInventories(
            @Valid @RequestParam List<Long> pricePolicyIds,
            @RequestParam(required = false) List<Long> productIds
    ) {
        Set<Inventory> inventories;
        if (FormatValidator.hasValue(productIds) && productIds.size() == pricePolicyIds.size()) {
            Map<Long, Long> productIdsByPricePolicyId = new HashMap<>();
            for (int i = 0; i < pricePolicyIds.size(); i++) {
                productIdsByPricePolicyId.put(pricePolicyIds.get(i), productIds.get(i));
            }
            inventories = getInventoryUseCase.getOrCreateInventories(productIdsByPricePolicyId);
        } else {
            inventories = getInventoryUseCase.getInventories(pricePolicyIds);
        }

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
     * (관리자) 풀필먼트 벤더 재고 동기화
     *
     * @param request 풀필먼트 벤더 재고 동기화 요청
     * @Author 성효빈
     * @Date 2026-02-07
     * @Description 풀필먼트 벤더 재고 정보를 동기화합니다.
     */
    @PostMapping("/fulfillment/vendors/stocks/sync")
    @PreAuthorize(ADMIN_POINTCUT)
    @SyncFulfillmentVendorInventoryApiDocs
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
}
