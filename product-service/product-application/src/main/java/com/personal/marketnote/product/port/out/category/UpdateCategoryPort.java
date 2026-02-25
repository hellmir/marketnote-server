package com.personal.marketnote.product.port.out.category;

import com.personal.marketnote.product.domain.category.Category;

/**
 * 카테고리 수정 포트
 *
 * @Author 성효빈
 * @Date 2025-12-31
 * @Description 카테고리 수정 기능을 제공합니다.
 */
public interface UpdateCategoryPort {
    /**
     * @param category 수정할 카테고리
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 카테고리를 수정합니다.
     */
    void update(Category category);
}
