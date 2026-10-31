package com.personal.marketnote.user.adapter.out.persistence;

import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserFingerprintJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.repository.UserFingerprintJpaRepository;
import com.personal.marketnote.user.domain.user.FingerprintSource;
import com.personal.marketnote.user.domain.user.UserFingerprint;
import com.personal.marketnote.user.domain.user.UserFingerprintSnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFingerprintPersistenceAdapter")
class UserFingerprintPersistenceAdapterTest {

    @Mock
    private UserFingerprintJpaRepository userFingerprintJpaRepository;

    @InjectMocks
    private UserFingerprintPersistenceAdapter adapter;

    private static final LocalDateTime FIRST_SEEN_AT = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime LAST_SEEN_AT = LocalDateTime.of(2026, 4, 20, 10, 30);

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("기존 핑거프린트가 존재하면 lastSeenAt만 업데이트하고 신규 저장은 호출하지 않는다")
        void updatesLastSeenAtWhenExists() {
            UserFingerprint domain = buildDomain(10L, "fp-abc", FIRST_SEEN_AT, LAST_SEEN_AT);
            UserFingerprintJpaEntity existing = UserFingerprintJpaEntity.of(10L, "fp-abc", FingerprintSource.LOGIN, FIRST_SEEN_AT);
            when(userFingerprintJpaRepository.findByUserIdAndFingerprint(10L, "fp-abc"))
                    .thenReturn(Optional.of(existing));

            adapter.save(domain);

            assertThat(existing.getLastSeenAt()).isEqualTo(LAST_SEEN_AT);
            verify(userFingerprintJpaRepository, never()).save(any(UserFingerprintJpaEntity.class));
        }

        @Test
        @DisplayName("기존 핑거프린트가 없으면 신규 엔티티를 저장한다")
        void savesNewEntityWhenNotExists() {
            UserFingerprint domain = buildDomain(20L, "fp-new", FIRST_SEEN_AT, FIRST_SEEN_AT);
            when(userFingerprintJpaRepository.findByUserIdAndFingerprint(20L, "fp-new"))
                    .thenReturn(Optional.empty());

            adapter.save(domain);

            ArgumentCaptor<UserFingerprintJpaEntity> captor = ArgumentCaptor.forClass(UserFingerprintJpaEntity.class);
            verify(userFingerprintJpaRepository).save(captor.capture());
            UserFingerprintJpaEntity saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(20L);
            assertThat(saved.getFingerprint()).isEqualTo("fp-new");
            assertThat(saved.getSource()).isEqualTo(FingerprintSource.LOGIN);
            assertThat(saved.getFirstSeenAt()).isEqualTo(FIRST_SEEN_AT);
            assertThat(saved.getLastSeenAt()).isEqualTo(FIRST_SEEN_AT);
        }
    }

    @Nested
    @DisplayName("findByUserIdAndFingerprint")
    class FindByUserIdAndFingerprint {

        @Test
        @DisplayName("엔티티가 존재하면 도메인으로 매핑하여 반환한다")
        void returnsDomainWhenFound() {
            UserFingerprintJpaEntity entity = UserFingerprintJpaEntity.of(30L, "fp-xyz", FingerprintSource.SIGNUP, FIRST_SEEN_AT);
            when(userFingerprintJpaRepository.findByUserIdAndFingerprint(30L, "fp-xyz"))
                    .thenReturn(Optional.of(entity));

            Optional<UserFingerprint> result = adapter.findByUserIdAndFingerprint(30L, "fp-xyz");

            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(30L);
            assertThat(result.get().getFingerprint()).isEqualTo("fp-xyz");
            assertThat(result.get().getSource()).isEqualTo(FingerprintSource.SIGNUP);
        }

        @Test
        @DisplayName("엔티티가 없으면 empty를 반환한다")
        void returnsEmptyWhenNotFound() {
            when(userFingerprintJpaRepository.findByUserIdAndFingerprint(99L, "missing"))
                    .thenReturn(Optional.empty());

            Optional<UserFingerprint> result = adapter.findByUserIdAndFingerprint(99L, "missing");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserId {

        @Test
        @DisplayName("사용자의 핑거프린트 리스트를 도메인으로 매핑하여 반환한다")
        void returnsListOfDomains() {
            UserFingerprintJpaEntity first = UserFingerprintJpaEntity.of(40L, "fp-1", FingerprintSource.LOGIN, FIRST_SEEN_AT);
            UserFingerprintJpaEntity second = UserFingerprintJpaEntity.of(40L, "fp-2", FingerprintSource.SIGNUP, FIRST_SEEN_AT);
            when(userFingerprintJpaRepository.findByUserId(40L)).thenReturn(List.of(first, second));

            List<UserFingerprint> result = adapter.findByUserId(40L);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserFingerprint::getFingerprint)
                    .containsExactly("fp-1", "fp-2");
        }

        @Test
        @DisplayName("핑거프린트가 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNone() {
            when(userFingerprintJpaRepository.findByUserId(50L)).thenReturn(List.of());

            List<UserFingerprint> result = adapter.findByUserId(50L);

            assertThat(result).isEmpty();
        }
    }

    private UserFingerprint buildDomain(Long userId, String fingerprint, LocalDateTime firstSeenAt, LocalDateTime lastSeenAt) {
        return UserFingerprint.from(
                UserFingerprintSnapshotState.builder()
                        .userId(userId)
                        .fingerprint(fingerprint)
                        .source(FingerprintSource.LOGIN)
                        .firstSeenAt(firstSeenAt)
                        .lastSeenAt(lastSeenAt)
                        .build()
        );
    }
}
