package com.personal.marketnote.product.adapter.out.mapper;

import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionPricePolicyJpaEntity;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.domain.pricepolicy.Rate;

import java.util.List;
import java.util.Optional;

public class PricePolicyJpaEntityToDomainMapper {
    public static Optional<PricePolicy> mapToDomain(PricePolicyJpaEntity pricePolicyJpaEntity) {
        return Optional.ofNullable(pricePolicyJpaEntity)
                .map(
                        entity -> {
                            PricePolicy pricePolicy = PricePolicy.from(
                                    PricePolicySnapshotState.builder()
                                            .id(pricePolicyJpaEntity.getId())
                                            .price(pricePolicyJpaEntity.getPrice())
                                            .discountPrice(pricePolicyJpaEntity.getDiscountPrice())
                                            .discountRate(Rate.of(pricePolicyJpaEntity.getDiscountRate()))
                                            .accumulatedPoint(pricePolicyJpaEntity.getAccumulatedPoint())
                                            .accumulationRate(Rate.of(pricePolicyJpaEntity.getAccumulationRate()))
                                            .popularity(pricePolicyJpaEntity.getPopularity())
                                            .status(pricePolicyJpaEntity.getStatus())
                                            .orderNum(pricePolicyJpaEntity.getOrderNum())
                                            .build()
                            );
                            pricePolicy.addProduct(
                                    ProductJpaEntityToDomainMapper.mapToDomainWithoutPolicyProduct(entity.getProductJpaEntity())
                                            .orElse(null)
                            );

                            return pricePolicy;
                        }
                );
    }

    public static Optional<PricePolicy> mapToDomain(PricePolicyJpaEntity pricePolicyJpaEntity, List<Long> optionIds) {
        return Optional.ofNullable(pricePolicyJpaEntity)
                .map(
                        entity -> {
                            PricePolicy pricePolicy = PricePolicy.from(
                                    PricePolicySnapshotState.builder()
                                            .id(pricePolicyJpaEntity.getId())
                                            .price(pricePolicyJpaEntity.getPrice())
                                            .discountPrice(pricePolicyJpaEntity.getDiscountPrice())
                                            .discountRate(Rate.of(pricePolicyJpaEntity.getDiscountRate()))
                                            .accumulatedPoint(pricePolicyJpaEntity.getAccumulatedPoint())
                                            .accumulationRate(Rate.of(pricePolicyJpaEntity.getAccumulationRate()))
                                            .popularity(pricePolicyJpaEntity.getPopularity())
                                            .status(pricePolicyJpaEntity.getStatus())
                                            .orderNum(pricePolicyJpaEntity.getOrderNum())
                                            .optionIds(optionIds)
                                            .build()
                            );
                            pricePolicy.addProduct(
                                    ProductJpaEntityToDomainMapper.mapToDomain(entity.getProductJpaEntity())
                                            .orElse(null)
                            );

                            return pricePolicy;
                        }
                );
    }

    public static Optional<PricePolicy> mapToDomainWithOptions(PricePolicyJpaEntity pricePolicyJpaEntity) {
        List<ProductOption> productOptions = pricePolicyJpaEntity.getProductOptionPricePolicyJpaEntities()
                .stream()
                .map(ProductOptionPricePolicyJpaEntity::getProductOptionJpaEntity)
                .toList()
                .stream()
                .map(
                        productOptionJpaEntity ->
                                ProductJpaEntityToDomainMapper.mapToDomain(productOptionJpaEntity)
                                        .orElse(null)
                )
                .toList();

        return Optional.ofNullable(pricePolicyJpaEntity)
                .map(
                        entity -> {
                            PricePolicy pricePolicy = PricePolicy.from(
                                    PricePolicySnapshotState.builder()
                                            .id(pricePolicyJpaEntity.getId())
                                            .price(pricePolicyJpaEntity.getPrice())
                                            .discountPrice(pricePolicyJpaEntity.getDiscountPrice())
                                            .discountRate(Rate.of(pricePolicyJpaEntity.getDiscountRate()))
                                            .accumulatedPoint(pricePolicyJpaEntity.getAccumulatedPoint())
                                            .accumulationRate(Rate.of(pricePolicyJpaEntity.getAccumulationRate()))
                                            .popularity(pricePolicyJpaEntity.getPopularity())
                                            .status(pricePolicyJpaEntity.getStatus())
                                            .orderNum(pricePolicyJpaEntity.getOrderNum())
                                            .productOptions(productOptions)
                                            .build()
                            );
                            pricePolicy.addProduct(
                                    ProductJpaEntityToDomainMapper.mapToDomainWithoutPolicyProduct(entity.getProductJpaEntity())
                                            .orElse(null)
                            );

                            return pricePolicy;
                        }
                );
    }
}
