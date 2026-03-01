package com.personal.marketnote.commerce.adapter.in.web.settlement.controller;

import com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs.ExecuteSettlementApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs.GetSettlementApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs.GetSettlementDetailApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs.GetSettlementsApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.settlement.mapper.SettlementRequestToCommandMapper;
import com.personal.marketnote.commerce.adapter.in.web.settlement.request.ExecuteSettlementRequest;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSettlementDetailResponse;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSettlementResponse;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSettlementsResponse;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementDetailResult;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementResult;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.ExecuteSettlementUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetSettlementDetailUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetSettlementUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 정산 컨트롤러 (관리자 전용)
 *
 * @Author 성효빈
 * @Date 2026-02-16
 * @Description 정산 관련 관리자 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/admin/settlements")
@Tag(name = "(관리자) 정산 API", description = "관리자 정산 관련 API")
@RequiredArgsConstructor
@Validated
public class SettlementController {
    private final ExecuteSettlementUseCase executeSettlementUseCase;
    private final GetSettlementUseCase getSettlementUseCase;
    private final GetSettlementDetailUseCase getSettlementDetailUseCase;

    /**
     * 정산 실행
     *
     * @param request 정산 실행 요청
     * @Author 성효빈
     * @Date 2026-02-16
     * @Description 지정된 연/월에 대해 판매자별 정산을 실행합니다.
     */
    @PostMapping
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
     * 정산 목록 조회
     *
     * @param year  정산 연도
     * @param month 정산 월
     * @return 정산 목록 조회 응답 {@link GetSettlementsResponse}
     * @Author 성효빈
     * @Date 2026-02-16
     * @Description 연/월 기준으로 정산 목록을 조회합니다.
     */
    @GetMapping
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
    @GetMapping("/{id}")
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
    @GetMapping("/{id}/allocations")
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
}
