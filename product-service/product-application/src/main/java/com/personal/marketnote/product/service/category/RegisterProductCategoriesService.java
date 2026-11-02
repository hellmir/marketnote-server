package com.personal.marketnote.product.service.category;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.category.Category;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.exception.InvalidProductCategoryException;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.in.command.RegisterProductCategoriesCommand;
import com.personal.marketnote.product.port.in.result.category.RegisterProductCategoriesResult;
import com.personal.marketnote.product.port.in.usecase.category.RegisterProductCategoriesUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.category.FindCategoryPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productcategory.ReplaceProductCategoriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterProductCategoriesService implements RegisterProductCategoriesUseCase {
    private final GetProductUseCase getProductUseCase;
    private final FindProductPort findProductPort;
    private final FindCategoryPort findCategoryPort;
    private final ReplaceProductCategoriesPort replaceProductCategoriesPort;

    @Override
    public RegisterProductCategoriesResult registerProductCategories(
            Long userId, boolean isAdmin, RegisterProductCategoriesCommand command
    ) {
        Long productId = command.productId();
        Product product = getProductUseCase.getProduct(productId);

        // 관리자 또는 상품의 소유자가 아닌 경우
        if (!isAdmin && !findProductPort.existsByIdAndSellerId(productId, userId)) {
            throw new NotProductOwnerException(FIRST_ERROR_CODE, productId);
        }

        List<Long> categoryIds = command.categoryIds();
        List<Category> foundCategories = findCategoryPort.findAllActiveByIds(categoryIds);
        Set<Long> foundIds = foundCategories.stream().map(Category::getId).collect(Collectors.toSet());

        if (!foundIds.containsAll(categoryIds)) {
            throw new InvalidProductCategoryException();
        }

        replaceProductCategoriesPort.replaceProductCategories(product.getId(), categoryIds);

        return RegisterProductCategoriesResult.of(product.getId(), categoryIds);
    }
}
