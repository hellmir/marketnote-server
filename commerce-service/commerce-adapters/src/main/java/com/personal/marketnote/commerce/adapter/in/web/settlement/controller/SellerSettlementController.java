package com.personal.marketnote.commerce.adapter.in.web.settlement.controller;

import com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs.GetSellerSettlementsApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.settlement.mapper.SellerSettlementRequestToQueryMapper;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSellerSettlementsResponse;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetSellerSettlementsUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_OR_SELLER_POINTCUT;

/**
 * 판매자 정산 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 판매자 본인의 정산 내역 조회 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/sellers/me/settlements")
@Tag(name = "(판매자) 정산 API", description = "판매자 정산 관련 API")
@RequiredArgsConstructor
@Validated
public class SellerSettlementController {
    private final GetSellerSettlementsUseCase getSellerSettlementsUseCase;

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
    @GetMapping
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
