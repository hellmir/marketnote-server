package com.personal.marketnote.product.adapter.in.web.category.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.AuthorityValidator;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.product.adapter.in.web.category.controller.apidocs.DeleteCategoryApiDocs;
import com.personal.marketnote.product.adapter.in.web.category.controller.apidocs.GetCategoriesApiDocs;
import com.personal.marketnote.product.adapter.in.web.category.controller.apidocs.RegisterCategoryApiDocs;
import com.personal.marketnote.product.adapter.in.web.category.controller.apidocs.RegisterProductCategoriesApiDocs;
import com.personal.marketnote.product.adapter.in.web.category.mapper.CategoryRequestToCommandMapper;
import com.personal.marketnote.product.adapter.in.web.category.request.RegisterCategoryRequest;
import com.personal.marketnote.product.adapter.in.web.category.request.RegisterProductCategoriesRequest;
import com.personal.marketnote.product.adapter.in.web.category.response.GetCategoriesResponse;
import com.personal.marketnote.product.adapter.in.web.category.response.RegisterCategoryResponse;
import com.personal.marketnote.product.adapter.in.web.category.response.RegisterProductCategoriesResponse;
import com.personal.marketnote.product.adapter.in.web.product.mapper.ProductRequestToCommandMapper;
import com.personal.marketnote.product.port.in.command.DeleteCategoryCommand;
import com.personal.marketnote.product.port.in.result.category.GetCategoriesResult;
import com.personal.marketnote.product.port.in.result.category.RegisterCategoryResult;
import com.personal.marketnote.product.port.in.result.category.RegisterProductCategoriesResult;
import com.personal.marketnote.product.port.in.usecase.category.DeleteCategoryUseCase;
import com.personal.marketnote.product.port.in.usecase.category.GetCategoryUseCase;
import com.personal.marketnote.product.port.in.usecase.category.RegisterCategoryUseCase;
import com.personal.marketnote.product.port.in.usecase.category.RegisterProductCategoriesUseCase;
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
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 카테고리 컨트롤러
 *
 * @Author 성효빈
 * @Date 2025-12-31
 * @Description 카테고리 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "카테고리 API", description = "카테고리 관련 API")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    private final RegisterCategoryUseCase registerCategoryUseCase;
    private final RegisterProductCategoriesUseCase registerProductCategoriesUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    /**
     * 카테고리 등록
     *
     * @param request 카테고리 등록 요청
     * @return 카테고리 등록 응답 {@link RegisterCategoryResponse}
     * @Author 성효빈
     * @Date 2026-01-01
     * @Description 카테고리를 등록합니다.
     */
    @PostMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterCategoryApiDocs
    public ResponseEntity<BaseResponse<RegisterCategoryResponse>> registerCategory(
            @jakarta.validation.Valid @RequestBody RegisterCategoryRequest request
    ) {
        RegisterCategoryResult result = registerCategoryUseCase.registerCategory(
                CategoryRequestToCommandMapper.mapToCommand(request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterCategoryResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "카테고리 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 카테고리 목록 조회
     *
     * @param parentCategoryId 상위 카테고리 ID
     * @return 카테고리 목록 조회 응답 {@link GetCategoriesResponse}
     * @Author 성효빈
     * @Date 2026-01-01
     * @Description 카테고리 목록을 조회합니다.
     */
    @GetMapping
    @GetCategoriesApiDocs
    public ResponseEntity<BaseResponse<GetCategoriesResponse>> getCategories(
            @RequestParam(value = "parentId", required = false) Long parentCategoryId
    ) {
        GetCategoriesResult result = getCategoryUseCase.getCategoriesByParentId(parentCategoryId);

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetCategoriesResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "카테고리 목록 조회 성공"
                )
        );
    }

    /**
     * 카테고리 삭제
     *
     * @param categoryId 카테고리 ID
     * @return 카테고리 삭제 응답
     * @Author 성효빈
     * @Date 2026-01-01
     * @Description 카테고리를 삭제합니다.
     */
    @DeleteMapping("/{categoryId}")
    @PreAuthorize(ADMIN_POINTCUT)
    @DeleteCategoryApiDocs
    public ResponseEntity<BaseResponse<Void>> deleteCategory(
            @PathVariable("categoryId") Long categoryId
    ) {
        deleteCategoryUseCase.deleteCategory(
                DeleteCategoryCommand.of(categoryId)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "카테고리 삭제 성공"
                )
        );
    }

    /**
     * 상품 카테고리 등록/수정
     *
     * @param productId 상품 ID
     * @param request   상품 카테고리 등록 요청
     * @return 상품 카테고리 등록 응답 {@link RegisterProductCategoriesResponse}
     * @Author 성효빈
     * @Date 2026-01-01
     * @Description 상품 카테고리를 등록합니다.
     */
    @PutMapping("{productId}/categories")
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @RegisterProductCategoriesApiDocs
    public ResponseEntity<BaseResponse<RegisterProductCategoriesResponse>> registerProductCategories(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody RegisterProductCategoriesRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        RegisterProductCategoriesResult result = registerProductCategoriesUseCase.registerProductCategories(
                ElementExtractor.extractUserId(principal),
                AuthorityValidator.hasAdminRole(principal),
                ProductRequestToCommandMapper.mapToCommand(productId, request)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        RegisterProductCategoriesResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 카테고리 등록 성공"
                )
        );
    }
}
