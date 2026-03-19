package com.personal.marketnote.product.adapter.in.web.shipping.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.product.adapter.in.web.shipping.controller.apidocs.GetShippingPolicyApiDocs;
import com.personal.marketnote.product.adapter.in.web.shipping.controller.apidocs.RegisterShippingPolicyApiDocs;
import com.personal.marketnote.product.adapter.in.web.shipping.controller.apidocs.UpdateShippingPolicyApiDocs;
import com.personal.marketnote.product.adapter.in.web.shipping.request.RegisterShippingPolicyRequest;
import com.personal.marketnote.product.adapter.in.web.shipping.request.UpdateShippingPolicyRequest;
import com.personal.marketnote.product.adapter.in.web.shipping.response.GetShippingPolicyResponse;
import com.personal.marketnote.product.adapter.in.web.shipping.response.RegisterShippingPolicyResponse;
import com.personal.marketnote.product.adapter.in.web.shipping.response.UpdateShippingPolicyResponse;
import com.personal.marketnote.product.port.in.command.RegisterShippingPolicyCommand;
import com.personal.marketnote.product.port.in.command.UpdateShippingPolicyCommand;
import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyResult;
import com.personal.marketnote.product.port.in.result.shipping.RegisterShippingPolicyResult;
import com.personal.marketnote.product.port.in.result.shipping.UpdateShippingPolicyResult;
import com.personal.marketnote.product.port.in.usecase.shipping.GetShippingPolicyUseCase;
import com.personal.marketnote.product.port.in.usecase.shipping.RegisterShippingPolicyUseCase;
import com.personal.marketnote.product.port.in.usecase.shipping.UpdateShippingPolicyUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    private final GetShippingPolicyUseCase getShippingPolicyUseCase;
    private final RegisterShippingPolicyUseCase registerShippingPolicyUseCase;
    private final UpdateShippingPolicyUseCase updateShippingPolicyUseCase;

    /**
     * (판매자/관리자) 배송비 정책 조회
     *
     * @param principal 인증된 사용자 정보
     * @return 배송비 정책 조회 응답 {@link GetShippingPolicyResponse}
     * @Author 성효빈
     * @Date 2026-03-19
     * @Description 판매자의 배송비 정책을 조회합니다.
     */
    @GetMapping
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @GetShippingPolicyApiDocs
    public ResponseEntity<BaseResponse<GetShippingPolicyResponse>> getShippingPolicy(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long sellerId = ElementExtractor.extractUserId(principal);

        GetShippingPolicyResult result = getShippingPolicyUseCase.getShippingPolicy(sellerId);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetShippingPolicyResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "판매자 배송비 정책 조회 성공"
                ),
                HttpStatus.OK
        );
    }

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

    /**
     * (판매자/관리자) 배송비 정책 수정
     *
     * @param request   배송비 정책 수정 요청
     * @param principal 인증된 사용자 정보
     * @return 배송비 정책 수정 응답 {@link UpdateShippingPolicyResponse}
     * @Author 성효빈
     * @Date 2026-03-19
     * @Description 판매자의 배송비 정책을 수정합니다.
     */
    @PutMapping
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @UpdateShippingPolicyApiDocs
    public ResponseEntity<BaseResponse<UpdateShippingPolicyResponse>> updateShippingPolicy(
            @Valid @RequestBody UpdateShippingPolicyRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long sellerId = ElementExtractor.extractUserId(principal);

        UpdateShippingPolicyResult result = updateShippingPolicyUseCase.updateShippingPolicy(
                sellerId,
                new UpdateShippingPolicyCommand(
                        request.deliveryCompany(),
                        request.shippingFee(),
                        request.freeShippingThreshold()
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpdateShippingPolicyResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "판매자 배송비 정책 수정 성공"
                ),
                HttpStatus.OK
        );
    }
}
