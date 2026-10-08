package com.personal.marketnote.community.adapter.out.persistence.like;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.like.repository.LikeJpaRepository;
import com.personal.marketnote.community.domain.like.Like;
import com.personal.marketnote.community.domain.like.LikeCreateState;
import com.personal.marketnote.community.domain.like.LikeSnapshotState;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import com.personal.marketnote.community.exception.LikeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikePersistenceAdapter")
class LikePersistenceAdapterTest {

    @Mock
    private LikeJpaRepository likeJpaRepository;

    @InjectMocks
    private LikePersistenceAdapter adapter;

    private Like activeLike;

    @BeforeEach
    void setUp() {
        activeLike = Like.from(
                LikeCreateState.builder()
                        .targetType(LikeTargetType.REVIEW)
                        .targetId(100L)
                        .userId(10L)
                        .build()
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("도메인을 엔티티로 변환해 저장한다")
        void savesDomainAsEntity() {
            ArgumentCaptor<LikeJpaEntity> captor = ArgumentCaptor.forClass(LikeJpaEntity.class);

            adapter.save(activeLike);

            verify(likeJpaRepository).save(captor.capture());
            LikeJpaEntity saved = captor.getValue();
            assertThat(saved.getId().getTargetType()).isEqualTo(LikeTargetType.REVIEW);
            assertThat(saved.getId().getTargetId()).isEqualTo(100L);
            assertThat(saved.getId().getUserId()).isEqualTo(10L);
            assertThat(saved.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("existsByTargetAndUser")
    class ExistsByTargetAndUser {

        @Test
        @DisplayName("리포지토리의 existsByTarget 결과를 그대로 반환한다 - true")
        void delegatesExistsByTargetReturningTrue() {
            when(likeJpaRepository.existsByTarget(LikeTargetType.BOARD, 200L, 20L)).thenReturn(true);

            boolean result = adapter.existsByTargetAndUser(LikeTargetType.BOARD, 200L, 20L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("리포지토리의 existsByTarget 결과를 그대로 반환한다 - false")
        void delegatesExistsByTargetReturningFalse() {
            when(likeJpaRepository.existsByTarget(LikeTargetType.REVIEW, 300L, 30L)).thenReturn(false);

            boolean result = adapter.existsByTargetAndUser(LikeTargetType.REVIEW, 300L, 30L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findByTargetAndUser")
    class FindByTargetAndUser {

        @Test
        @DisplayName("엔티티가 존재하면 도메인으로 변환해 Optional로 반환한다")
        void returnsDomainWhenEntityFound() {
            LikeJpaEntity entity = newActiveEntity(LikeTargetType.REVIEW, 100L, 10L);
            when(likeJpaRepository.findByTargetAndUser(LikeTargetType.REVIEW, 100L, 10L))
                    .thenReturn(Optional.of(entity));

            Optional<Like> result = adapter.findByTargetAndUser(LikeTargetType.REVIEW, 100L, 10L);

            assertThat(result).isPresent();
            assertThat(result.get().getTargetType()).isEqualTo(LikeTargetType.REVIEW);
            assertThat(result.get().getTargetId()).isEqualTo(100L);
            assertThat(result.get().getUserId()).isEqualTo(10L);
            assertThat(result.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("엔티티가 없으면 LikeNotFoundException을 던진다")
        void throwsWhenEntityMissing() {
            when(likeJpaRepository.findByTargetAndUser(LikeTargetType.BOARD, 999L, 99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> adapter.findByTargetAndUser(LikeTargetType.BOARD, 999L, 99L))
                    .isInstanceOf(LikeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("엔티티를 조회해 도메인 상태로 업데이트한다")
        void updatesEntityStatusFromDomain() {
            LikeJpaEntity entity = newActiveEntity(LikeTargetType.REVIEW, 100L, 10L);
            when(likeJpaRepository.findByTargetAndUser(LikeTargetType.REVIEW, 100L, 10L))
                    .thenReturn(Optional.of(entity));

            Like revertedLike = Like.from(
                    LikeSnapshotState.builder()
                            .targetType(LikeTargetType.REVIEW)
                            .targetId(100L)
                            .userId(10L)
                            .status(EntityStatus.INACTIVE)
                            .createdAt(LocalDateTime.now())
                            .modifiedAt(LocalDateTime.now())
                            .build()
            );

            adapter.update(revertedLike);

            assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);
            verify(likeJpaRepository, never()).save(entity);
        }

        @Test
        @DisplayName("엔티티가 없으면 LikeNotFoundException을 던진다")
        void throwsWhenEntityMissing() {
            when(likeJpaRepository.findByTargetAndUser(LikeTargetType.BOARD, 404L, 40L))
                    .thenReturn(Optional.empty());

            Like missing = Like.from(
                    LikeCreateState.builder()
                            .targetType(LikeTargetType.BOARD)
                            .targetId(404L)
                            .userId(40L)
                            .build()
            );

            assertThatThrownBy(() -> adapter.update(missing))
                    .isInstanceOf(LikeNotFoundException.class);
        }
    }

    private LikeJpaEntity newActiveEntity(LikeTargetType targetType, Long targetId, Long userId) {
        Like like = Like.from(
                LikeCreateState.builder()
                        .targetType(targetType)
                        .targetId(targetId)
                        .userId(userId)
                        .build()
        );
        LikeJpaEntity entity = LikeJpaEntity.from(like);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(entity, "modifiedAt", LocalDateTime.now());
        return entity;
    }
}
