package com.personal.marketnote.product.service.category;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.category.Category;
import com.personal.marketnote.product.exception.CategoryNotFoundException;
import com.personal.marketnote.product.port.in.result.category.GetCategoriesResult;
import com.personal.marketnote.product.port.in.usecase.category.GetCategoryUseCase;
import com.personal.marketnote.product.port.out.category.FindCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetCategoryService implements GetCategoryUseCase {
    private final FindCategoryPort findCategoryPort;

    @Override
    public Category getCategory(Long id) {
        return findCategoryPort.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("존재하지 않는 카테고리 ID입니다. 전송된 카테고리 ID: %d", id));
    }

    @Override
    public GetCategoriesResult getCategoriesByParentId(Long parentCategoryId) {
        List<Category> categories = findCategoryPort.findActiveByParentId(parentCategoryId);
        return GetCategoriesResult.from(categories);
    }
}
