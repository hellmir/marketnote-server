package com.personal.marketnote.product.port.out.category;

import com.personal.marketnote.product.domain.category.Category;

/**
 * 카테고리 저장 포트
 *
 * @Author 성효빈
 * @Date 2025-12-31
 * @Description 카테고리 저장 기능을 제공합니다.
 */
public interface SaveCategoryPort {
    /**
     * @param category 저장할 카테고리
     * @return 저장된 카테고리 {@link Category}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 카테고리를 저장합니다.
     */
    Category save(Category category);
}
