package com.personal.marketnote.product.adapter.out.persistence.cart;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.product.adapter.out.mapper.CartJpaEntityToDomainMapper;
import com.personal.marketnote.product.adapter.out.persistence.cart.entity.CartProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.cart.repository.CartJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository.PricePolicyJpaRepository;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.exception.CartProductNotFoundException;
import com.personal.marketnote.product.port.out.cart.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class CartPersistenceAdapter implements SaveCartProductPort, FindCartProductsPort, FindCartProductPort, UpdateCartProductPort, DeleteCartProductPort {
    private final CartJpaRepository cartJpaRepository;
    private final PricePolicyJpaRepository pricePolicyJpaRepository;

    @Override
    public CartProduct save(CartProduct cartProduct) {
        PricePolicyJpaEntity pricePolicyJpaEntity
                = pricePolicyJpaRepository.getReferenceById(cartProduct.getPricePolicy().getId());
        CartProductJpaEntity savedEntity
                = cartJpaRepository.save(CartProductJpaEntity.from(cartProduct, pricePolicyJpaEntity));

        return CartJpaEntityToDomainMapper.mapToDomain(savedEntity).orElse(null);
    }

    @Override
    public List<CartProduct> findByUserId(Long userId) {
        List<CartProductJpaEntity> cartProductJpaEntities = cartJpaRepository.findByIdUserId(userId);
        return cartProductJpaEntities.stream()
                .map(CartJpaEntityToDomainMapper::mapToDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public Optional<CartProduct> findCartProductByUserIdAndPricePolicyId(Long userId, Long pricePolicyId) {
        return CartJpaEntityToDomainMapper.mapToDomain(
                cartJpaRepository.findByIdUserIdAndPricePolicyId(userId, pricePolicyId).orElse(null)
        );
    }

    @Override
    public boolean existsByUserIdAndPolicyId(Long userId, Long pricePolicyId) {
        return cartJpaRepository.existsByUserIdAndPolicyId(userId, pricePolicyId);
    }

    @Override
    public void update(CartProduct cartProduct, Long originalPricePolicyId) throws CartProductNotFoundException {
        CartProductJpaEntity entity = findCartProductEntity(
                cartProduct.getUserId(),
                originalPricePolicyId
        );

        Long newPricePolicyId = cartProduct.getPricePolicy().getId();
        boolean pricePolicyChanged = !originalPricePolicyId.equals(newPricePolicyId);

        if (pricePolicyChanged) {
            cartJpaRepository.delete(entity);
            PricePolicyJpaEntity pricePolicyJpaEntity = pricePolicyJpaRepository.getReferenceById(newPricePolicyId);
            cartJpaRepository.save(CartProductJpaEntity.from(cartProduct, pricePolicyJpaEntity));
            return;
        }

        entity.updateFrom(cartProduct);
    }

    private CartProductJpaEntity findCartProductEntity(Long userId, Long pricePolicyId)
            throws CartProductNotFoundException {
        return cartJpaRepository.findByIdUserIdAndPricePolicyId(userId, pricePolicyId)
                .orElseThrow(() -> new CartProductNotFoundException(pricePolicyId));
    }

    @Override
    public void delete(Long userId, List<Long> pricePolicyIds) {
        cartJpaRepository.deleteByUserIdAndPricePolicyIdIn(userId, pricePolicyIds);
    }

    @Override
    public void deleteAll(Long userId) {
        cartJpaRepository.deleteByUserId(userId);
    }
}
