package com.personal.marketnote.product.port.in.usecase.category;

import com.personal.marketnote.product.port.in.command.RegisterCategoryCommand;
import com.personal.marketnote.product.port.in.result.category.RegisterCategoryResult;

public interface RegisterCategoryUseCase {
    /**
     * @param command 카테고리 등록 커맨드
     * @return 카테고리 등록 결과 {@link RegisterCategoryResult}
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 카테고리를 등록합니다.
     */
    RegisterCategoryResult registerCategory(RegisterCategoryCommand command);
}
