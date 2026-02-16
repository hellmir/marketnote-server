package com.personal.marketnote.product.port.in.usecase.category;

import com.personal.marketnote.product.port.in.command.RegisterProductCategoriesCommand;
import com.personal.marketnote.product.port.in.result.category.RegisterProductCategoriesResult;

public interface RegisterProductCategoriesUseCase {
    /**
     * @param userId 사용자 ID
     * @param isAdmin 관리자 여부
     * @param command 상품 카테고리 등록 커맨드
     * @return 상품 카테고리 등록 결과 {@link RegisterProductCategoriesResult}
     * @Date 2026-02-16
     * @Author 성효빈
     * @Description 상품 카테고리를 등록합니다.
     */
    RegisterProductCategoriesResult registerProductCategories(
            Long userId, boolean isAdmin, RegisterProductCategoriesCommand command
    );
}
