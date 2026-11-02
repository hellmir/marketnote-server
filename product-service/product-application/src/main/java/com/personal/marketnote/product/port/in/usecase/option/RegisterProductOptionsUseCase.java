package com.personal.marketnote.product.port.in.usecase.option;

import com.personal.marketnote.product.port.in.command.RegisterProductOptionsCommand;
import com.personal.marketnote.product.port.in.result.option.UpdateProductOptionsResult;

/**
 * 상품 옵션 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-01
 * @Description 상품 옵션 등록 기능을 제공합니다.
 */
public interface RegisterProductOptionsUseCase {
    /**
     * @param userId  사용자 ID
     * @param isAdmin 관리자 여부
     * @param command 상품 옵션 등록 커맨드
     * @return 상품 옵션 등록 결과 {@link UpdateProductOptionsResult}
     * @Date 2026-01-01
     * @Author 성효빈
     * @Description 상품 옵션을 등록합니다.
     */
    UpdateProductOptionsResult registerProductOptions(
            Long userId, boolean isAdmin, RegisterProductOptionsCommand command
    );
}
