package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonCategoryJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonCategoryMappingJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository.GifticonCategoryJpaRepository;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository.GifticonCategoryMappingJpaRepository;
import com.personal.marketnote.reward.domain.exception.GifticonCategoryNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategorySnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GifticonCategoryPersistenceAdapter 테스트")
class GifticonCategoryPersistenceAdapterTest {

    @Mock
    private GifticonCategoryJpaRepository categoryRepository;
    @Mock
    private GifticonCategoryMappingJpaRepository mappingRepository;

    @InjectMocks
    private GifticonCategoryPersistenceAdapter adapter;

    private GifticonCategoryJpaEntity buildEntity(Long id, String code) {
        GifticonCategory restored = GifticonCategory.from(GifticonCategorySnapshotState.builder()
                .id(id)
                .categoryCode(code)
                .categoryName("카테고리")
                .displayName("카테고리 표시명")
                .iconUrl("http://img.test.com/icon.png")
                .exposed(true)
                .orderNum(1)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
        return GifticonCategoryJpaEntity.from(restored);
    }

    @Test
    @DisplayName("findByCategoryCode는 엔티티를 도메인으로 변환한다")
    void shouldFindByCategoryCode() {
        given(categoryRepository.findByCategoryCode("C001")).willReturn(Optional.of(buildEntity(1L, "C001")));
        Optional<GifticonCategory> result = adapter.findByCategoryCode("C001");
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findById는 엔티티를 도메인으로 변환한다")
    void shouldFindById() {
        given(categoryRepository.findById(1L)).willReturn(Optional.of(buildEntity(1L, "C001")));
        Optional<GifticonCategory> result = adapter.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findAllOrderByOrderNumAsc는 정렬된 카테고리 목록을 반환한다")
    void shouldFindAllOrderedByOrderNum() {
        given(categoryRepository.findAllByOrderByOrderNumAsc())
                .willReturn(List.of(buildEntity(1L, "C001"), buildEntity(2L, "C002")));
        List<GifticonCategory> result = adapter.findAllOrderByOrderNumAsc();
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findAllExposed는 노출 카테고리 목록을 반환한다")
    void shouldFindAllExposed() {
        given(categoryRepository.findAllExposed())
                .willReturn(List.of(buildEntity(1L, "C001")));
        List<GifticonCategory> result = adapter.findAllExposed();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("save는 엔티티를 저장하고 도메인을 반환한다")
    void shouldSave() {
        GifticonCategoryJpaEntity entity = buildEntity(1L, "C001");
        given(categoryRepository.save(any(GifticonCategoryJpaEntity.class))).willReturn(entity);
        GifticonCategory result = adapter.save(entity.toDomain());
        assertThat(result.getCategoryCode().getValue()).isEqualTo("C001");
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("기존 엔티티가 존재하면 updateFrom으로 도메인 상태를 반영한다")
        void shouldUpdateExistingEntity() {
            GifticonCategoryJpaEntity existing = buildEntity(1L, "C001");
            given(categoryRepository.findById(1L)).willReturn(Optional.of(existing));
            adapter.update(existing.toDomain());
            verify(categoryRepository).findById(1L);
        }

        @Test
        @DisplayName("기존 엔티티가 없으면 GifticonCategoryNotFoundException을 던진다")
        void shouldThrowNotFoundWhenNoEntity() {
            GifticonCategoryJpaEntity sample = buildEntity(99L, "C099");
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> adapter.update(sample.toDomain()))
                    .isInstanceOf(GifticonCategoryNotFoundException.class);
        }
    }

    @Test
    @DisplayName("findCategoryIdByGiftishowCategorySeq는 매핑 엔티티의 카테고리 ID를 반환한다")
    void shouldFindCategoryIdByGiftishowSeq() {
        GifticonCategoryMappingJpaEntity mapping = GifticonCategoryMappingJpaEntity.of("seq1", 5L);
        given(mappingRepository.findByGiftishowCategorySeq("seq1")).willReturn(Optional.of(mapping));
        Optional<Long> result = adapter.findCategoryIdByGiftishowCategorySeq("seq1");
        assertThat(result).contains(5L);
    }

    @Test
    @DisplayName("save 매핑은 mappingRepository에 저장한다")
    void shouldSaveMapping() {
        adapter.save("seq1", 10L);
        verify(mappingRepository).save(any(GifticonCategoryMappingJpaEntity.class));
    }
}
