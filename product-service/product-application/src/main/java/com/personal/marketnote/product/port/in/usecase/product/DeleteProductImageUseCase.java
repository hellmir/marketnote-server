package com.personal.marketnote.product.port.in.usecase.product;

import com.personal.marketnote.product.port.in.command.DeleteProductImageCommand;

/**
 * 상품 이미지 삭제 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-07
 * @Description 상품 이미지 삭제 기능을 제공합니다.
 */
public interface DeleteProductImageUseCase {
    /**
     * @param userId  사용자 ID
     * @param isAdmin 관리자 여부
     * @param command 상품 이미지 삭제 커맨드
     * @Date 2026-01-07
     * @Author 성효빈
     * @Description 상품 이미지를 삭제합니다.
     */
    void delete(Long userId, boolean isAdmin, DeleteProductImageCommand command);
}
