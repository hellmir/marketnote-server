package com.personal.marketnote.product.adapter.out.mapper;

import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductTagJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionCategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionJpaEntity;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.domain.option.ProductOptionCategorySnapshotState;
import com.personal.marketnote.product.domain.option.ProductOptionSnapshotState;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.domain.product.ProductTagSnapshotState;

import java.util.List;
import java.util.Optional;

public class ProductJpaEntityToDomainMapper {
    public static Optional<Product> mapToDomain(ProductJpaEntity productJpaEntity) {
        return Optional.ofNullable(productJpaEntity)
                .map(
                        entity -> {
                            List<ProductTag> tags = entity.getProductTagJpaEntities().stream()
                                    .map(ProductJpaEntityToDomainMapper::mapToDomain)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .toList();

                            return Product.from(
                                    ProductSnapshotState.builder()
                                            .id(entity.getId())
                                            .sellerId(entity.getSellerId())
                                            .name(entity.getName())
                                            .brandName(entity.getBrandName())
                                            .detail(entity.getDetail())
                                            .defaultPricePolicy(
                                                    PricePolicyJpaEntityToDomainMapper.mapToDomain(entity.getDefaultPricePolicy())
                                                            .orElse(null)
                                            )
                                            .sales(entity.getSales())
                                            .viewCount(entity.getViewCount())
                                            .popularity(entity.getPopularity())
                                            .findAllOptionsYn(entity.isFindAllOptionsYn())
                                            .productTags(tags)
                                            .orderNum(entity.getOrderNum())
                                            .status(entity.getStatus())
                                            .build()
                            );
                        }
                );
    }

    public static Optional<Product> mapToDomainWithoutPolicyProduct(ProductJpaEntity productJpaEntity) {
        return Optional.ofNullable(productJpaEntity)
                .map(
                        entity -> {
                            List<ProductTag> tags = entity.getProductTagJpaEntities().stream()
                                    .map(ProductJpaEntityToDomainMapper::mapToDomain)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .toList();

                            return Product.from(
                                    ProductSnapshotState.builder()
                                            .id(entity.getId())
                                            .sellerId(entity.getSellerId())
                                            .name(entity.getName())
                                            .brandName(entity.getBrandName())
                                            .detail(entity.getDetail())
                                            .sales(entity.getSales())
                                            .viewCount(entity.getViewCount())
                                            .popularity(entity.getPopularity())
                                            .findAllOptionsYn(entity.isFindAllOptionsYn())
                                            .productTags(tags)
                                            .orderNum(entity.getOrderNum())
                                            .status(entity.getStatus())
                                            .build()
                            );
                        }
                );
    }

    private static Optional<ProductOption> mapToDomain(
            ProductOptionJpaEntity productOptionJpaEntity,
            ProductOptionCategory productOptionCategory
    ) {
        return Optional.ofNullable(productOptionJpaEntity)
                .map(
                        entity -> ProductOption.from(
                                ProductOptionSnapshotState.builder()
                                        .id(entity.getId())
                                        .category(productOptionCategory)
                                        .content(entity.getContent())
                                        .status(entity.getStatus())
                                        .build()
                        )
                );
    }

    private static Optional<ProductTag> mapToDomain(ProductTagJpaEntity productTagJpaEntity) {
        return Optional.ofNullable(productTagJpaEntity)
                .map(entity -> ProductTag.from(
                        ProductTagSnapshotState.builder()
                                .id(entity.getId())
                                .productId(entity.getProductJpaEntity().getId())
                                .name(entity.getName())
                                .orderNum(entity.getOrderNum())
                                .status(entity.getStatus())
                                .build()
                ));
    }

    public static Optional<ProductOption> mapToDomain(ProductOptionJpaEntity productOptionJpaEntity) {
        return Optional.ofNullable(productOptionJpaEntity)
                .map(
                        entity -> ProductOption.from(
                                ProductOptionSnapshotState.builder()
                                        .id(entity.getId())
                                        .category(
                                                mapToDomain(productOptionJpaEntity.getProductOptionCategoryJpaEntity())
                                                        .orElse(null)
                                        )
                                        .content(entity.getContent())
                                        .status(entity.getStatus())
                                        .build()
                        )
                );
    }

    public static Optional<ProductOptionCategory> mapToDomain(
            ProductOptionCategoryJpaEntity productOptionCategoryJpaEntity
    ) {
        return Optional.ofNullable(productOptionCategoryJpaEntity)
                .map(entity -> {
                    Product product = ProductJpaEntityToDomainMapper
                            .mapToDomain(entity.getProductJpaEntity()).orElse(null);
                    ProductOptionCategory productOptionCategory = ProductOptionCategory.from(
                            ProductOptionCategorySnapshotState.builder()
                                    .id(entity.getId())
                                    .product(product)
                                    .name(entity.getName())
                                    .options(null)
                                    .orderNum(entity.getOrderNum())
                                    .status(entity.getStatus())
                                    .build()
                    );

                    List<ProductOption> options = entity.getProductOptionJpaEntities()
                            .stream()
                            .map(optEntity -> mapToDomain(optEntity, productOptionCategory))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .toList();

                    return ProductOptionCategory.from(
                            ProductOptionCategorySnapshotState.builder()
                                    .id(entity.getId())
                                    .product(product)
                                    .name(entity.getName())
                                    .options(options)
                                    .orderNum(entity.getOrderNum())
                                    .status(entity.getStatus())
                                    .build()
                    );
                });
    }

    public static Optional<ProductOptionCategory> mapToDomainWithoutProductTags(
            ProductOptionCategoryJpaEntity productOptionCategoryJpaEntity
    ) {
        return Optional.ofNullable(productOptionCategoryJpaEntity)
                .map(entity -> {
                    Product product = mapToDomainWithoutProductTags(entity.getProductJpaEntity());
                    ProductOptionCategory productOptionCategory = ProductOptionCategory.from(
                            ProductOptionCategorySnapshotState.builder()
                                    .id(entity.getId())
                                    .product(product)
                                    .name(entity.getName())
                                    .options(null)
                                    .orderNum(entity.getOrderNum())
                                    .status(entity.getStatus())
                                    .build()
                    );

                    List<ProductOption> options = entity.getProductOptionJpaEntities()
                            .stream()
                            .map(optEntity -> mapToDomain(optEntity, productOptionCategory))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .toList();

                    return ProductOptionCategory.from(
                            ProductOptionCategorySnapshotState.builder()
                                    .id(entity.getId())
                                    .product(product)
                                    .name(entity.getName())
                                    .options(options)
                                    .orderNum(entity.getOrderNum())
                                    .status(entity.getStatus())
                                    .build()
                    );
                });
    }

    private static Product mapToDomainWithoutProductTags(ProductJpaEntity productJpaEntity) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(productJpaEntity.getId())
                        .sellerId(productJpaEntity.getSellerId())
                        .name(productJpaEntity.getName())
                        .brandName(productJpaEntity.getBrandName())
                        .detail(productJpaEntity.getDetail())
                        .sales(productJpaEntity.getSales())
                        .viewCount(productJpaEntity.getViewCount())
                        .popularity(productJpaEntity.getPopularity())
                        .findAllOptionsYn(productJpaEntity.isFindAllOptionsYn())
                        .productTags(List.of())
                        .orderNum(productJpaEntity.getOrderNum())
                        .status(productJpaEntity.getStatus())
                        .build()
        );
    }
}
