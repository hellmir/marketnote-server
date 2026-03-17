package com.personal.marketnote.product.mapper;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.option.ProductOptionCategoryCreateState;
import com.personal.marketnote.product.domain.option.ProductOptionCreateState;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicyCreateState;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductCreateState;
import com.personal.marketnote.product.domain.product.ProductTagCreateState;
import com.personal.marketnote.product.port.in.command.RegisterPricePolicyCommand;
import com.personal.marketnote.product.port.in.command.RegisterProductCommand;
import com.personal.marketnote.product.port.in.command.RegisterProductOptionsCommand;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

public class ProductCommandToStateMapper {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public static ProductCreateState mapToState(RegisterProductCommand command) {
        return ProductCreateState.builder()
                .sellerId(command.sellerId())
                .name(command.name())
                .brandName(command.brandName())
                .detail(command.detail())
                .findAllOptionsYn(Boolean.TRUE.equals(command.isFindAllOptions()))
                .tags(
                        command.tags()
                                .stream()
                                .map(tag -> ProductTagCreateState.builder().name(tag).build())
                                .toList()
                )
                .build();
    }

    public static ProductOptionCategoryCreateState mapToState(
            Product product, RegisterProductOptionsCommand registerProductOptionsCommand
    ) {
        return
                ProductOptionCategoryCreateState.builder()
                        .product(product)
                        .name(registerProductOptionsCommand.categoryName())
                        .optionStates(
                                registerProductOptionsCommand.options()
                                        .stream()
                                        .map(ProductCommandToStateMapper::mapToState)
                                        .collect(Collectors.toList())
                        )
                        .build();
    }

    public static ProductOptionCreateState mapToState(RegisterProductOptionsCommand.OptionItem optionItem) {
        return ProductOptionCreateState.builder()
                .content(optionItem.content())
                .build();
    }

    public static PricePolicyCreateState mapToState(
            Product product, RegisterPricePolicyCommand command
    ) {
        BigDecimal price = BigDecimal.valueOf(command.price());
        BigDecimal discountPrice = BigDecimal.valueOf(command.discountPrice());

        BigDecimal discountRate = calculateDiscountRate(price, discountPrice);
        BigDecimal accumulationRate = calculateAccumulationRate(
                BigDecimal.valueOf(command.accumulatedPoint()), discountPrice
        );

        return
                PricePolicyCreateState.builder()
                        .product(product)
                        .price(command.price())
                        .discountPrice(command.discountPrice())
                        .discountRate(discountRate)
                        .accumulatedPoint(command.accumulatedPoint())
                        .accumulationRate(accumulationRate)
                        .status(EntityStatus.ACTIVE)
                        .optionIds(command.optionIds())
                        .build();
    }

    private static BigDecimal calculateDiscountRate(BigDecimal price, BigDecimal discountPrice) {
        if (price.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return price.subtract(discountPrice)
                .divide(price, 3, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(1, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateAccumulationRate(BigDecimal accumulatedPoint, BigDecimal discountPrice) {
        if (discountPrice.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return accumulatedPoint
                .divide(discountPrice, 3, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(1, RoundingMode.HALF_UP);
    }
}
