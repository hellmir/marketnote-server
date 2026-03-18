package com.personal.marketnote.product.adapter.in.web.shipping.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.product.adapter.in.web.shipping.controller.apidocs.RegisterShippingPolicyApiDocs;
import com.personal.marketnote.product.adapter.in.web.shipping.request.RegisterShippingPolicyRequest;
import com.personal.marketnote.product.adapter.in.web.shipping.response.RegisterShippingPolicyResponse;
import com.personal.marketnote.product.port.in.command.RegisterShippingPolicyCommand;
import com.personal.marketnote.product.port.in.result.shipping.RegisterShippingPolicyResult;
import com.personal.marketnote.product.port.in.usecase.shipping.RegisterShippingPolicyUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_OR_SELLER_POINTCUT;

/**
 * 배송비 정책 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-03-17
 * @Description 판매자 배송비 정책 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/shipping-policies")
@Tag(name = "배송비 정책 API", description = "판매자 배송비 정책 관련 API")
@RequiredArgsConstructor
@Slf4j
public class ShippingPolicyController {

    private final RegisterShippingPolicyUseCase registerShippingPolicyUseCase;

    /**
     * (판매자/관리자) 배송비 정책 등록
     *
     * @param request   배송비 정책 등록 요청
     * @param principal 인증된 사용자 정보
     * @return 배송비 정책 등록 응답 {@link RegisterShippingPolicyResponse}
     * @Author 성효빈
     * @Date 2026-03-17
     * @Description 판매자의 배송비 정책을 등록합니다.
     */
    @PostMapping
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @RegisterShippingPolicyApiDocs
    public ResponseEntity<BaseResponse<RegisterShippingPolicyResponse>> registerShippingPolicy(
            @Valid @RequestBody RegisterShippingPolicyRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long sellerId = ElementExtractor.extractUserId(principal);

        RegisterShippingPolicyResult result = registerShippingPolicyUseCase.registerShippingPolicy(
                sellerId,
                new RegisterShippingPolicyCommand(
                        request.deliveryCompany(),
                        request.shippingFee(),
                        request.freeShippingThreshold()
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterShippingPolicyResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "판매자 배송비 정책 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }
}
