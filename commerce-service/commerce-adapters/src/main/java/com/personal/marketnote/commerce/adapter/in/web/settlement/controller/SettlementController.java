package com.personal.marketnote.commerce.adapter.in.web.settlement.controller;

import com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs.*;
import com.personal.marketnote.commerce.adapter.in.web.settlement.mapper.SellerSettlementRequestToQueryMapper;
import com.personal.marketnote.commerce.adapter.in.web.settlement.mapper.SettlementRequestToCommandMapper;
import com.personal.marketnote.commerce.adapter.in.web.settlement.request.ExecuteSettlementRequest;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSellerSettlementsResponse;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSettlementDetailResponse;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSettlementResponse;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSettlementsResponse;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementDetailResult;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementResult;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.*;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_OR_SELLER_POINTCUT;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 정산 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-02-16
 * @Description 정산 관련 API를 제공합니다.
 */
@RestController
@Tag(name = "정산 API", description = "정산 관련 API")
@RequiredArgsConstructor
@Validated
public class SettlementController {
    private final ExecuteSettlementUseCase executeSettlementUseCase;
    private final RetryFailedSettlementUseCase retryFailedSettlementUseCase;
    private final CancelSettlementUseCase cancelSettlementUseCase;
    private final ReExecuteSettlementUseCase reExecuteSettlementUseCase;
    private final GetFailedSettlementsUseCase getFailedSettlementsUseCase;
    private final GetSettlementUseCase getSettlementUseCase;
    private final GetSettlementDetailUseCase getSettlementDetailUseCase;
    private final GetSellerSettlementsUseCase getSellerSettlementsUseCase;

    /**
     * 정산 실행
     *
     * @param request 정산 실행 요청
     * @Author 성효빈
     * @Date 2026-02-16
     * @Description 지정된 연/월에 대해 판매자별 정산을 실행합니다.
     */
    @PostMapping("/api/v1/admin/settlements")
    @PreAuthorize(ADMIN_POINTCUT)
    @ExecuteSettlementApiDocs
    public ResponseEntity<BaseResponse<Void>> executeSettlement(
            @Valid @RequestBody ExecuteSettlementRequest request
    ) {
        executeSettlementUseCase.executeSettlement(
                SettlementRequestToCommandMapper.mapToCommand(request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "정산 실행 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 실패한 정산을 재시도한다.
     *
     * @param id 정산 ID
     * @return 재시도 결과 응답
     * @author 성효빈
     * @since 2026-03-02
     */
    @PostMapping("/api/v1/admin/settlements/{id}/retry")
    @PreAuthorize(ADMIN_POINTCUT)
    @RetryFailedSettlementApiDocs
    public ResponseEntity<BaseResponse<Void>> retrySettlement(
            @PathVariable("id") Long id
    ) {
        retryFailedSettlementUseCase.retrySettlement(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "정산 재시도 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 완료된 정산을 취소한다.
     *
     * @param id 정산 ID
     * @return 취소 결과 응답
     * @author 성효빈
     * @since 2026-03-02
     */
    @PostMapping("/api/v1/admin/settlements/{id}/cancel")
    @PreAuthorize(ADMIN_POINTCUT)
    @CancelSettlementApiDocs
    public ResponseEntity<BaseResponse<Void>> cancelSettlement(
            @PathVariable("id") Long id
    ) {
        cancelSettlementUseCase.cancelSettlement(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "정산 취소 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 취소된 정산을 재실행한다.
     *
     * @param id 정산 ID
     * @return 재실행 결과 응답
     * @author 성효빈
     * @since 2026-03-02
     */
    @PostMapping("/api/v1/admin/settlements/{id}/re-execute")
    @PreAuthorize(ADMIN_POINTCUT)
    @ReExecuteSettlementApiDocs
    public ResponseEntity<BaseResponse<Void>> reExecuteSettlement(
            @PathVariable("id") Long id
    ) {
        reExecuteSettlementUseCase.reExecuteSettlement(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "정산 재실행 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 실패한 정산 목록을 조회한다.
     *
     * @return 실패한 정산 목록 응답
     * @author 성효빈
     * @since 2026-03-02
     */
    @GetMapping("/api/v1/admin/settlements/failed")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFailedSettlementsApiDocs
    public ResponseEntity<BaseResponse<GetSettlementsResponse>> getFailedSettlements() {
        GetSettlementsResult result = getFailedSettlementsUseCase.getFailedSettlements();

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetSettlementsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "실패한 정산 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 정산 목록 조회
     *
     * @param year  정산 연도
     * @param month 정산 월
     * @return 정산 목록 조회 응답 {@link GetSettlementsResponse}
     * @Author 성효빈
     * @Date 2026-02-16
     * @Description 연/월 기준으로 정산 목록을 조회합니다.
     */
    @GetMapping("/api/v1/admin/settlements")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetSettlementsApiDocs
    public ResponseEntity<BaseResponse<GetSettlementsResponse>> getSettlements(
            @RequestParam @NotNull @Min(2020) @Max(2100) Integer year,
            @RequestParam @NotNull @Min(1) @Max(12) Integer month
    ) {
        GetSettlementsResult result = getSettlementUseCase.getSettlements(
                SettlementRequestToCommandMapper.mapToQuery(year, month)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetSettlementsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "정산 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 정산 단건 조회
     *
     * @param id 정산 ID
     * @return 정산 단건 조회 응답 {@link GetSettlementResponse}
     * @Author 성효빈
     * @Date 2026-02-16
     * @Description 정산 ID로 정산 상세 정보를 조회합니다.
     */
    @GetMapping("/api/v1/admin/settlements/{id}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetSettlementApiDocs
    public ResponseEntity<BaseResponse<GetSettlementResponse>> getSettlement(
            @PathVariable("id") Long id
    ) {
        GetSettlementResult result = getSettlementUseCase.getSettlement(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetSettlementResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "정산 단건 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 정산 내역 상세 조회
     *
     * @param id 정산 ID
     * @return 정산에 포함된 결제 배분 목록 {@link GetSettlementDetailResponse}
     * @Author 성효빈
     * @Date 2026-03-02
     * @Description 정산 ID로 해당 정산에 포함된 결제 배분(PaymentAllocation) 목록을 조회합니다.
     */
    @GetMapping("/api/v1/admin/settlements/{id}/allocations")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetSettlementDetailApiDocs
    public ResponseEntity<BaseResponse<List<GetSettlementDetailResponse>>> getSettlementAllocations(
            @PathVariable("id") Long id
    ) {
        List<GetSettlementDetailResult> results = getSettlementDetailUseCase.getSettlementAllocations(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetSettlementDetailResponse.fromList(results),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "정산 내역 상세 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 나의 정산 내역 조회
     *
     * @param year      정산 연도
     * @param month     정산 월 (선택)
     * @param principal 인증된 사용자 정보
     * @return 판매자 정산 내역 조회 응답 {@link GetSellerSettlementsResponse}
     * @Author 성효빈
     * @Date 2026-03-02
     * @Description 판매자 본인의 정산 내역을 조회합니다.
     */
    @GetMapping("/api/v1/sellers/me/settlements")
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @GetSellerSettlementsApiDocs
    public ResponseEntity<BaseResponse<GetSellerSettlementsResponse>> getMySettlements(
            @RequestParam @NotNull @Min(2020) @Max(2100) Integer year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long sellerId = ElementExtractor.extractUserId(principal);

        GetSettlementsResult result = getSellerSettlementsUseCase.getSellerSettlements(
                SellerSettlementRequestToQueryMapper.mapToQuery(sellerId, year, month)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetSellerSettlementsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "나의 정산 내역 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
