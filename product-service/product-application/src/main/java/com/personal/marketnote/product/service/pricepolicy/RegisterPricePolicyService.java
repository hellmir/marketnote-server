package com.personal.marketnote.product.service.pricepolicy;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.exception.InvalidPricePolicyAccumulatedPointException;
import com.personal.marketnote.product.exception.InvalidPricePolicyPriceException;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.mapper.ProductCommandToStateMapper;
import com.personal.marketnote.product.port.in.command.RegisterPricePolicyCommand;
import com.personal.marketnote.product.port.in.result.pricepolicy.RegisterPricePolicyResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.RegisterPricePolicyUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import com.personal.marketnote.product.port.out.pricepolicy.SavePricePolicyPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.UpdateOptionPricePolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterPricePolicyService implements RegisterPricePolicyUseCase {
    private final GetProductUseCase getProductUseCase;
    private final FindProductPort findProductPort;
    private final SavePricePolicyPort savePricePolicyPort;
    private final UpdateOptionPricePolicyPort updateOptionPricePolicyPort;
    private final PublishProductEventPort publishProductEventPort;

    @Override
    public RegisterPricePolicyResult registerPricePolicy(
            Long userId, boolean isAdmin, RegisterPricePolicyCommand command
    ) {
        Long productId = command.productId();
        if (!isAdmin && !findProductPort.existsByIdAndSellerId(productId, userId)) {
            throw new NotProductOwnerException(FIRST_ERROR_CODE, productId);
        }

        validateDiscountPriceNotExceedPrice(command);
        validateAccumulatedPointNotExceedDiscountPrice(command);

        Product product = getProductUseCase.getProduct(productId);

        PricePolicy pricePolicy = PricePolicy.from(ProductCommandToStateMapper.mapToState(product, command));
        Long id = savePricePolicyPort.save(pricePolicy);

        List<Long> optionIds = command.optionIds();
        if (FormatValidator.hasValue(optionIds)) {
            updateOptionPricePolicyPort.assignPricePolicyToOptions(productId, id, optionIds);
        }

        // Outbox 이벤트 저장 (트랜잭션 내)
        publishProductEventPort.publishPricePolicyCreatedEvent(productId, id);

        return RegisterPricePolicyResult.of(id);
    }

    private void validateDiscountPriceNotExceedPrice(RegisterPricePolicyCommand command) {
        if (command.discountPrice().compareTo(command.price()) > 0) {
            throw new InvalidPricePolicyPriceException();
        }
    }

    private void validateAccumulatedPointNotExceedDiscountPrice(RegisterPricePolicyCommand command) {
        if (command.discountPrice().compareTo(0L) <= 0) {
            return;
        }
        if (command.accumulatedPoint().compareTo(command.discountPrice()) > 0) {
            throw new InvalidPricePolicyAccumulatedPointException();
        }
    }
}
