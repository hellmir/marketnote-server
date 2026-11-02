package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonBrandJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository.GifticonBrandJpaRepository;
import com.personal.marketnote.reward.domain.exception.GifticonBrandNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonBrand;
import com.personal.marketnote.reward.domain.gifticon.GifticonBrandSnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GifticonBrandPersistenceAdapter 테스트")
class GifticonBrandPersistenceAdapterTest {

    @Mock
    private GifticonBrandJpaRepository repository;

    @InjectMocks
    private GifticonBrandPersistenceAdapter adapter;

    private GifticonBrandJpaEntity buildEntity(Long id, String code) {
        GifticonBrand restored = GifticonBrand.from(GifticonBrandSnapshotState.builder()
                .id(id)
                .brandCode(code)
                .brandName("스타벅스")
                .brandImageUrl("http://img.test.com/brand.jpg")
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
        return GifticonBrandJpaEntity.from(restored);
    }

    @Test
    @DisplayName("findByBrandCode는 엔티티를 도메인으로 변환한다")
    void shouldFindByBrandCode() {
        given(repository.findByBrandCode("B001")).willReturn(Optional.of(buildEntity(1L, "B001")));
        Optional<GifticonBrand> result = adapter.findByBrandCode("B001");
        assertThat(result).isPresent();
        assertThat(result.get().getBrandCode().getValue()).isEqualTo("B001");
    }

    @Test
    @DisplayName("save는 엔티티를 저장한다")
    void shouldSave() {
        GifticonBrand domain = buildEntity(1L, "B001").toDomain();
        adapter.save(domain);
        verify(repository).save(any(GifticonBrandJpaEntity.class));
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("기존 엔티티가 존재하면 updateFrom으로 도메인 상태를 반영한다")
        void shouldUpdateExistingEntity() {
            GifticonBrandJpaEntity existing = buildEntity(1L, "B001");
            given(repository.findById(1L)).willReturn(Optional.of(existing));
            adapter.update(existing.toDomain());
            verify(repository).findById(1L);
        }

        @Test
        @DisplayName("기존 엔티티가 없으면 GifticonBrandNotFoundException을 던진다")
        void shouldThrowNotFoundWhenNoEntity() {
            GifticonBrandJpaEntity sample = buildEntity(99L, "B099");
            given(repository.findById(99L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> adapter.update(sample.toDomain()))
                    .isInstanceOf(GifticonBrandNotFoundException.class);
        }
    }
}
