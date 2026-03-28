package com.personal.marketnote.product.adapter.in.web.shipping.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.product.adapter.in.web.shipping.response.GetShippingPoliciesBySellerIdsResponse;
import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyBySellerResult;
import com.personal.marketnote.product.port.in.usecase.shipping.GetShippingPolicyUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 내부 배송비 정책 컨트롤러 (서비스 간 통신용)
 *
 * @Author 성효빈
 * @Date 2026-03-18
 * @Description HMAC 인증 기반 서비스 간 통신용 배송비 정책 조회 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/internal/shipping-policies")
@Tag(
        name = "내부 배송비 정책 API",
        description = "서비스 간 통신용 배송비 정책 API"
)
@RequiredArgsConstructor
@Validated
@Slf4j
public class InternalShippingPolicyController {

    private final GetShippingPolicyUseCase getShippingPolicyUseCase;

    /**
     * 판매자별 배송비 정책 배치 조회 (서비스 간 통신용)
     *
     * @param sellerIds 판매자 ID 목록
     * @return 배송비 정책 배치 조회 응답 {@link GetShippingPoliciesBySellerIdsResponse}
     */
    @GetMapping("/sellers")
    public ResponseEntity<BaseResponse<GetShippingPoliciesBySellerIdsResponse>> getShippingPoliciesBySellerIds(
            @RequestParam @Size(max = 100, message = "판매자 ID는 최대 100개까지 조회 가능합니다") List<Long> sellerIds
    ) {
        List<GetShippingPolicyBySellerResult> results = getShippingPolicyUseCase.getShippingPolicies(sellerIds);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetShippingPoliciesBySellerIdsResponse.from(results),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "판매자별 배송비 정책 배치 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
