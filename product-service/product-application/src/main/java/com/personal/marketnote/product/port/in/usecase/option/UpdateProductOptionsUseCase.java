package com.personal.marketnote.product.port.in.usecase.option;

import com.personal.marketnote.product.port.in.command.UpdateProductOptionsCommand;
import com.personal.marketnote.product.port.in.result.option.UpdateProductOptionsResult;

/**
 * 상품 옵션 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 상품 옵션 수정 기능을 제공합니다.
 */
public interface UpdateProductOptionsUseCase {
    /**
     * @param userId  사용자 ID
     * @param isAdmin 관리자 여부
     * @param command 상품 옵션 수정 커맨드
     * @return 상품 옵션 수정 결과 {@link UpdateProductOptionsResult}
     * @Date 2026-02-16
     * @Author 성효빈
     * @Description 상품 옵션을 수정합니다.
     */
    UpdateProductOptionsResult updateProductOptions(
            Long userId, boolean isAdmin, UpdateProductOptionsCommand command
    );
}
