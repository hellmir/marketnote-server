package com.personal.marketnote.product.service.category;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.category.Category;
import com.personal.marketnote.product.exception.CategoryHasChildrenException;
import com.personal.marketnote.product.exception.CategoryHasProductsException;
import com.personal.marketnote.product.exception.CategoryNotFoundException;
import com.personal.marketnote.product.port.in.command.DeleteCategoryCommand;
import com.personal.marketnote.product.port.in.usecase.category.DeleteCategoryUseCase;
import com.personal.marketnote.product.port.in.usecase.category.GetCategoryUseCase;
import com.personal.marketnote.product.port.out.category.FindCategoryPort;
import com.personal.marketnote.product.port.out.category.UpdateCategoryPort;
import com.personal.marketnote.product.port.out.productcategory.FindProductCategoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class DeleteCategoryService implements DeleteCategoryUseCase {
    private final GetCategoryUseCase getCategoryUseCase;
    private final FindCategoryPort findCategoryPort;
    private final UpdateCategoryPort updateCategoryPort;
    private final FindProductCategoryPort findProductCategoryPort;

    @Override
    public void deleteCategory(DeleteCategoryCommand command) {
        Long categoryId = command.categoryId();

        if (!findCategoryPort.existsById(categoryId)) {
            log.warn("존재하지 않는 카테고리입니다. categoryId={}", categoryId);
            throw new CategoryNotFoundException();
        }

        if (findCategoryPort.existsChildren(categoryId)) {
            log.warn("하위 카테고리가 존재합니다. categoryId={}", categoryId);
            throw new CategoryHasChildrenException();
        }

        if (findProductCategoryPort.existsByCategoryId(categoryId)) {
            log.warn("해당 카테고리에 등록된 상품이 존재합니다. categoryId={}", categoryId);
            throw new CategoryHasProductsException();
        }

        Category category = getCategoryUseCase.getCategory(categoryId);
        category.delete();

        updateCategoryPort.update(category);
    }
}
