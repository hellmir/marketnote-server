package com.personal.marketnote.product.port.in.usecase.product;

import com.personal.marketnote.product.port.in.command.UpdateProductCommand;
import com.personal.marketnote.product.port.in.result.product.UpdateProductResult;

/**
 * 상품 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 상품 수정 기능을 제공합니다.
 */
public interface UpdateProductUseCase {
    /**
     * @param userId  사용자 ID
     * @param isAdmin 관리자 여부
     * @param command 상품 정보 수정 커맨드
     * @return 상품 수정 결과 {@link UpdateProductResult}
     * @Date 2026-02-05
     * @Author 성효빈
     * @Description 상품 정보를 수정합니다.
     */
    UpdateProductResult update(Long userId, boolean isAdmin, UpdateProductCommand command);
}
