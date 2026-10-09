package com.personal.marketnote.product.adapter.out.persistence.productoption.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionCategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productoption.entity.ProductOptionJpaEntity;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.domain.option.ProductOptionCategoryCreateState;
import com.personal.marketnote.product.domain.option.ProductOptionCreateState;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
class ProductOptionJpaRepositoryTest {

    @Autowired
    private ProductOptionJpaRepository productOptionJpaRepository;

    @Autowired
    private ProductOptionCategoryJpaRepository productOptionCategoryJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Nested
    @DisplayName("findIdsByCategoryId")
    class FindIdsByCategoryId {

        @Test
        @DisplayName("카테고리에 속한 모든 옵션 ID를 반환한다")
        void returnsAllOptionIdsOfCategory() {
            ProductJpaEntity product = saveProduct("상품");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            ProductOptionJpaEntity red = saveOption(category, "빨강");
            ProductOptionJpaEntity blue = saveOption(category, "파랑");
            entityManager.flush();
            entityManager.clear();

            List<Long> results = productOptionJpaRepository.findIdsByCategoryId(category.getId());

            assertThat(results).containsExactlyInAnyOrder(red.getId(), blue.getId());
        }

        @Test
        @DisplayName("다른 카테고리의 옵션은 포함하지 않는다")
        void excludesOtherCategoryOptions() {
            ProductJpaEntity product = saveProduct("상품");
            ProductOptionCategoryJpaEntity categoryA = saveOptionCategory(product, "색상");
            ProductOptionCategoryJpaEntity categoryB = saveOptionCategory(product, "사이즈");
            ProductOptionJpaEntity red = saveOption(categoryA, "빨강");
            saveOption(categoryB, "L");
            entityManager.flush();
            entityManager.clear();

            List<Long> results = productOptionJpaRepository.findIdsByCategoryId(categoryA.getId());

            assertThat(results).containsExactly(red.getId());
        }

        @Test
        @DisplayName("옵션이 없는 카테고리는 빈 목록을 반환한다")
        void returnsEmptyListWhenNoOptions() {
            ProductJpaEntity product = saveProduct("상품");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            entityManager.flush();
            entityManager.clear();

            List<Long> results = productOptionJpaRepository.findIdsByCategoryId(category.getId());

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteAllByCategoryId")
    class DeleteAllByCategoryId {

        @Test
        @DisplayName("지정한 카테고리의 모든 옵션을 삭제한다")
        void deletesAllOptionsOfCategory() {
            ProductJpaEntity product = saveProduct("상품");
            ProductOptionCategoryJpaEntity category = saveOptionCategory(product, "색상");
            saveOption(category, "빨강");
            saveOption(category, "파랑");
            entityManager.flush();

            productOptionJpaRepository.deleteAllByCategoryId(category.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(productOptionJpaRepository.findIdsByCategoryId(category.getId())).isEmpty();
        }

        @Test
        @DisplayName("다른 카테고리의 옵션은 영향을 받지 않는다")
        void doesNotAffectOtherCategories() {
            ProductJpaEntity product = saveProduct("상품");
            ProductOptionCategoryJpaEntity categoryA = saveOptionCategory(product, "색상");
            ProductOptionCategoryJpaEntity categoryB = saveOptionCategory(product, "사이즈");
            saveOption(categoryA, "빨강");
            ProductOptionJpaEntity sizeL = saveOption(categoryB, "L");
            entityManager.flush();

            productOptionJpaRepository.deleteAllByCategoryId(categoryA.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(productOptionJpaRepository.findIdsByCategoryId(categoryA.getId())).isEmpty();
            assertThat(productOptionJpaRepository.findIdsByCategoryId(categoryB.getId()))
                    .containsExactly(sizeL.getId());
        }
    }

    private ProductJpaEntity saveProduct(String name) {
        Product product = Product.from(
                ProductSnapshotState.builder()
                        .productKey(RandomCodeGenerator.generateProductKey())
                        .sellerId(1L)
                        .name(name)
                        .brandName("브랜드")
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

    private ProductOptionCategoryJpaEntity saveOptionCategory(ProductJpaEntity product, String name) {
        ProductOptionCategoryCreateState state = ProductOptionCategoryCreateState.builder()
                .product(null)
                .name(name)
                .optionStates(List.of())
                .build();
        ProductOptionCategory category = ProductOptionCategory.from(state);
        ProductOptionCategoryJpaEntity entity = ProductOptionCategoryJpaEntity.from(category, product);
        ProductOptionCategoryJpaEntity saved = productOptionCategoryJpaRepository.save(entity);
        saved.setIdToOrderNum();
        return saved;
    }

    private ProductOptionJpaEntity saveOption(ProductOptionCategoryJpaEntity category, String content) {
        ProductOptionCreateState state = ProductOptionCreateState.builder()
                .category(null)
                .content(content)
                .build();
        ProductOption option = ProductOption.from(state);
        ProductOptionJpaEntity entity = ProductOptionJpaEntity.from(category, option);
        entityManager.persist(entity);
        entityManager.flush();
        entity.setIdToOrderNum();
        return entity;
    }
}
