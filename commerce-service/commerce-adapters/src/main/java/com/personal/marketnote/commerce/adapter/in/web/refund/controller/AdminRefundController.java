package com.personal.marketnote.commerce.adapter.in.web.refund.controller;

import com.personal.marketnote.commerce.adapter.in.web.refund.controller.apidocs.GetAdminRefundsApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.refund.response.GetAdminRefundResponse;
import com.personal.marketnote.commerce.port.in.result.refund.GetAdminRefundResult;
import com.personal.marketnote.commerce.port.in.usecase.refund.GetAdminRefundsUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 관리자 환불 조회 컨트롤러.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@RestController
@RequestMapping("/api/v1/admin/refunds")
@Tag(name = "(관리자) 환불 API", description = "관리자 환불 조회 API")
@RequiredArgsConstructor
@Validated
public class AdminRefundController {
    private final GetAdminRefundsUseCase getAdminRefundsUseCase;

    /**
     * 주문별 환불 목록을 조회한다.
     *
     * @param orderId 주문 ID
     * @return 환불 목록 응답
     * @author 성효빈
     * @since 2026-03-02
     */
    @GetMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @GetAdminRefundsApiDocs
    public ResponseEntity<BaseResponse<List<GetAdminRefundResponse>>> getRefundsByOrderId(
            @RequestParam("order-id") @NotNull @Min(1) Long orderId
    ) {
        List<GetAdminRefundResult> results = getAdminRefundsUseCase.getRefundsByOrderId(orderId);
        List<GetAdminRefundResponse> responses = results.stream()
                .map(GetAdminRefundResponse::from)
                .toList();
        return new ResponseEntity<>(
                BaseResponse.of(responses, HttpStatus.OK, DEFAULT_SUCCESS_CODE, "환불 목록 조회 성공"),
                HttpStatus.OK
        );
    }
}
