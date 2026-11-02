package com.personal.marketnote.product.adapter.out.mapper;

import com.personal.marketnote.product.adapter.out.persistence.category.entity.CategoryJpaEntity;
import com.personal.marketnote.product.domain.category.Category;
import com.personal.marketnote.product.domain.category.CategorySnapshotState;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CategoryJpaEntityToDomainMapper {
    public static Optional<Category> mapToDomain(CategoryJpaEntity entity) {
        return Optional.ofNullable(entity)
                .map(e -> Category.from(
                        CategorySnapshotState.builder()
                                .id(e.getId())
                                .parentCategoryId(e.getParentCategoryId())
                                .name(e.getName())
                                .status(e.getStatus())
                                .build()
                ));
    }

    public static List<Category> mapToDomainList(List<CategoryJpaEntity> entities) {
        return entities.stream()
                .map(CategoryJpaEntityToDomainMapper::mapToDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}


