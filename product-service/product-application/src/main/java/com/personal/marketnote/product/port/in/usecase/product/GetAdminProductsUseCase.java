package com.personal.marketnote.product.port.in.usecase.product;

import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.port.in.result.product.GetAdminProductsResult;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 관리자 상품 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 관리자 상품 목록 조회 기능을 제공합니다.
 */
public interface GetAdminProductsUseCase {
    /**
     * @param categoryId     카테고리 ID
     * @param pricePolicyIds 가격 정책 ID 목록
     * @param cursor         커서(무한 스크롤 페이지 설정)
     * @param pageSize       페이지 크기
     * @param sortDirection  정렬 방향
     * @param sortProperty   정렬 속성
     * @param searchTarget   검색 대상
     * @param searchKeyword  검색 키워드
     * @return 관리자 상품 목록 조회 결과 {@link GetAdminProductsResult}
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 관리자 상품 목록을 조회합니다.
     */
    GetAdminProductsResult getAdminProducts(
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
