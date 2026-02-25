package com.personal.marketnote.product.port.out.product;

import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 상품 조회 포트
 *
 * @Author 성효빈
 * @Date 2025-12-31
 * @Description 상품 조회 관련 기능을 제공합니다.
 */
public interface FindProductPort {
    /**
     * @param productId 상품 ID
     * @param sellerId  판매자 ID
     * @return 상품 존재 여부 {@link boolean}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 상품의 존재 여부를 확인합니다.
     */
    boolean existsByIdAndSellerId(Long productId, Long sellerId);

    /**
     * @param productId 상품 ID
     * @return 상품 도메인 {@link Product}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 활성화/비활성화/비노출 상품을 조회합니다.
     */
    Optional<Product> findById(Long productId);

    /**
     * @param productId 상품 ID
     * @return 활성화 상태의 상품 도메인 {@link Product}
     * @Date 2026-02-23
     * @Author 성효빈
     * @Description 활성화 상태의 상품만 조회합니다.
     */
    Optional<Product> findActiveById(Long productId);

    /**
     * @return 모든 상품 목록 {@link List<Product>}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 모든 활성화/비활성화/비노출 상품 목록을 조회합니다.
     */
    List<Product> findAll();

    /**
     * @param categoryId 카테고리 ID
     * @return 카테고리에 속한 모든 상품 목록 {@link List<Product>}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 카테고리에 속한 모든 상품 목록을 조회합니다.
     */
    List<Product> findAllByCategoryId(Long categoryId);

    /**
     * @param cursor   커서
     * @param pageable 페이지네이션 정보
     * @return 모든 상품 목록 {@link List<Product>}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 모든 상품 목록을 조회합니다(페이지네이션 적용).
     */
    List<Product> findAllActive(
            Long cursor,
            Pageable pageable,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword
    );

    /**
     * @param categoryId 카테고리 ID
     * @param cursor     커서
     * @param pageable   페이지네이션 정보
     * @return 카테고리에 속한 상품 목록 {@link List<Product>}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 카테고리에 속한 상품 목록을 조회합니다(페이지네이션 적용).
     */
    List<Product> findAllActiveByCategoryId(
            Long categoryId,
            Long cursor,
            Pageable pageable,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword
    );

    /**
     * @param pricePolicyIds 가격 정책 ID 목록
     * @return 상품 목록 {@link List<Product>}
     * @Date 2026-01-05
     * @Author 성효빈
     * @Description 가격 정책 ID 목록에 해당하는 상품 목록을 조회합니다.
     */
    List<Product> findByPricePolicyIds(List<Long> pricePolicyIds);
}
