package com.personal.marketnote.product.adapter.out.persistence.product;

import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductTagJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductTagJpaRepository;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.domain.product.ProductTagCreateState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductTagPersistenceAdapterTest {

    @InjectMocks
    private ProductTagPersistenceAdapter adapter;

    @Mock
    private ProductTagJpaRepository productTagJpaRepository;

    @Nested
    @DisplayName("updateOrderNums")
    class UpdateOrderNums {

        @Test
        @DisplayName("주어진 태그 ID에 맵에 지정된 orderNum을 설정한다")
        void updatesOrderNumsForGivenTags() {
            ProductTagJpaEntity tag1 = buildTagEntity(1L, "태그1");
            ProductTagJpaEntity tag2 = buildTagEntity(2L, "태그2");
            when(productTagJpaRepository.findAllByProductJpaEntityIdAndIdIn(eq(10L), anyList()))
                    .thenReturn(List.of(tag1, tag2));

            Map<Long, Long> orderMap = new LinkedHashMap<>();
            orderMap.put(1L, 100L);
            orderMap.put(2L, 200L);
            adapter.updateOrderNums(10L, orderMap);

            assertThat(tag1.getOrderNum()).isEqualTo(100L);
            assertThat(tag2.getOrderNum()).isEqualTo(200L);
            verify(productTagJpaRepository).findAllByProductJpaEntityIdAndIdIn(eq(10L), anyList());
            verifyNoMoreInteractions(productTagJpaRepository);
        }

        @Test
        @DisplayName("맵에 있는 태그 ID는 새 orderNum으로 갱신한다")
        void updatesSingleTagOrderNum() {
            ProductTagJpaEntity tag1 = buildTagEntity(1L, "태그1");
            tag1.changeOrderNum(999L);
            when(productTagJpaRepository.findAllByProductJpaEntityIdAndIdIn(eq(10L), anyList()))
                    .thenReturn(List.of(tag1));

            adapter.updateOrderNums(10L, Map.of(1L, 100L));

            assertThat(tag1.getOrderNum()).isEqualTo(100L);
        }

        @Test
        @DisplayName("빈 맵을 전달하면 repository 조회만 하고 아무것도 변경하지 않는다")
        void doesNothingForEmptyMap() {
            when(productTagJpaRepository.findAllByProductJpaEntityIdAndIdIn(eq(10L), anyList()))
                    .thenReturn(List.of());

            adapter.updateOrderNums(10L, Map.of());

            verify(productTagJpaRepository).findAllByProductJpaEntityIdAndIdIn(eq(10L), anyList());
            verifyNoMoreInteractions(productTagJpaRepository);
        }
    }

    private ProductTagJpaEntity buildTagEntity(Long id, String name) {
        ProductJpaEntity productEntity = mock(ProductJpaEntity.class);
        ProductTag tag = ProductTag.from(ProductTagCreateState.builder().name(name).build());
        ProductTagJpaEntity entity = ProductTagJpaEntity.from(productEntity, tag);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
