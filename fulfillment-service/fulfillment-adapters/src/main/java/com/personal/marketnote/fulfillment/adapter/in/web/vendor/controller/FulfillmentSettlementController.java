package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.GetFulfillmentSettlementDailyCostsApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FulfillmentSettlementRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentSettlementDailyCostsResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSettlementDailyCostsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentSettlementDailyCostsUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 파스토 정산 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-02-08
 * @Description 파스토 정산 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/vendors/fassto/settlements")
@Tag(name = "파스토 정산 API", description = "파스토 정산 관련 API")
@RequiredArgsConstructor
public class FulfillmentSettlementController {
    private final GetFulfillmentSettlementDailyCostsUseCase getFulfillmentSettlementDailyCostsUseCase;

    /**
     * (관리자) 파스토 물류비 일별 비용 조회
     *
     * @param yearMonth    정산 월(YYYYMM)
     * @param whCd         파스토 센터 코드
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @Author 성효빈
     * @Date 2026-02-08
     * @Description 파스토 물류비 일별 비용을 조회합니다.
     */
    @GetMapping("/daily-costs/{yearMonth}/{whCd}/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentSettlementDailyCostsApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentSettlementDailyCostsResponse>> getDailyCosts(
            @PathVariable String yearMonth,
            @PathVariable String whCd,
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFulfillmentSettlementDailyCostsResult result = getFulfillmentSettlementDailyCostsUseCase.getDailyCosts(
                FulfillmentSettlementRequestToCommandMapper.mapToDailyCostsCommand(yearMonth, whCd, customerCode, accessToken)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentSettlementDailyCostsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 물류비 일별 비용 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
