package com.personal.marketnote.product.adapter.out.persistence.productcategory;

import com.personal.marketnote.product.adapter.out.persistence.productcategory.entity.ProductCategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productcategory.repository.ProductCategoryJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCategoryPersistenceAdapterTest {

    @Mock
    private ProductCategoryJpaRepository productCategoryJpaRepository;

    @InjectMocks
    private ProductCategoryPersistenceAdapter adapter;

    @Nested
    @DisplayName("replaceProductCategories")
    class ReplaceProductCategories {

        @Test
        @DisplayName("기존 매핑 삭제 후 새 매핑을 저장한다")
        void deletesExistingAndSavesNew() {
            adapter.replaceProductCategories(10L, List.of(100L, 200L));

            verify(productCategoryJpaRepository).deleteByProductId(10L);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<ProductCategoryJpaEntity>> captor = ArgumentCaptor.forClass(List.class);
            verify(productCategoryJpaRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(2);
            assertThat(captor.getValue().get(0).getProductId()).isEqualTo(10L);
            assertThat(captor.getValue().get(0).getCategoryId()).isEqualTo(100L);
            assertThat(captor.getValue().get(1).getProductId()).isEqualTo(10L);
            assertThat(captor.getValue().get(1).getCategoryId()).isEqualTo(200L);
        }

        @Test
        @DisplayName("categoryIds가 null이면 매핑 저장을 생략한다")
        void skipsSaveWhenCategoryIdsNull() {
            adapter.replaceProductCategories(10L, null);

            verify(productCategoryJpaRepository).deleteByProductId(10L);
            verify(productCategoryJpaRepository, never()).saveAll(any(List.class));
        }

        @Test
        @DisplayName("categoryIds가 비어 있으면 매핑 저장을 생략한다")
        void skipsSaveWhenCategoryIdsEmpty() {
            adapter.replaceProductCategories(10L, List.of());

            verify(productCategoryJpaRepository).deleteByProductId(10L);
            verify(productCategoryJpaRepository, never()).saveAll(any(List.class));
        }
    }

    @Nested
    @DisplayName("existsByCategoryId")
    class ExistsByCategoryId {

        @Test
        @DisplayName("repository에 위임한다")
        void delegatesToRepository() {
            when(productCategoryJpaRepository.existsByCategoryId(100L)).thenReturn(true);

            boolean result = adapter.existsByCategoryId(100L);

            assertThat(result).isTrue();
            verify(productCategoryJpaRepository).existsByCategoryId(100L);
        }
    }
}
