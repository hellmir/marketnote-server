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

import java.util.stream.Collectors;

public class ProductCommandToStateMapper {
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
        java.math.BigDecimal price = java.math.BigDecimal.valueOf(command.price());
        java.math.BigDecimal discountPrice = java.math.BigDecimal.valueOf(command.discountPrice());
        java.math.BigDecimal hundred = new java.math.BigDecimal("100");

        // discountRate = (price - discountPrice) / price * 100
        java.math.BigDecimal discountRate = price.subtract(discountPrice)
                .divide(price, 3, java.math.RoundingMode.HALF_UP)
                .multiply(hundred)
                .setScale(1, java.math.RoundingMode.HALF_UP);

        // accumulationRate = accumulatedPoint / discountPrice * 100
        java.math.BigDecimal accumulationRate = java.math.BigDecimal.valueOf(command.accumulatedPoint())
                .divide(discountPrice, 3, java.math.RoundingMode.HALF_UP)
                .multiply(hundred)
                .setScale(1, java.math.RoundingMode.HALF_UP);

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
}
