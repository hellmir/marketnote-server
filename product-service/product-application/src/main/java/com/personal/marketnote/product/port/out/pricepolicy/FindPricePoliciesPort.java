package com.personal.marketnote.product.port.out.pricepolicy;

import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 가격 정책 목록 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-02
 * @Description 가격 정책 목록 조회 관련 기능을 제공합니다.
 */
public interface FindPricePoliciesPort {
    /**
     * @param productId 상품 ID
     * @return 가격 정책 목록 {@link List<PricePolicy>}
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 상품 ID로 가격 정책 목록을 조회합니다.
     */
    List<PricePolicy> findByProductId(Long productId);

    /**
     * @param ids 가격 정책 ID 목록
     * @return 가격 정책 목록 {@link List<PricePolicy>}
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 가격 정책 ID 목록으로 가격 정책을 조회합니다.
     */
    List<PricePolicy> findByIds(List<Long> ids);

    /**
     * @param pricePolicyIds 가격 정책 ID 목록
     * @param cursor         커서
     * @param pageable       페이지네이션 정보
     * @param sortProperty   정렬 속성
     * @param searchTarget   검색 대상
     * @param searchKeyword  검색 키워드
     * @return 가격 정책 목록 {@link List<PricePolicy>}
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 가격 정책 목록을 조회합니다(페이지네이션 적용).
     */
    List<PricePolicy> findPricePolicies(
            List<Long> pricePolicyIds,
            Long cursor,
            Pageable pageable,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword
    );

    /**
     * @param categoryId     카테고리 ID
     * @param pricePolicyIds 가격 정책 ID 목록
     * @param cursor         커서
     * @param pageable       페이지네이션 정보
     * @param sortProperty   정렬 속성
     * @param searchTarget   검색 대상
     * @param searchKeyword  검색 키워드
     * @return 카테고리별 가격 정책 목록 {@link List<PricePolicy>}
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 카테고리별 가격 정책 목록을 조회합니다(페이지네이션 적용).
     */
    List<PricePolicy> findPricePoliciesByCategoryId(
            Long categoryId,
            List<Long> pricePolicyIds,
            Long cursor,
            Pageable pageable,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword
    );

    /**
     * @param categoryId    카테고리 ID
     * @param searchTarget  검색 대상
     * @param searchKeyword 검색 키워드
     * @return 카테고리 가격 정책 총 개수 {@link long}
     * @Date 2026-01-02
     * @Author 성효빈
     * @Description 카테고리 가격 정책 총 개수를 조회합니다.
     */
    long countActivePricePoliciesByCategoryId(Long categoryId, ProductSearchTarget searchTarget, String searchKeyword);

    List<PricePolicy> findPricePoliciesByOffset(
            List<Long> pricePolicyIds,
            int page,
            int pageSize,
            Sort.Direction sortDirection,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword,
            Long categoryId
    );
}
