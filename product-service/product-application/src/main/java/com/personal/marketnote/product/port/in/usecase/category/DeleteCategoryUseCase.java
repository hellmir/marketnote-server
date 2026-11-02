package com.personal.marketnote.product.port.in.usecase.category;

import com.personal.marketnote.product.port.in.command.DeleteCategoryCommand;

/**
 * 카테고리 삭제 유스케이스
 *
 * @Author 성효빈
 * @Date 2025-12-31
 * @Description 카테고리 삭제 기능을 제공합니다.
 */
public interface DeleteCategoryUseCase {
    /**
     * @param command 삭제 카테고리 커맨드
     * @Date 2025-12-31
     * @Author 성효빈
     * @Description 카테고리를 삭제합니다.
     */
    void deleteCategory(DeleteCategoryCommand command);
}
