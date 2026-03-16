package com.personal.marketnote.product.adapter.in.web.product.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.AuthorityValidator;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.in.web.product.controller.apidocs.*;
import com.personal.marketnote.product.adapter.in.web.product.mapper.ProductRequestToCommandMapper;
import com.personal.marketnote.product.adapter.in.web.product.request.RegisterProductRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.ReorderProductTagsRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.UpdateProductRequest;
import com.personal.marketnote.product.adapter.in.web.product.response.*;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.port.in.command.DeleteProductCommand;
import com.personal.marketnote.product.port.in.command.DeleteProductImageCommand;
import com.personal.marketnote.product.port.in.result.product.*;
import com.personal.marketnote.product.port.in.usecase.product.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.*;

/**
 * 상품 컨트롤러
 *
 * @Author 성효빈
 * @Date 2025-12-30
 * @Description 상품 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "상품 API", description = "상품 관련 API")
@RequiredArgsConstructor
public class ProductController {
    private static final String GET_PRODUCTS_DEFAULT_PAGE_SIZE = "4";

    private final RegisterProductUseCase registerProductUseCase;
    private final GetProductSortPropertiesUseCase getProductSortPropertiesUseCase;
    private final GetProductSearchTargetsUseCase getProductSearchTargetsUseCase;
    private final GetProductUseCase getProductUseCase;
    private final GetAdminProductsUseCase getAdminProductsUseCase;
    private final GetAdminProductDetailUseCase getAdminProductDetailUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final ReorderProductTagsUseCase reorderProductTagsUseCase;
    private final DeleteProductImageUseCase deleteProductImageUseCase;

    /**
     * (판매자/관리자) 상품 등록
     *
     * @param request 상품 등록 요청
     * @return 상품 등록 응답 {@link RegisterProductResponse}
     * @Author 성효빈
     * @Date 2025-12-30
     * @Description 상품을 등록합니다.
     */
    @PostMapping
    @PreAuthorize(ADMIN_OR_SELLER_PRINCIPAL_POINTCUT)
    @RegisterProductApiDocs
    public ResponseEntity<BaseResponse<RegisterProductResponse>> registerProduct(
            @Valid @RequestBody RegisterProductRequest request
    ) {
        RegisterProductResult result = registerProductUseCase.registerProduct(
                ProductRequestToCommandMapper.mapToCommand(request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterProductResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "상품 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 상품 정렬 속성 목록 조회
     *
     * @return 상품 정렬 속성 목록 조회 응답 {@link GetProductSortPropertiesResponse}
     * @Author 성효빈
     * @Date 2026-01-02
     * @Description 상품 정렬 속성 목록을 조회합니다.
     */
    @GetMapping("/sort-properties")
    @GetProductSortPropertiesApiDocs
    public ResponseEntity<BaseResponse<GetProductSortPropertiesResponse>> getProductSortProperties() {
        GetProductSortPropertiesResult result = getProductSortPropertiesUseCase.getProductSortProperties();

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetProductSortPropertiesResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 정렬 속성 목록 조회 성공"
                )
        );
    }

    /**
     * 상품 검색 대상 목록 조회
     *
     * @return 상품 검색 대상 목록 조회 응답 {@link GetProductSearchTargetsResponse}
     * @Author 성효빈
     * @Date 2026-01-02
     * @Description 상품 검색 대상 목록을 조회합니다.
     */
    @GetMapping("/search-targets")
    @GetProductSearchTargetsApiDocs
    public ResponseEntity<BaseResponse<GetProductSearchTargetsResponse>> getProductSearchTargets() {
        GetProductSearchTargetsResult result = getProductSearchTargetsUseCase.getProductSearchTargets();

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetProductSearchTargetsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 검색 대상 목록 조회 성공"
                )
        );
    }

    /**
     * 상품 목록 조회
     *
     * @param categoryId     카테고리 ID
     * @param pricePolicyIds 가격 정책 ID 목록
     * @param cursor         커서(무한 스크롤 페이지 설정)
     * @param pageSize       페이지 크기
     * @param sortDirection  정렬 방향
     * @param sortProperty   정렬 속성
     * @param searchTarget   검색 대상
     * @param searchKeyword  검색 키워드
     * @return 상품 목록 조회 응답 {@link GetProductsResponse}
     * @Author 성효빈
     * @Date 2025-12-31
     * @Description 상품 목록을 조회합니다.
     */
    @GetMapping
    @GetProductsApiDocs
    public ResponseEntity<BaseResponse<GetProductsResponse>> getProducts(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "pricePolicyIds", required = false) List<Long> pricePolicyIds,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "pageSize", required = false, defaultValue = GET_PRODUCTS_DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "ORDER_NUM") ProductSortProperty sortProperty,
            @RequestParam(required = false, defaultValue = "NAME") ProductSearchTarget searchTarget,
            @RequestParam(required = false) String searchKeyword
    ) {
        GetProductsResult result = getProductUseCase.getProducts(
                categoryId,
                pricePolicyIds,
                cursor,
                pageSize,
                sortDirection,
                sortProperty,
                searchTarget,
                searchKeyword
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetProductsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 목록 조회 성공"
                )
        );
    }

    /**
     * (관리자) 상품 목록 조회
     *
     * @param categoryId     카테고리 ID
     * @param pricePolicyIds 가격 정책 ID 목록
     * @param cursor         커서(무한 스크롤 페이지 설정)
     * @param pageSize       페이지 크기
     * @param sortDirection  정렬 방향
     * @param sortProperty   정렬 속성
     * @param searchTarget   검색 대상
     * @param searchKeyword  검색 키워드
     * @return 관리자 상품 목록 조회 응답 {@link GetAdminProductsResponse}
     * @Author 성효빈
     * @Date 2026-02-03
     * @Description 관리자 상품 목록을 조회합니다.
     */
    @GetMapping("/admin")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetAdminProductsApiDocs
    public ResponseEntity<BaseResponse<GetAdminProductsResponse>> getAdminProducts(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "pricePolicyIds", required = false) List<Long> pricePolicyIds,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "pageSize", required = false, defaultValue = GET_PRODUCTS_DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "ORDER_NUM") ProductSortProperty sortProperty,
            @RequestParam(required = false, defaultValue = "NAME") ProductSearchTarget searchTarget,
            @RequestParam(required = false) String searchKeyword
    ) {
        GetAdminProductsResult result = getAdminProductsUseCase.getAdminProducts(
                categoryId,
                pricePolicyIds,
                cursor,
                pageSize,
                sortDirection,
                sortProperty,
                searchTarget,
                searchKeyword
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetAdminProductsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "관리자 상품 목록 조회 성공"
                )
        );
    }

    /**
     * (관리자) 상품 상세 정보 조회(파스토 연동)
     *
     * @param id                상품 ID
     * @param selectedOptionIds 선택된 옵션 ID 목록
     * @return 관리자 상품 상세 정보 조회 응답 {@link GetAdminProductDetailResponse}
     * @Author 성효빈
     * @Date 2026-02-03
     * @Description 관리자 상품 상세 정보를 조회합니다. 파스토 상품/모음상품 정보를 함께 반환합니다.
     */
    @GetMapping("/admin/{id}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetAdminProductDetailApiDocs
    public ResponseEntity<BaseResponse<GetAdminProductDetailResponse>> getAdminProductDetail(
            @PathVariable("id") Long id,
            @RequestParam(value = "selectedOptionIds", required = false) List<Long> selectedOptionIds
    ) {
        if (FormatValidator.hasNoValue(selectedOptionIds)) {
            selectedOptionIds = List.of();
        }

        GetAdminProductDetailResult result = getAdminProductDetailUseCase.getAdminProductDetail(
                id,
                selectedOptionIds
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetAdminProductDetailResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "관리자 상품 상세 정보 조회 성공"
                )
        );
    }

    /**
     * (비회원) 가격 정책 ID로 상품 상세 정보 조회
     *
     * @param pricePolicyId 가격 정책 ID
     * @return 상품 상세 정보 조회 응답 {@link GetProductInfoResponse}
     * @Author 성효빈
     * @Date 2026-01-27
     * @Description 가격 정책 ID로 상품 상세 정보를 조회합니다.
     */
    @GetMapping("/price-policies/{pricePolicyId}")
    @GetProductInfoByPricePolicyIdApiDocs
    public ResponseEntity<BaseResponse<GetProductInfoResponse>> getProductInfoByPricePolicyId(
            @PathVariable("pricePolicyId") Long pricePolicyId
    ) {
        GetProductInfoWithOptionsResult getProductInfoWithOptionsResult
                = getProductUseCase.getProductInfo(pricePolicyId);

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetProductInfoResponse.from(getProductInfoWithOptionsResult),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 상세 정보 조회 성공"
                )
        );
    }

    /**
     * (비회원) 상품 상세 정보 조회
     *
     * @param id                상품 ID
     * @param selectedOptionIds 선택된 옵션 ID 목록
     * @return 상품 상세 정보 조회 응답 {@link GetProductInfoResponse}
     * @Author 성효빈
     * @Date 2026-01-02
     * @Description 상품 상세 정보를 조회합니다.
     */
    @GetMapping("/{id}")
    @GetProductInfoApiDocs
    public ResponseEntity<BaseResponse<GetProductInfoResponse>> getProductInfo(
            @PathVariable("id") Long id,
            @RequestParam(value = "selectedOptionIds", required = false) List<Long> selectedOptionIds
    ) {
        if (FormatValidator.hasNoValue(selectedOptionIds)) {
            selectedOptionIds = List.of();
        }
        GetProductInfoWithOptionsResult getProductInfoWithOptionsResult
                = getProductUseCase.getProductInfo(id, selectedOptionIds);

        return ResponseEntity.ok(
                BaseResponse.of(
                        GetProductInfoResponse.from(getProductInfoWithOptionsResult),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 상세 정보 조회 성공"
                )
        );
    }

    /**
     * (판매자/관리자) 상품 정보 수정
     *
     * @param id      상품 ID
     * @param request 상품 정보 수정 요청
     * @Author 성효빈
     * @Date 2026-01-01
     * @Description 상품 정보를 수정합니다. 연동된 파스토 상품 정보도 함께 수정합니다.
     */
    @PutMapping("/{id}")
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @UpdateProductApiDocs
    public ResponseEntity<BaseResponse<Void>> updateProduct(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        updateProductUseCase.update(
                ElementExtractor.extractUserId(principal),
                AuthorityValidator.hasAdminRole(principal),
                ProductRequestToCommandMapper.mapToCommand(id, request)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 정보 수정 성공"
                )
        );
    }

    /**
     * (판매자/관리자) 상품 태그 순서 변경
     *
     * @param id        상품 ID
     * @param request   상품 태그 순서 변경 요청
     * @param principal 인증된 사용자
     * @Author 성효빈
     * @Date 2026-03-17
     * @Description 상품 태그의 순서를 변경합니다.
     */
    @PatchMapping("/{id}/tags/order")
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @ReorderProductTagsApiDocs
    public ResponseEntity<BaseResponse<Void>> reorderProductTags(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReorderProductTagsRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        reorderProductTagsUseCase.reorderProductTags(
                ElementExtractor.extractUserId(principal),
                AuthorityValidator.hasAdminRole(principal),
                ProductRequestToCommandMapper.mapToCommand(id, request)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 태그 순서 변경 성공"
                )
        );
    }

    /**
     * (판매자/관리자) 상품 이미지 삭제
     *
     * @param id 상품 ID
     * @Author 성효빈
     * @Date 2026-01-07
     * @Description 상품 이미지를 삭제합니다.
     */
    @DeleteMapping("/{id}/images/{fileId}")
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @DeleteProductImageApiDocs
    public ResponseEntity<BaseResponse<Void>> deleteProductImage(
            @PathVariable("id") Long id,
            @PathVariable("fileId") Long fileId,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        deleteProductImageUseCase.delete(
                ElementExtractor.extractUserId(principal),
                AuthorityValidator.hasAdminRole(principal),
                DeleteProductImageCommand.of(id, fileId)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 이미지 삭제 성공"
                )
        );
    }

    /**
     * (판매자/관리자) 상품 삭제
     *
     * @param id 상품 ID
     * @Author 성효빈
     * @Date 2026-01-02
     * @Description 상품을 삭제합니다.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_OR_SELLER_POINTCUT)
    @DeleteProductApiDocs
    public ResponseEntity<BaseResponse<Void>> deleteProduct(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        deleteProductUseCase.delete(
                ElementExtractor.extractUserId(principal),
                AuthorityValidator.hasAdminRole(principal),
                DeleteProductCommand.of(id)
        );

        return ResponseEntity.ok(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "상품 삭제 성공"
                )
        );
    }
}
