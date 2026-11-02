package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Terms")
class TermsTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime MODIFIED_AT = LocalDateTime.of(2026, 2, 1, 0, 0);

    @Nested
    @DisplayName("from(CreateState)")
    class FromCreateState {

        @Test
        @DisplayName("CreateState로 약관을 생성하면 id는 null이고 나머지 필드가 매핑된다")
        void shouldCreateFromCreateState() {
            TermsCreateState state = TermsCreateState.builder()
                    .content("테스트 약관 내용")
                    .requiredYn(true)
                    .createdAt(CREATED_AT)
                    .modifiedAt(MODIFIED_AT)
                    .status(EntityStatus.ACTIVE)
                    .build();

            Terms terms = Terms.from(state);

            assertThat(terms.getId()).isNull();
            assertThat(terms.getContent()).isEqualTo("테스트 약관 내용");
            assertThat(terms.getRequiredYn()).isTrue();
            assertThat(terms.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(terms.getModifiedAt()).isEqualTo(MODIFIED_AT);
            assertThat(terms.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("from(SnapshotState)")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로 복원하면 id 포함 모든 필드가 매핑된다")
        void shouldRestoreAllFields() {
            TermsSnapshotState state = TermsSnapshotState.builder()
                    .id(100L)
                    .content("복원된 약관")
                    .requiredYn(false)
                    .createdAt(CREATED_AT)
                    .modifiedAt(MODIFIED_AT)
                    .status(EntityStatus.INACTIVE)
                    .build();

            Terms terms = Terms.from(state);

            assertThat(terms.getId()).isEqualTo(100L);
            assertThat(terms.getContent()).isEqualTo("복원된 약관");
            assertThat(terms.getRequiredYn()).isFalse();
            assertThat(terms.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("isRequired")
    class IsRequired {

        @Test
        @DisplayName("requiredYn=true이면 isRequired는 true를 반환한다")
        void shouldReturnTrueWhenRequiredYnTrue() {
            Terms terms = Terms.from(
                    TermsSnapshotState.builder()
                            .id(1L)
                            .content("약관")
                            .requiredYn(true)
                            .status(EntityStatus.ACTIVE)
                            .build()
            );

            assertThat(terms.isRequired()).isTrue();
        }

        @Test
        @DisplayName("requiredYn=false이면 isRequired는 false를 반환한다")
        void shouldReturnFalseWhenRequiredYnFalse() {
            Terms terms = Terms.from(
                    TermsSnapshotState.builder()
                            .id(1L)
                            .content("약관")
                            .requiredYn(false)
                            .status(EntityStatus.ACTIVE)
                            .build()
            );

            assertThat(terms.isRequired()).isFalse();
        }
    }
}
