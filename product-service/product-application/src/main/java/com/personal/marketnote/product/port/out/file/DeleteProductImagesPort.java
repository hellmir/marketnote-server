package com.personal.marketnote.product.port.out.file;

/**
 * 상품 이미지 삭제 포트
 *
 * @Author 성효빈
 * @Date 2026-01-07
 * @Description 상품 이미지 삭제 기능을 제공합니다.
 */
public interface DeleteProductImagesPort {
    /**
     * @param fileId 삭제할 파일 ID
     * @Date 2026-01-07
     * @Author 성효빈
     * @Description 파일 ID로 상품 이미지를 삭제합니다.
     */
    void delete(Long fileId);
}
