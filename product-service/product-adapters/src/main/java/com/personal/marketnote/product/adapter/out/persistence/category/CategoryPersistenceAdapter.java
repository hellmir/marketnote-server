package com.personal.marketnote.product.adapter.out.persistence.category;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.mapper.CategoryJpaEntityToDomainMapper;
import com.personal.marketnote.product.adapter.out.persistence.category.entity.CategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.category.repository.CategoryJpaRepository;
import com.personal.marketnote.product.domain.category.Category;
import com.personal.marketnote.product.exception.CategoryNotFoundException;
import com.personal.marketnote.product.port.out.category.DeleteCategoryPort;
import com.personal.marketnote.product.port.out.category.FindCategoryPort;
import com.personal.marketnote.product.port.out.category.SaveCategoryPort;
import com.personal.marketnote.product.port.out.category.UpdateCategoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements FindCategoryPort, SaveCategoryPort, DeleteCategoryPort, UpdateCategoryPort {
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Optional<Category> findById(Long id) {
        return CategoryJpaEntityToDomainMapper.mapToDomain(
                categoryJpaRepository.findById(id).orElse(null)
        );
    }

    @Override
    public List<Category> findActiveByParentId(Long parentCategoryId) {
        return CategoryJpaEntityToDomainMapper.mapToDomainList(
                categoryJpaRepository.findActiveByParentId(parentCategoryId)
        );
    }

    @Override
    public List<Category> findAllActiveByIds(List<Long> ids) {
        return CategoryJpaEntityToDomainMapper.mapToDomainList(
                categoryJpaRepository.findAllByIdInAndStatus(ids, EntityStatus.ACTIVE)
        );
    }

    @Override
    public Category save(Category category) {
        CategoryJpaEntity entity = CategoryJpaEntity.of(
                category.getParentCategoryId(),
                category.getName()
        );
        CategoryJpaEntity saved = categoryJpaRepository.save(entity);

        return CategoryJpaEntityToDomainMapper.mapToDomain(saved).get();
    }

    @Override
    public boolean existsById(Long id) {
        return categoryJpaRepository.existsById(id);
    }

    @Override
    public boolean existsChildren(Long id) {
        return categoryJpaRepository.existsByParentCategoryIdAndStatus(id, EntityStatus.ACTIVE);
    }

    @Override
    public void deleteById(Long id) {
        categoryJpaRepository.deleteById(id);
    }

    @Override
    public void update(Category category) {
        Long id = category.getId();
        CategoryJpaEntity entity = categoryJpaRepository.findById(id)
                .orElseThrow(
                        () -> new CategoryNotFoundException("존재하지 않는 카테고리 ID입니다. 전송된 카테고리 ID: %d", id)
                );

        entity.updateFrom(category);
    }
}
