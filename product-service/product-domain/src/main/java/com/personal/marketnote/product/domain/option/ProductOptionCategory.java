package com.personal.marketnote.product.domain.option;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.product.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductOptionCategory {
    private Long id;
    private Product product;
    private String name;
    private List<ProductOption> options;
    private Long orderNum;
    private EntityStatus status;

    public static ProductOptionCategory from(ProductOptionCategoryCreateState state) {
        List<ProductOption> optionList = FormatValidator.hasValue(state.getOptionStates())
                ? state.getOptionStates()
                .stream()
                .map(ProductOption::from)
                .toList()
                : List.of();

        return ProductOptionCategory.builder()
                .product(state.getProduct())
                .name(state.getName())
                .options(optionList)
                .status(EntityStatus.ACTIVE)
                .build();
    }

    public static ProductOptionCategory from(ProductOptionCategorySnapshotState state) {
        return ProductOptionCategory.builder()
                .id(state.getId())
                .product(state.getProduct())
                .name(state.getName())
                .options(state.getOptions())
                .orderNum(state.getOrderNum())
                .status(state.getStatus())
                .build();
    }
}
