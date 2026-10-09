package com.personal.marketnote.product.adapter.out.persistence.cart.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import com.personal.marketnote.product.adapter.out.persistence.cart.entity.CartProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository.PricePolicyJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.domain.cart.CartProductCreateState;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicyCreateState;
import com.personal.marketnote.product.domain.pricepolicy.Rate;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
class CartJpaRepositoryTest {

    @Autowired
    private CartJpaRepository cartJpaRepository;

    @Autowired
    private PricePolicyJpaRepository pricePolicyJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Nested
    @DisplayName("findByIdUserId")
    class FindByIdUserId {

        @Test
        @DisplayName("유저의 모든 장바구니 상품을 조회한다")
        void returnsAllCartItemsOfUser() {
            ProductJpaEntity product1 = saveProduct("상품1", "브랜드1");
            ProductJpaEntity product2 = saveProduct("상품2", "브랜드2");
            PricePolicyJpaEntity policy1 = savePricePolicy(product1);
            PricePolicyJpaEntity policy2 = savePricePolicy(product2);
            saveCartProduct(100L, policy1, (short) 2);
            saveCartProduct(100L, policy2, (short) 3);
            entityManager.flush();
            entityManager.clear();

            List<CartProductJpaEntity> results = cartJpaRepository.findByIdUserId(100L);

            assertThat(results).hasSize(2);
            assertThat(results).extracting(c -> c.getId().getPricePolicyId())
                    .containsExactlyInAnyOrder(policy1.getId(), policy2.getId());
        }

        @Test
        @DisplayName("다른 유저의 장바구니는 포함하지 않는다")
        void excludesOtherUsersCartItems() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            PricePolicyJpaEntity policy = savePricePolicy(product);
            saveCartProduct(100L, policy, (short) 1);
            saveCartProduct(200L, policy, (short) 5);
            entityManager.flush();
            entityManager.clear();

