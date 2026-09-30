package com.personal.marketnote.product.adapter.out.persistence.category;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.category.entity.CategoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.category.repository.CategoryJpaRepository;
import com.personal.marketnote.product.domain.category.Category;
import com.personal.marketnote.product.domain.category.CategoryCreateState;
import com.personal.marketnote.product.domain.category.CategorySnapshotState;
import com.personal.marketnote.product.exception.CategoryNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryPersistenceAdapterTest {

    @Mock
    private CategoryJpaRepository categoryJpaRepository;

    @InjectMocks
    private CategoryPersistenceAdapter adapter;

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("존재하지 않으면 empty를 반환한다")
        void returnsEmptyWhenNotFound() {
            when(categoryJpaRepository.findById(1L)).thenReturn(Optional.empty());

            Optional<Category> result = adapter.findById(1L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하면 도메인으로 매핑한다")
        void returnsMappedDomain() {
            CategoryJpaEntity entity = mock(CategoryJpaEntity.class);
            when(entity.getId()).thenReturn(1L);
            when(entity.getParentCategoryId()).thenReturn(2L);
            when(entity.getName()).thenReturn("카테고리");
            when(entity.getStatus()).thenReturn(EntityStatus.ACTIVE);
            when(categoryJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            Optional<Category> result = adapter.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getParentCategoryId()).isEqualTo(2L);
            assertThat(result.get().getName()).isEqualTo("카테고리");
            assertThat(result.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("findActiveByParentId")
    class FindActiveByParentId {

        @Test
        @DisplayName("결과가 없으면 빈 리스트를 반환한다")
        void returnsEmptyList() {
            when(categoryJpaRepository.findActiveByParentId(2L)).thenReturn(List.of());

            List<Category> result = adapter.findActiveByParentId(2L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllActiveByIds")
    class FindAllActiveByIds {

        @Test
        @DisplayName("ACTIVE 상태로 조회한다")
        void callsRepositoryWithActiveStatus() {
            when(categoryJpaRepository.findAllByIdInAndStatus(List.of(1L, 2L), EntityStatus.ACTIVE))
                    .thenReturn(List.of());

            List<Category> result = adapter.findAllActiveByIds(List.of(1L, 2L));

            assertThat(result).isEmpty();
            verify(categoryJpaRepository).findAllByIdInAndStatus(List.of(1L, 2L), EntityStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("새 카테고리를 저장하고 저장된 도메인을 반환한다")
        void savesCategory() {
            Category category = Category.from(CategoryCreateState.of(2L, "새 카테고리"));
            CategoryJpaEntity savedEntity = mock(CategoryJpaEntity.class);
            when(savedEntity.getId()).thenReturn(10L);
            when(savedEntity.getParentCategoryId()).thenReturn(2L);
            when(savedEntity.getName()).thenReturn("새 카테고리");
            when(savedEntity.getStatus()).thenReturn(EntityStatus.ACTIVE);
            when(categoryJpaRepository.save(any(CategoryJpaEntity.class))).thenReturn(savedEntity);

            Category result = adapter.save(category);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getParentCategoryId()).isEqualTo(2L);
            assertThat(result.getName()).isEqualTo("새 카테고리");
            ArgumentCaptor<CategoryJpaEntity> captor = ArgumentCaptor.forClass(CategoryJpaEntity.class);
            verify(categoryJpaRepository).save(captor.capture());
            assertThat(captor.getValue().getParentCategoryId()).isEqualTo(2L);
            assertThat(captor.getValue().getName()).isEqualTo("새 카테고리");
        }
    }

    @Nested
    @DisplayName("existsById")
    class ExistsById {

        @Test
        @DisplayName("repository에 위임한다")
        void delegatesToRepository() {
            when(categoryJpaRepository.existsById(1L)).thenReturn(true);

            boolean result = adapter.existsById(1L);

            assertThat(result).isTrue();
            verify(categoryJpaRepository).existsById(1L);
        }
    }

    @Nested
    @DisplayName("existsChildren")
    class ExistsChildren {

        @Test
        @DisplayName("자식 카테고리 존재 여부를 ACTIVE 상태로 확인한다")
        void checksActiveChildren() {
            when(categoryJpaRepository.existsByParentCategoryIdAndStatus(1L, EntityStatus.ACTIVE))
                    .thenReturn(true);

            boolean result = adapter.existsChildren(1L);

            assertThat(result).isTrue();
            verify(categoryJpaRepository).existsByParentCategoryIdAndStatus(1L, EntityStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("repository에 위임하여 삭제한다")
        void delegatesToRepository() {
            adapter.deleteById(1L);

            verify(categoryJpaRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("카테고리가 존재하면 엔티티를 업데이트한다")
        void updatesExisting() {
            Category category = Category.from(
                    CategorySnapshotState.builder()
                            .id(1L)
                            .parentCategoryId(2L)
                            .name("업데이트")
                            .status(EntityStatus.ACTIVE)
                            .build()
            );
            CategoryJpaEntity entity = mock(CategoryJpaEntity.class);
            when(categoryJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            adapter.update(category);

            verify(entity).updateFrom(category);
        }

        @Test
        @DisplayName("카테고리가 없으면 CategoryNotFoundException을 던진다")
        void throwsWhenNotFound() {
            Category category = Category.from(
                    CategorySnapshotState.builder()
                            .id(99L)
                            .parentCategoryId(2L)
                            .name("없는 카테고리")
                            .status(EntityStatus.ACTIVE)
                            .build()
            );
            when(categoryJpaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adapter.update(category))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }
}
