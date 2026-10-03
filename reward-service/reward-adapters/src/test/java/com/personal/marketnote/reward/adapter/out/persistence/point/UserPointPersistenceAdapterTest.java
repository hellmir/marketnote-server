package com.personal.marketnote.reward.adapter.out.persistence.point;

import com.personal.marketnote.reward.adapter.out.persistence.point.entity.UserPointJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.point.repository.UserPointJpaRepository;
import com.personal.marketnote.reward.domain.point.PointAmount;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointCreateState;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import com.personal.marketnote.reward.exception.UserPointNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPointPersistenceAdapter 테스트")
class UserPointPersistenceAdapterTest {

    private static final Long USER_ID = 1L;
    private static final String USER_KEY = "user-key-1";

    @Mock
    private UserPointJpaRepository repository;

    @InjectMocks
    private UserPointPersistenceAdapter adapter;

    private UserPointJpaEntity buildEntity(long amount) {
        UserPoint domain = UserPoint.from(UserPointSnapshotState.builder()
                .userId(USER_ID)
                .userKey(USER_KEY)
                .amount(PointAmount.of(amount))
                .addExpectedAmount(PointAmount.of(0L))
                .expireExpectedAmount(PointAmount.of(0L))
                .build());
        return UserPointJpaEntity.from(domain);
    }

    @Test
    @DisplayName("save는 엔티티를 저장하고 도메인을 반환한다")
    void shouldSaveAndReturnDomain() {
        // given
        UserPoint userPoint = UserPoint.from(UserPointCreateState.builder()
                .userId(USER_ID)
                .userKey(USER_KEY)
                .amount(PointAmount.of(0L))
                .addExpectedAmount(PointAmount.of(0L))
                .expireExpectedAmount(PointAmount.of(0L))
                .build());
        given(repository.save(any(UserPointJpaEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        UserPoint saved = adapter.save(userPoint);

        // then
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getUserKey()).isEqualTo(USER_KEY);
        verify(repository).save(any(UserPointJpaEntity.class));
    }

    @Test
    @DisplayName("existsByUserId는 리포지토리 결과를 그대로 반환한다")
    void shouldDelegateExistsByUserId() {
        given(repository.existsByUserId(USER_ID)).willReturn(true);
        assertThat(adapter.existsByUserId(USER_ID)).isTrue();
    }

    @Test
    @DisplayName("existsByUserKey는 리포지토리 결과를 그대로 반환한다")
    void shouldDelegateExistsByUserKey() {
        given(repository.existsByUserKey(USER_KEY)).willReturn(true);
        assertThat(adapter.existsByUserKey(USER_KEY)).isTrue();
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserIdTest {

        @Test
        @DisplayName("엔티티가 존재하면 도메인을 담은 Optional을 반환한다")
        void shouldReturnDomainWhenExists() {
            given(repository.findByUserId(USER_ID)).willReturn(Optional.of(buildEntity(5_000L)));
            Optional<UserPoint> result = adapter.findByUserId(USER_ID);
            assertThat(result).isPresent();
            assertThat(result.get().getAmount().getValue()).isEqualTo(5_000L);
        }

        @Test
        @DisplayName("엔티티가 없으면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNoEntity() {
            given(repository.findByUserId(USER_ID)).willReturn(Optional.empty());
            assertThat(adapter.findByUserId(USER_ID)).isEmpty();
        }
    }

    @Test
    @DisplayName("findByUserKey는 엔티티를 도메인으로 변환한다")
    void shouldFindByUserKey() {
        given(repository.findByUserKey(USER_KEY)).willReturn(Optional.of(buildEntity(2_000L)));
        Optional<UserPoint> result = adapter.findByUserKey(USER_KEY);
        assertThat(result).isPresent();
        assertThat(result.get().getUserKey()).isEqualTo(USER_KEY);
    }

    @Test
    @DisplayName("findByUserIdForUpdate는 비관적 잠금 조회 결과를 도메인으로 반환한다")
    void shouldFindByUserIdForUpdate() {
        given(repository.findWithLockingByUserId(USER_ID)).willReturn(Optional.of(buildEntity(3_000L)));
        Optional<UserPoint> result = adapter.findByUserIdForUpdate(USER_ID);
        assertThat(result).isPresent();
        verify(repository).findWithLockingByUserId(USER_ID);
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("기존 엔티티가 존재하면 updateFrom으로 도메인 상태를 반영한다")
        void shouldUpdateExistingEntity() {
            // given
            UserPointJpaEntity existing = buildEntity(0L);
            given(repository.findByUserId(USER_ID)).willReturn(Optional.of(existing));
            UserPoint domain = existing.toDomain().withAmount(10_000L);

            // when
            UserPoint result = adapter.update(domain);

            // then
            assertThat(existing.getAmount()).isEqualTo(10_000L);
            assertThat(result.getAmount().getValue()).isEqualTo(10_000L);
        }

        @Test
        @DisplayName("기존 엔티티가 없으면 UserPointNotFoundException을 던진다")
        void shouldThrowNotFoundWhenNoEntity() {
            // given
            UserPoint domain = UserPoint.from(UserPointCreateState.builder()
                    .userId(USER_ID)
                    .userKey(USER_KEY)
                    .amount(PointAmount.of(0L))
                    .addExpectedAmount(PointAmount.of(0L))
                    .expireExpectedAmount(PointAmount.of(0L))
                    .build());
            given(repository.findByUserId(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adapter.update(domain))
                    .isInstanceOf(UserPointNotFoundException.class);
        }
    }
}
