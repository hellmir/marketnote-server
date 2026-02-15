package com.personal.marketnote.product.service.category;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.category.Category;
import com.personal.marketnote.product.domain.category.CategoryCreateState;
import com.personal.marketnote.product.exception.CategoryNotFoundException;
import com.personal.marketnote.product.port.in.command.RegisterCategoryCommand;
import com.personal.marketnote.product.port.in.result.category.RegisterCategoryResult;
import com.personal.marketnote.product.port.in.usecase.category.RegisterCategoryUseCase;
import com.personal.marketnote.product.port.out.category.FindCategoryPort;
import com.personal.marketnote.product.port.out.category.SaveCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterCategoryService implements RegisterCategoryUseCase {
    private final SaveCategoryPort saveCategoryPort;
    private final FindCategoryPort findCategoryPort;

    @Override
    public RegisterCategoryResult registerCategory(RegisterCategoryCommand command) {
        Long parentCategoryId = command.parentCategoryId();
        String name = command.name();

        if (
                FormatValidator.hasValue(parentCategoryId)
                        && findCategoryPort.findAllActiveByIds(List.of(parentCategoryId)).isEmpty()
        ) {
            throw new CategoryNotFoundException(
                    "%s::존재하지 않는 상위 카테고리 ID입니다. 전송된 상위 카테고리 ID: %d",
                    parentCategoryId
            );
        }

        Category savedCategory = saveCategoryPort.save(
                Category.from(
                        CategoryCreateState.of(parentCategoryId, name)
                )
        );

        return RegisterCategoryResult.from(savedCategory);
    }
}
