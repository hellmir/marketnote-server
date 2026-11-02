package com.personal.marketnote.product.adapter.in.web.pricepolicy.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.AuthorityValidator;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.product.adapter.in.web.pricepolicy.controller.apidocs.DeletePricePolicyApiDocs;
import com.personal.marketnote.product.adapter.in.web.pricepolicy.controller.apidocs.GetPricePoliciesApiDocs;
import com.personal.marketnote.product.adapter.in.web.pricepolicy.controller.apidocs.RegisterPricePolicyApiDocs;
import com.personal.marketnote.product.adapter.in.web.pricepolicy.request.RegisterPricePolicyRequest;
import com.personal.marketnote.product.adapter.in.web.pricepolicy.response.GetPricePoliciesResponse;
import com.personal.marketnote.product.adapter.in.web.pricepolicy.response.RegisterPricePolicyResponse;
import com.personal.marketnote.product.port.in.command.RegisterPricePolicyCommand;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetPricePoliciesResult;
import com.personal.marketnote.product.port.in.result.pricepolicy.RegisterPricePolicyResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.DeletePricePolicyUseCase;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePoliciesUseCase;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.RegisterPricePolicyUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_OR_SELLER_POINTCUT;

/**
 * 가격 정책 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 상품 가격 정책 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/products/{productId}/price-policies")
@Tag(name = "상품 가격 정책 API", description = "상품 가격 정책 관련 API")
@RequiredArgsConstructor
@Slf4j
public class PricePolicyController {
    private final RegisterPricePolicyUseCase registerPricePolicyUseCase;
    private final GetPricePoliciesUseCase getPricePoliciesUseCase;
    private final DeletePricePolicyUseCase deletePricePolicyUseCase;

    /**
     * (판매자/관리자) 상품 가격 정책 등록
     *
     * @param productId 상품 ID
     * @param request   상품 가격 정책 등록 요청
     * @return 상품 가격 정책 등록 응답 {@link RegisterPricePolicyResponse}
     * @Author 성효빈
     * @Date 2026-01-18
     * @Description 상품 가격 정책을 등록합니다.
     */
    @PostMapping
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @RegisterPricePolicyApiDocs
    public ResponseEntity<BaseResponse<RegisterPricePolicyResponse>> registerPricePolicy(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody RegisterPricePolicyRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        RegisterPricePolicyResult result = registerPricePolicyUseCase.registerPricePolicy(
                ElementExtractor.extractUserId(principal),
                AuthorityValidator.hasAdminRole(principal),
                RegisterPricePolicyCommand.of(
                        productId,
                        request.getPrice(),
                        request.getDiscountPrice(),
                        request.getAccumulatedPoint(),
                        request.getOptionIds()
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterPricePolicyResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "상품 가격 정책 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * (비회원) 상품 가격 정책 목록 조회
     *
     * @param productId 상품 ID
     * @return 상품 가격 정책 목록 조회 응답 {@link GetPricePoliciesResponse}
     * @Author 성효빈
     * @Date 2026-01-18
     * @Description 상품 가격 정책 목록을 조회합니다.
     */
    @GetMapping
    @GetPricePoliciesApiDocs
    public ResponseEntity<BaseResponse<GetPricePoliciesResponse>> getPricePolicies(
            @PathVariable("productId") Long productId
    ) {
        GetPricePoliciesResult result = getPricePoliciesUseCase.getPricePoliciesAndOptions(productId);
        return ResponseEntity.ok(
                BaseResponse.of(
                        GetPricePoliciesResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 가격 정책 목록 조회 성공"
                )
        );
    }

    /**
     * 상품 가격 정책 삭제
     *
     * @param productId     상품 ID
     * @param pricePolicyId 가격 정책 ID
     * @return 상품 가격 정책 삭제 응답 {@link Void}
     * @Author 성효빈
     * @Date 2026-01-18
     * @Description 상품 가격 정책을 삭제합니다.
     */
    @DeleteMapping("/{pricePolicyId}")
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @DeletePricePolicyApiDocs
    public ResponseEntity<BaseResponse<Void>> deletePricePolicy(
            @PathVariable("productId") Long productId,
            @PathVariable("pricePolicyId") Long pricePolicyId,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        deletePricePolicyUseCase.delete(
                ElementExtractor.extractUserId(principal),
                AuthorityValidator.hasAdminRole(principal),
                productId,
                pricePolicyId
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 가격 정책 삭제 성공"
                )
        );
    }
}


