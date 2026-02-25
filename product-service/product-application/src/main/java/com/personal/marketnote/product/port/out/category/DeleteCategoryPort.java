package com.personal.marketnote.product.port.out.category;

/**
 * 카테고리 삭제 포트
 *
 * @Author 성효빈
 * @Date 2025-12-31
 * @Description 카테고리 삭제 기능을 제공합니다.
 */
public interface DeleteCategoryPort {
    /**
     * @param categoryId 삭제할 카테고리 ID
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 카테고리 ID로 카테고리를 삭제합니다.
     */
    void deleteById(Long categoryId);
}
