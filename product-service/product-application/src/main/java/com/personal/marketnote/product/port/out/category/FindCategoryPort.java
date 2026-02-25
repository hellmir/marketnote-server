package com.personal.marketnote.product.port.out.category;

import com.personal.marketnote.product.domain.category.Category;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 조회 포트
 *
 * @Author 성효빈
 * @Date 2025-12-31
 * @Description 카테고리 조회 관련 기능을 제공합니다.
 */
public interface FindCategoryPort {
    /**
     * @param id 카테고리 ID
     * @return 카테고리 {@link Optional}&lt;{@link Category}&gt;
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 카테고리 ID로 카테고리를 조회합니다.
     */
    Optional<Category> findById(Long id);

    /**
     * @param parentCategoryId 상위 카테고리 ID
     * @return 활성 하위 카테고리 목록 {@link List}&lt;{@link Category}&gt;
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 상위 카테고리 ID로 활성 상태의 하위 카테고리 목록을 조회합니다.
     */
    List<Category> findActiveByParentId(Long parentCategoryId);

    /**
     * @param categoryIds 카테고리 ID 목록
     * @return 활성 카테고리 목록 {@link List}&lt;{@link Category}&gt;
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 카테고리 ID 목록으로 활성 상태의 카테고리 목록을 조회합니다.
     */
    List<Category> findAllActiveByIds(List<Long> categoryIds);

    /**
     * @param categoryId 카테고리 ID
     * @return 카테고리 존재 여부 {@link boolean}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 카테고리 ID로 카테고리의 존재 여부를 확인합니다.
     */
    boolean existsById(Long categoryId);

    /**
     * @param categoryId 카테고리 ID
     * @return 하위 카테고리 존재 여부 {@link boolean}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 카테고리 ID로 하위 카테고리의 존재 여부를 확인합니다.
     */
    boolean existsChildren(Long categoryId);
}
