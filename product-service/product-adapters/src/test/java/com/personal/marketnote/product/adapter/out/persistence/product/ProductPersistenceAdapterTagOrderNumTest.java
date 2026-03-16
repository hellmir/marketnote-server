package com.personal.marketnote.product.adapter.out.persistence.product;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductTagJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductCreateState;
import com.personal.marketnote.product.domain.product.ProductTagCreateState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, ProductPersistenceAdapter.class})
class ProductPersistenceAdapterTagOrderNumTest {

    @Autowired
    private ProductPersistenceAdapter productPersistenceAdapter;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Test
    @DisplayName("상품 등록 시 cascade 저장된 태그에 orderNum이 자동 할당된다")
    void shouldAssignOrderNumToTagsWhenProductIsSaved() {
        // given
        Product product = Product.from(
                ProductCreateState.builder()
                        .sellerId(1L)
                        .name("테스트 상품")
                        .brandName("테스트 브랜드")
                        .detail("상세 설명")
                        .findAllOptionsYn(false)
                        .tags(List.of(
                                ProductTagCreateState.builder().productId(null).name("태그A").build(),
                                ProductTagCreateState.builder().productId(null).name("태그B").build(),
                                ProductTagCreateState.builder().productId(null).name("태그C").build()
                        ))
                        .build()
        );

        // when
        Product savedProduct = productPersistenceAdapter.save(product);

        // then
        ProductJpaEntity savedEntity = productJpaRepository.findById(savedProduct.getId()).orElseThrow();
        List<ProductTagJpaEntity> tags = savedEntity.getProductTagJpaEntities();
        assertThat(tags).hasSize(3);
        for (ProductTagJpaEntity tag : tags) {
            assertThat(tag.getOrderNum())
                    .as("태그 '%s'의 orderNum이 할당되어야 한다", tag.getName())
                    .isNotNull()
                    .isEqualTo(tag.getId());
        }
    }

    @Test
    @DisplayName("상품 수정 시 재추가된 태그에 orderNum이 자동 할당된다")
    void shouldAssignOrderNumToTagsWhenProductIsUpdated() {
        // given
        Product product = Product.from(
                ProductCreateState.builder()
                        .sellerId(1L)
                        .name("테스트 상품")
                        .brandName("테스트 브랜드")
                        .detail("상세 설명")
                        .findAllOptionsYn(false)
                        .tags(List.of())
                        .build()
        );

        Product savedProduct = productPersistenceAdapter.save(product);

        // when
        savedProduct.update("테스트 상품", "테스트 브랜드", "상세 설명", false,
                List.of("신규태그A", "신규태그B"));
        productPersistenceAdapter.update(savedProduct);

        // then
        ProductJpaEntity updatedEntity = productJpaRepository.findById(savedProduct.getId()).orElseThrow();
        List<ProductTagJpaEntity> tags = updatedEntity.getProductTagJpaEntities();
        assertThat(tags).hasSize(2);
        for (ProductTagJpaEntity tag : tags) {
            assertThat(tag.getOrderNum())
                    .as("태그 '%s'의 orderNum이 할당되어야 한다", tag.getName())
                    .isNotNull()
                    .isEqualTo(tag.getId());
        }
    }
}