            List<CartProductJpaEntity> results = cartJpaRepository.findByIdUserId(100L);

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getId().getUserId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("장바구니가 비어있으면 빈 목록을 반환한다")
        void returnsEmptyListWhenNoCartItems() {
            List<CartProductJpaEntity> results = cartJpaRepository.findByIdUserId(999L);

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdUserIdAndPricePolicyId")
    class FindByIdUserIdAndPricePolicyId {

        @Test
        @DisplayName("유저 ID와 가격 정책 ID로 특정 장바구니 항목을 조회한다")
        void returnsSpecificCartItem() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            PricePolicyJpaEntity policy = savePricePolicy(product);
            saveCartProduct(100L, policy, (short) 4);
            entityManager.flush();
            entityManager.clear();

            Optional<CartProductJpaEntity> result = cartJpaRepository
                    .findByIdUserIdAndPricePolicyId(100L, policy.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getQuantity()).isEqualTo((short) 4);
        }

        @Test
        @DisplayName("매칭되는 항목이 없으면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotFound() {
            Optional<CartProductJpaEntity> result = cartJpaRepository
                    .findByIdUserIdAndPricePolicyId(100L, 999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByUserIdAndPricePolicyIdIn")
    class DeleteByUserIdAndPricePolicyIdIn {

        @Test
        @DisplayName("지정한 가격 정책 ID에 해당하는 유저의 장바구니 항목을 삭제한다")
        void deletesSpecifiedCartItems() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            PricePolicyJpaEntity policy1 = savePricePolicy(product);
            PricePolicyJpaEntity policy2 = savePricePolicy(product);
            PricePolicyJpaEntity policy3 = savePricePolicy(product);
            saveCartProduct(100L, policy1, (short) 1);
            saveCartProduct(100L, policy2, (short) 2);
            saveCartProduct(100L, policy3, (short) 3);
            entityManager.flush();

            cartJpaRepository.deleteByUserIdAndPricePolicyIdIn(100L, List.of(policy1.getId(), policy2.getId()));
            entityManager.flush();
            entityManager.clear();

            List<CartProductJpaEntity> remaining = cartJpaRepository.findByIdUserId(100L);
            assertThat(remaining).hasSize(1);
            assertThat(remaining.getFirst().getId().getPricePolicyId()).isEqualTo(policy3.getId());
        }

        @Test
        @DisplayName("다른 유저의 장바구니는 영향을 받지 않는다")
        void doesNotAffectOtherUsersCart() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            PricePolicyJpaEntity policy = savePricePolicy(product);
            saveCartProduct(100L, policy, (short) 1);
            saveCartProduct(200L, policy, (short) 5);
            entityManager.flush();

            cartJpaRepository.deleteByUserIdAndPricePolicyIdIn(100L, List.of(policy.getId()));
            entityManager.flush();
            entityManager.clear();

            assertThat(cartJpaRepository.findByIdUserId(100L)).isEmpty();
            assertThat(cartJpaRepository.findByIdUserId(200L)).hasSize(1);
        }
    }

    @Nested
    @DisplayName("deleteByUserId")
    class DeleteByUserId {

        @Test
        @DisplayName("유저의 모든 장바구니 항목을 삭제한다")
        void deletesAllCartItemsForUser() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            PricePolicyJpaEntity policy1 = savePricePolicy(product);
            PricePolicyJpaEntity policy2 = savePricePolicy(product);
            saveCartProduct(100L, policy1, (short) 1);
            saveCartProduct(100L, policy2, (short) 2);
            entityManager.flush();

            cartJpaRepository.deleteByUserId(100L);
            entityManager.flush();
            entityManager.clear();

            assertThat(cartJpaRepository.findByIdUserId(100L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUserIdAndPolicyId")
    class ExistsByUserIdAndPolicyId {

        @Test
        @DisplayName("유저 ID와 가격 정책 ID에 해당하는 장바구니 항목이 존재하면 true 를 반환한다")
        void returnsTrueWhenCartItemExists() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            PricePolicyJpaEntity policy = savePricePolicy(product);
            saveCartProduct(100L, policy, (short) 1);
            entityManager.flush();
            entityManager.clear();

            boolean result = cartJpaRepository.existsByUserIdAndPolicyId(100L, policy.getId());

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("매칭되는 장바구니 항목이 없으면 false 를 반환한다")
        void returnsFalseWhenCartItemNotExists() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            PricePolicyJpaEntity policy = savePricePolicy(product);
            saveCartProduct(100L, policy, (short) 1);
            entityManager.flush();
            entityManager.clear();

            boolean result = cartJpaRepository.existsByUserIdAndPolicyId(100L, 999L);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("다른 유저의 장바구니 항목은 false 를 반환한다")
        void returnsFalseForOtherUser() {
            ProductJpaEntity product = saveProduct("상품", "브랜드");
            PricePolicyJpaEntity policy = savePricePolicy(product);
            saveCartProduct(100L, policy, (short) 1);
            entityManager.flush();
            entityManager.clear();

            boolean result = cartJpaRepository.existsByUserIdAndPolicyId(200L, policy.getId());

            assertThat(result).isFalse();
        }
    }

    private ProductJpaEntity saveProduct(String name, String brandName) {
        Product product = Product.from(
                ProductSnapshotState.builder()
                        .productKey(RandomCodeGenerator.generateProductKey())
                        .sellerId(1L)
                        .name(name)
                        .brandName(brandName)
                        .detail("설명")
                        .findAllOptionsYn(false)
                        .productTags(List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
        ProductJpaEntity entity = ProductJpaEntity.from(product);
        ProductJpaEntity saved = productJpaRepository.save(entity);
        saved.setIdToOrderNum();
        return saved;
    }

    private PricePolicyJpaEntity savePricePolicy(ProductJpaEntity productEntity) {
        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicyCreateState.builder()
                        .price(10000L)
                        .discountPrice(9000L)
                        .discountRate(Rate.of(BigDecimal.valueOf(10.0)))
                        .accumulatedPoint(100L)
                        .accumulationRate(Rate.of(BigDecimal.valueOf(1.0)))
                        .build()
        );
        PricePolicyJpaEntity entity = PricePolicyJpaEntity.from(productEntity, pricePolicy);
        PricePolicyJpaEntity saved = pricePolicyJpaRepository.save(entity);
        saved.setIdToOrderNum();
        return saved;
    }

    private void saveCartProduct(Long userId, PricePolicyJpaEntity policyEntity, short quantity) {
        CartProduct cartProduct = CartProduct.from(
                CartProductCreateState.builder()
                        .userId(userId)
                        .sharerKey(UUID.randomUUID())
                        .pricePolicy(null)
                        .imageUrl("http://example.com/image.jpg")
                        .quantity(quantity)
                        .build()
        );
        CartProductJpaEntity entity = CartProductJpaEntity.from(cartProduct, policyEntity);
        cartJpaRepository.save(entity);
    }
}
