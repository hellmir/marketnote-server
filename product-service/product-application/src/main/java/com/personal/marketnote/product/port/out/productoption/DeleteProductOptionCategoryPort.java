package com.personal.marketnote.product.port.out.productoption;

/**
 * 상품 옵션 카테고리 삭제 포트
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 상품 옵션 카테고리 삭제 기능을 제공합니다.
 */
public interface DeleteProductOptionCategoryPort {
    /**
     * @param id 상품 옵션 카테고리 ID
     * @return void
     * @Date 2026-01-01
     * @Author 성효빈
     * @Description 상품 옵션 카테고리 ID로 옵션 카테고리를 삭제합니다.
     */
    void deleteById(Long id);
}
