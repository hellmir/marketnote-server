package com.personal.marketnote.product.service.option;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.exception.OptionsNoValueException;
import com.personal.marketnote.product.mapper.ProductCommandToStateMapper;
import com.personal.marketnote.product.port.in.command.RegisterProductOptionsCommand;
import com.personal.marketnote.product.port.in.result.option.UpdateProductOptionsResult;
import com.personal.marketnote.product.port.in.usecase.option.RegisterProductOptionsUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.SaveProductOptionsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static com.personal.marketnote.common.domain.exception.ExceptionCode.SECOND_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterProductOptionsService implements RegisterProductOptionsUseCase {
    private final GetProductUseCase getProductUseCase;
    private final FindProductPort findProductPort;
    private final SaveProductOptionsPort saveProductOptionsPort;

    @Override
    public UpdateProductOptionsResult registerProductOptions(
            Long userId, boolean isAdmin, RegisterProductOptionsCommand command
    ) {
        Long productId = command.productId();
        if (!isAdmin && !findProductPort.existsByIdAndSellerId(productId, userId)) {
            throw new NotProductOwnerException(FIRST_ERROR_CODE, productId);
        }
        Product product = getProductUseCase.getProduct(productId);

        List<RegisterProductOptionsCommand.OptionItem> optionItems = command.options();
        if (FormatValidator.hasNoValue(optionItems)) {
            throw new OptionsNoValueException(SECOND_ERROR_CODE);
        }

        ProductOptionCategory savedCategory = saveProductOptionsPort.save(
                ProductOptionCategory.from(ProductCommandToStateMapper.mapToState(product, command))
        );

        return UpdateProductOptionsResult.from(savedCategory);
    }
}
