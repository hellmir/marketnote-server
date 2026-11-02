package com.personal.marketnote.product.port.in.usecase.product;

import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.port.in.result.product.GetProductInfoWithOptionsResult;
import com.personal.marketnote.product.port.in.result.product.GetProductsResult;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 상품 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2025-12-31
 * @Description 상품 조회 관련 기능을 제공합니다.
 */
public interface GetProductUseCase {
    /**
     * @param id 상품 ID
     * @return 상품 도메인 {@link Product}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 상품을 조회합니다.
     */
    Product getProduct(Long id);

    /**
     * @param pricePolicyId 가격 정책 ID
     * @return 상품 상세 정보 응답 {@link GetProductInfoWithOptionsResult}
     * @Date 2026-01-27
     * @Author 성효빈
     * @Description 상품 상세 정보를 조회합니다.
     */
    GetProductInfoWithOptionsResult getProductInfo(Long pricePolicyId);

    /**
     * @param id                상품 ID
     * @param selectedOptionIds 선택된 옵션 ID 목록
     * @return 상품 상세 정보 응답 {@link GetProductInfoWithOptionsResult}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 활성화 상태의 상품 상세 정보를 조회합니다.
     */
    GetProductInfoWithOptionsResult getProductInfo(Long id, List<Long> selectedOptionIds);

    /**
     * @param id                상품 ID
     * @param selectedOptionIds 선택된 옵션 ID 목록
     * @return 상품 상세 정보 응답 {@link GetProductInfoWithOptionsResult}
     * @Date 2026-02-23
     * @Author 성효빈
     * @Description 비활성화/비노출 상품을 포함하여 상품 상세 정보를 조회합니다. (관리자용)
     */
    GetProductInfoWithOptionsResult getProductInfoIncludingInactive(Long id, List<Long> selectedOptionIds);

    /**
     * @param categoryId     카테고리 ID
     * @param pricePolicyIds 가격 정책 ID 목록
     * @param cursor         커서(무한 스크롤 페이지 설정)
     * @param pageSize       페이지 크기
     * @param sortDirection  정렬 방향
     * @param sortProperty   정렬 속성
     * @param searchTarget   검색 대상
     * @param searchKeyword  검색 키워드
     * @return 상품 목록 조회 결과 {@link GetProductsResult}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 상품 목록을 조회합니다.
     */
    GetProductsResult getProducts(
            Long categoryId,
            List<Long> pricePolicyIds,
            Long cursor,
            int pageSize,
            Sort.Direction sortDirection,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword
    );
}
