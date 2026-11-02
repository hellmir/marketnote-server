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
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static com.personal.marketnote.common.domain.exception.ExceptionCode.SECOND_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

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
            throw new CategoryNotFoundException("존재하지 않는 카테고리 ID입니다. 전송된 카테고리 ID: %d", categoryId);
        }

        if (findCategoryPort.existsChildren(categoryId)) {
            throw new CategoryHasChildrenException(FIRST_ERROR_CODE, categoryId);
        }

        if (findProductCategoryPort.existsByCategoryId(categoryId)) {
            throw new CategoryHasProductsException(SECOND_ERROR_CODE, categoryId);
        }

        Category category = getCategoryUseCase.getCategory(categoryId);
        category.delete();

        updateCategoryPort.update(category);
    }
}
