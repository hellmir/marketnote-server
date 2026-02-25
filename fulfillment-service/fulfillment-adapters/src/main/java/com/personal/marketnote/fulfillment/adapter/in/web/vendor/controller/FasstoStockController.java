package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.GetFasstoStockDetailApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.GetFasstoStocksApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.SyncFasstoAllStocksApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FasstoStockRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFasstoStocksResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoStocksResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFasstoStockDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFasstoStocksUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.SyncFasstoAllStockUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 파스토 재고 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 파스토 재고 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/vendors/fassto/stocks")
@Tag(name = "파스토 재고 API", description = "파스토 재고 관련 API")
@RequiredArgsConstructor
public class FasstoStockController {
    private final GetFasstoStocksUseCase getFasstoStocksUseCase;
    private final GetFasstoStockDetailUseCase getFasstoStockDetailUseCase;
    private final SyncFasstoAllStockUseCase syncFasstoAllStockUseCase;

    /**
     * (관리자) 파스토 재고 목록 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param outOfStockYn 품절 상품 조회 여부(Y/N)
     * @param whCd         창고 코드
     * @Author 성효빈
     * @Date 2026-02-05
     * @Description 파스토 재고 목록을 조회합니다.
     */
    @GetMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFasstoStocksApiDocs
    public ResponseEntity<BaseResponse<GetFasstoStocksResponse>> getStocks(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String outOfStockYn,
            @RequestParam(required = false) String whCd
    ) {
        GetFasstoStocksResult result = getFasstoStocksUseCase.getStocks(
                FasstoStockRequestToCommandMapper.mapToStocksCommand(customerCode, accessToken, outOfStockYn, whCd)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoStocksResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 재고 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 단일 상품 재고 정보 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param cstGodCd     고객사상품코드
     * @param outOfStockYn 품절 상품 조회 여부(Y/N)
     * @Author 성효빈
     * @Date 2026-02-03
     * @Description 파스토 단일 상품 재고를 조회합니다.
     */
    @GetMapping("/detail/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFasstoStockDetailApiDocs
    public ResponseEntity<BaseResponse<GetFasstoStocksResponse>> getStockDetail(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam("cstGodCd") String cstGodCd,
            @RequestParam(required = false) String outOfStockYn
    ) {
        GetFasstoStocksResult result = getFasstoStockDetailUseCase.getStockDetail(
                FasstoStockRequestToCommandMapper.mapToStockDetailCommand(customerCode, accessToken, cstGodCd, outOfStockYn)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoStocksResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 단일 상품 재고 정보 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 전체 상품 재고 동기화
     *
     * @param customerCode 파스토 고객사 코드
     * @param whCd         창고 코드
     * @Author 성효빈
     * @Date 2026-02-07
     * @Description 전체 상품 재고와 파스토 재고를 동기화합니다.
     */
    @PostMapping("/sync/all/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @SyncFasstoAllStocksApiDocs
    public ResponseEntity<BaseResponse<Void>> syncAllStocks(
            @PathVariable String customerCode,
            @RequestParam(required = false) String whCd
    ) {
        syncFasstoAllStockUseCase.syncAll(
                FasstoStockRequestToCommandMapper.mapToSyncAllCommand(customerCode, whCd)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "전체 상품 재고 동기화 성공"
                ),
                HttpStatus.OK
        );
    }
}
