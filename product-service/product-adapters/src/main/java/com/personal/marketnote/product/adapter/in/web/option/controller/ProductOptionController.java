package com.personal.marketnote.product.adapter.in.web.option.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.AuthorityValidator;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.product.adapter.in.web.option.controller.apidocs.DeleteProductOptionsApiDocs;
import com.personal.marketnote.product.adapter.in.web.option.controller.apidocs.GetProductOptionsApiDocs;
import com.personal.marketnote.product.adapter.in.web.option.controller.apidocs.RegisterProductOptionsApiDocs;
import com.personal.marketnote.product.adapter.in.web.option.controller.apidocs.UpdateProductOptionsApiDocs;
import com.personal.marketnote.product.adapter.in.web.option.request.UpdateProductOptionsRequest;
import com.personal.marketnote.product.adapter.in.web.option.response.GetProductOptionsResponse;
import com.personal.marketnote.product.adapter.in.web.option.response.UpsertProductOptionsResponse;
import com.personal.marketnote.product.adapter.in.web.product.mapper.ProductRequestToCommandMapper;
import com.personal.marketnote.product.port.in.result.option.GetProductOptionsResult;
import com.personal.marketnote.product.port.in.result.option.UpdateProductOptionsResult;
import com.personal.marketnote.product.port.in.usecase.option.DeleteProductOptionsUseCase;
import com.personal.marketnote.product.port.in.usecase.option.GetProductOptionsUseCase;
import com.personal.marketnote.product.port.in.usecase.option.RegisterProductOptionsUseCase;
import com.personal.marketnote.product.port.in.usecase.option.UpdateProductOptionsUseCase;
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
 * 상품 옵션 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 상품 옵션 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/products/{productId}")
@Tag(name = "상품 옵션 API", description = "상품 옵션 관련 API")
@RequiredArgsConstructor
@Slf4j
public class ProductOptionController {
    private final GetProductOptionsUseCase getProductOptionsUseCase;
    private final RegisterProductOptionsUseCase registerProductOptionsUseCase;
    private final UpdateProductOptionsUseCase updateProductOptionsUseCase;
    private final DeleteProductOptionsUseCase deleteProductOptionsUseCase;

    /**
     * 상품 옵션 카테고리 등록
     *
     * @param productId 상품 ID
     * @param request   상품 옵션 카테고리 등록 요청
     * @return 상품 옵션 카테고리 등록 응답 {@link UpsertProductOptionsResponse}
     * @Author 성효빈
     * @Date 2026-01-01
     * @Description 상품 옵션 카테고리를 등록합니다.
     */
    @PostMapping("/option-categories")
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @RegisterProductOptionsApiDocs
    public ResponseEntity<BaseResponse<UpsertProductOptionsResponse>> registerProductOptionCategories(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateProductOptionsRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        UpdateProductOptionsResult result = registerProductOptionsUseCase.registerProductOptions(
                ElementExtractor.extractUserId(principal),
                AuthorityValidator.hasAdminRole(principal),
                ProductRequestToCommandMapper.mapToCommand(productId, request));

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpsertProductOptionsResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "상품 카테고리 및 옵션 목록 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 상품 옵션 카테고리 및 옵션 목록 조회
     *
     * @param productId 상품 ID
     * @return 옵션 카테고리 및 옵션 목록 응답 {@link GetProductOptionsResponse}
     * @Author 성효빈
     * @Date 2026-01-01
     * @Description 특정 상품의 옵션 카테고리 및 하위 옵션 목록을 조회합니다.
     */
    @GetMapping("/option-categories")
    @GetProductOptionsApiDocs
    public ResponseEntity<BaseResponse<GetProductOptionsResponse>> getProductOptions(
            @PathVariable("productId") Long productId
    ) {
        GetProductOptionsResult result = getProductOptionsUseCase.getProductOptions(productId);

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetProductOptionsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 옵션 카테고리 및 옵션 목록 조회 성공"
                )
        );
    }

    /**
     * 상품 옵션 카테고리 수정
     *
     * @param productId 상품 ID
     * @param id        상품 옵션 카테고리 ID
     * @param request   상품 옵션 카테고리 수정 요청
     * @return 상품 옵션 카테고리 수정 응답 {@link UpsertProductOptionsResponse}
     * @Author 성효빈
     * @Date 2026-01-01
     * @Description 상품 옵션 카테고리를 수정합니다.
     */
    @PutMapping("/option-categories/{id}")
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @UpdateProductOptionsApiDocs
    public ResponseEntity<BaseResponse<UpsertProductOptionsResponse>> updateProductOptionCategories(
            @PathVariable("productId") Long productId,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateProductOptionsRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        UpdateProductOptionsResult result = updateProductOptionsUseCase.updateProductOptions(
                ElementExtractor.extractUserId(principal),
                AuthorityValidator.hasAdminRole(principal),
                ProductRequestToCommandMapper.mapToUpdateCommand(productId, id, request)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        UpsertProductOptionsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 옵션 카테고리 수정 성공"
                )
        );
    }

    /**
     * 상품 옵션 카테고리 삭제
     *
     * @param productId 상품 ID
     * @param id        상품 옵션 카테고리 ID
     * @return 상품 옵션 카테고리 삭제 응답
     * @Author 성효빈
     * @Date 2026-01-01
     * @Description 상품 옵션 카테고리를 삭제합니다.
     */
    @DeleteMapping("/option-categories/{id}")
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @DeleteProductOptionsApiDocs
    public ResponseEntity<BaseResponse<Void>> deleteProductOptionCategories(
            @PathVariable("productId") Long productId,
            @PathVariable("id") Long id,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        deleteProductOptionsUseCase.deleteProductOptions(
                ElementExtractor.extractUserId(principal),
                AuthorityValidator.hasAdminRole(principal),
                productId,
                id
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 옵션 삭제 성공"
                )
        );
    }
}
