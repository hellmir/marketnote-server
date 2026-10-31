package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.user.domain.user.exception.FingerprintNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserFingerprint")
class UserFingerprintTest {

    private static final LocalDateTime FIRST_SEEN_AT = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime LAST_SEEN_AT = LocalDateTime.of(2026, 4, 20, 12, 0);

    @Nested
    @DisplayName("from(CreateState)")
    class FromCreateState {

        @Test
        @DisplayName("유효한 CreateState로 UserFingerprint를 생성하면 firstSeenAt과 lastSeenAt이 동일하게 세팅된다")
        void shouldCreateWithSameFirstAndLastSeen() {
            UserFingerprintCreateState state = UserFingerprintCreateState.builder()
                    .userId(10L)
                    .fingerprint("fp-abc")
                    .source(FingerprintSource.LOGIN)
                    .firstSeenAt(FIRST_SEEN_AT)
                    .build();

            UserFingerprint fingerprint = UserFingerprint.from(state);

            assertThat(fingerprint.getUserId()).isEqualTo(10L);
            assertThat(fingerprint.getFingerprint()).isEqualTo("fp-abc");
            assertThat(fingerprint.getSource()).isEqualTo(FingerprintSource.LOGIN);
            assertThat(fingerprint.getFirstSeenAt()).isEqualTo(FIRST_SEEN_AT);
            assertThat(fingerprint.getLastSeenAt()).isEqualTo(FIRST_SEEN_AT);
            assertThat(fingerprint.getId()).isNull();
        }

        @Test
        @DisplayName("fingerprint가 null이면 FingerprintNoValueException을 던진다")
        void shouldThrowWhenFingerprintNull() {
            UserFingerprintCreateState state = UserFingerprintCreateState.builder()
                    .userId(10L)
                    .fingerprint(null)
                    .source(FingerprintSource.SIGNUP)
                    .firstSeenAt(FIRST_SEEN_AT)
                    .build();

            assertThatThrownBy(() -> UserFingerprint.from(state))
                    .isInstanceOf(FingerprintNoValueException.class);
        }

        @Test
        @DisplayName("fingerprint가 빈 문자열이면 FingerprintNoValueException을 던진다")
        void shouldThrowWhenFingerprintBlank() {
            UserFingerprintCreateState state = UserFingerprintCreateState.builder()
                    .userId(10L)
                    .fingerprint("")
                    .source(FingerprintSource.SIGNUP)
                    .firstSeenAt(FIRST_SEEN_AT)
                    .build();

            assertThatThrownBy(() -> UserFingerprint.from(state))
                    .isInstanceOf(FingerprintNoValueException.class);
        }
    }

    @Nested
    @DisplayName("from(SnapshotState)")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로 복원하면 모든 필드가 그대로 매핑된다")
        void shouldRestoreAllFields() {
            UserFingerprintSnapshotState state = UserFingerprintSnapshotState.builder()
                    .id(100L)
                    .userId(20L)
                    .fingerprint("fp-xyz")
                    .source(FingerprintSource.LOGIN)
                    .firstSeenAt(FIRST_SEEN_AT)
                    .lastSeenAt(LAST_SEEN_AT)
                    .build();

            UserFingerprint fingerprint = UserFingerprint.from(state);

            assertThat(fingerprint.getId()).isEqualTo(100L);
            assertThat(fingerprint.getUserId()).isEqualTo(20L);
            assertThat(fingerprint.getFingerprint()).isEqualTo("fp-xyz");
            assertThat(fingerprint.getSource()).isEqualTo(FingerprintSource.LOGIN);
            assertThat(fingerprint.getFirstSeenAt()).isEqualTo(FIRST_SEEN_AT);
            assertThat(fingerprint.getLastSeenAt()).isEqualTo(LAST_SEEN_AT);
        }
    }

    @Nested
    @DisplayName("updateLastSeenAt")
    class UpdateLastSeenAt {

        @Test
        @DisplayName("updateLastSeenAt 호출 시 lastSeenAt이 변경된다")
        void shouldUpdateLastSeenAt() {
            UserFingerprint fingerprint = UserFingerprint.from(
                    UserFingerprintSnapshotState.builder()
                            .id(1L)
                            .userId(10L)
                            .fingerprint("fp")
                            .source(FingerprintSource.LOGIN)
                            .firstSeenAt(FIRST_SEEN_AT)
                            .lastSeenAt(FIRST_SEEN_AT)
                            .build()
            );

            fingerprint.updateLastSeenAt(LAST_SEEN_AT);

            assertThat(fingerprint.getLastSeenAt()).isEqualTo(LAST_SEEN_AT);
            assertThat(fingerprint.getFirstSeenAt()).isEqualTo(FIRST_SEEN_AT);
        }
    }
}
