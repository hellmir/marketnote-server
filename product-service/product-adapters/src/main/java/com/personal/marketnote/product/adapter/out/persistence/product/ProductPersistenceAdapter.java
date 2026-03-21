package com.personal.marketnote.product.adapter.out.persistence.product;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.mapper.ProductJpaEntityToDomainMapper;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductTagJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.exception.ProductNotFoundException;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.product.SaveProductPort;
import com.personal.marketnote.product.port.out.product.UpdateProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;

@PersistenceAdapter
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements SaveProductPort, FindProductPort, UpdateProductPort {
    private final ProductJpaRepository productJpaRepository;

    @Override
    @CachePut(value = "product:detail", key = "#result.id", unless = "#result == null")
    public Product save(Product product) {
        ProductJpaEntity savedEntity = productJpaRepository.save(ProductJpaEntity.from(product));
        savedEntity.setIdToOrderNum();
        savedEntity.getProductTagJpaEntities().forEach(ProductTagJpaEntity::setIdToOrderNum);

        return ProductJpaEntityToDomainMapper.mapToDomain(savedEntity).get();
    }

    @Override
    public boolean existsByIdAndSellerId(Long productId, Long sellerId) {
        return productJpaRepository.existsByIdAndSellerId(productId, sellerId);
    }

    @Override
    @Cacheable(
            value = "product:detail",
            key = "#id",
            unless = "#result == null || T(java.util.Optional).empty().equals(#result)"
    )
    public Optional<Product> findById(Long id) {
        return ProductJpaEntityToDomainMapper.mapToDomain(
                productJpaRepository.findById(id).orElse(null));
    }

    @Override
    public Optional<Product> findActiveById(Long id) {
        return findById(id).filter(Product::isActive);
    }

    @Override
    public List<Product> findAll() {
        return loadProductsWithAssociations(productJpaRepository.findAll()).stream()
                .map(entity -> ProductJpaEntityToDomainMapper.mapToDomain(entity).get())
                .toList();
    }

    @Override
    public List<Product> findAllByCategoryId(Long categoryId) {

        return loadProductsWithAssociations(productJpaRepository.findAllByCategoryIdOrderByOrderNumAsc(categoryId)).stream()
                .map(entity -> ProductJpaEntityToDomainMapper.mapToDomain(entity).get())
                .toList();
    }

    @Override
    public List<Product> findByPricePolicyIds(List<Long> pricePolicyIds) {
        return loadProductsWithAssociations(productJpaRepository.findByPricePolicyIds(pricePolicyIds)).stream()
                .map(ProductJpaEntityToDomainMapper::mapToDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    @CacheEvict(value = "product:detail", key = "#product.id")
    public void update(Product product) throws ProductNotFoundException {
        ProductJpaEntity entity = findEntityById(product.getId());
        entity.updateFrom(product);
        productJpaRepository.flush();
        entity.getProductTagJpaEntities().forEach(ProductTagJpaEntity::setIdToOrderNum);
    }

    private ProductJpaEntity findEntityById(Long id) throws ProductNotFoundException {
        return productJpaRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private List<ProductJpaEntity> loadProductsWithAssociations(List<ProductJpaEntity> baseEntities) {
        if (FormatValidator.hasNoValue(baseEntities)) {
            return List.of();
        }

        List<Long> ids = baseEntities.stream()
                .map(ProductJpaEntity::getId)
                .filter(Objects::nonNull)
                .toList();

        if (FormatValidator.hasNoValue(ids)) {
            return List.of();
        }

        List<ProductJpaEntity> hydrated = productJpaRepository.findAllWithTagsAndPoliciesByIdIn(ids);
        if (FormatValidator.hasNoValue(hydrated)) {
            return List.of();
        }

        Map<Long, ProductJpaEntity> hydratedById = new LinkedHashMap<>();
        for (ProductJpaEntity entity : hydrated) {
            hydratedById.putIfAbsent(entity.getId(), entity);
        }

        return ids.stream()
                .map(hydratedById::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
