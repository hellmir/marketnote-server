package com.personal.marketnote.reward.domain.point;

import com.personal.marketnote.reward.domain.exception.InvalidPointAmountException;
import com.personal.marketnote.reward.domain.exception.UserPointHistoryAmountNoValueException;
import com.personal.marketnote.reward.domain.exception.UserPointHistoryChangeTypeNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserPointHistoryTest {

    private static final Long USER_ID = 1L;
    private static final Long SOURCE_ID = 100L;
    private static final String REASON = "테스트 사유";
    private static final LocalDateTime ACCUMULATED_AT = LocalDateTime.of(2026, 4, 13, 10, 0);
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 4, 13, 10, 0);

    private UserPointHistoryCreateState createState(UserPointChangeType changeType, Long amount) {
        return createStateWithPointAmount(changeType, amount == null ? null : PointAmount.of(amount));
    }

    private UserPointHistoryCreateState createStateWithPointAmount(UserPointChangeType changeType, PointAmount amount) {
        return UserPointHistoryCreateState.builder()
                .userId(USER_ID)
                .changeType(changeType)
                .amount(amount)
                .isReflected(false)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(SOURCE_ID)
                .reason(REASON)
                .accumulatedAt(ACCUMULATED_AT)
                .build();
    }

    private UserPointHistorySnapshotState snapshotState(UserPointChangeType changeType, Long amount) {
        return UserPointHistorySnapshotState.builder()
                .id(1L)
                .userId(USER_ID)
                .changeType(changeType)
                .amount(PointAmount.of(amount))
                .isReflected(false)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(SOURCE_ID)
                .reason(REASON)
                .accumulatedAt(ACCUMULATED_AT)
                .createdAt(CREATED_AT)
                .build();
    }

    @Nested
    @DisplayName("from(UserPointHistoryCreateState)")
    class FromCreateState {

        @Test
        @DisplayName("CreateState의 changeType이 도메인 객체에 매핑된다")
        void shouldMapChangeTypeFromCreateState() {
            UserPointHistory history = UserPointHistory.from(createState(UserPointChangeType.DEDUCTION, 500L));

            assertThat(history.getChangeType()).isEqualTo(UserPointChangeType.DEDUCTION);
            assertThat(history.getAmountValue()).isEqualTo(500L);
        }

        @Test
        @DisplayName("CreateState의 ACCRUAL changeType이 도메인 객체에 매핑된다")
        void shouldMapAccrualChangeTypeFromCreateState() {
            UserPointHistory history = UserPointHistory.from(createState(UserPointChangeType.ACCRUAL, 1000L));

            assertThat(history.getChangeType()).isEqualTo(UserPointChangeType.ACCRUAL);
            assertThat(history.getAmountValue()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("CreateState의 changeType이 null이면 UserPointHistoryChangeTypeNoValueException이 발생한다")
        void shouldThrowWhenChangeTypeIsNull() {
            assertThatThrownBy(() -> UserPointHistory.from(createState(null, 500L)))
                    .isInstanceOf(UserPointHistoryChangeTypeNoValueException.class);
        }

        @Test
        @DisplayName("CreateState의 amount가 null이면 UserPointHistoryAmountNoValueException이 발생한다")
        void shouldThrowWhenAmountIsNull() {
            assertThatThrownBy(() -> UserPointHistory.from(createStateWithPointAmount(UserPointChangeType.ACCRUAL, null)))
                    .isInstanceOf(UserPointHistoryAmountNoValueException.class);
        }

        @Test
        @DisplayName("CreateState의 amount에 음수를 전달하면 PointAmount 생성 시점에 InvalidPointAmountException이 발생한다")
        void shouldThrowWhenAmountIsNegative() {
            assertThatThrownBy(() -> createState(UserPointChangeType.DEDUCTION, -100L))
                    .isInstanceOf(InvalidPointAmountException.class);
        }

        @Test
        @DisplayName("CreateState의 amount가 0이면 정상 생성된다 (회원가입 케이스)")
        void shouldAllowZeroAmount() {
            UserPointHistory history = UserPointHistory.from(createState(UserPointChangeType.ACCRUAL, 0L));

            assertThat(history.getAmountValue()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("from(UserPointHistorySnapshotState)")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState의 changeType이 도메인 객체에 매핑된다")
        void shouldMapChangeTypeFromSnapshotState() {
            UserPointHistory history = UserPointHistory.from(snapshotState(UserPointChangeType.DEDUCTION, 700L));

            assertThat(history.getChangeType()).isEqualTo(UserPointChangeType.DEDUCTION);
            assertThat(history.getAmountValue()).isEqualTo(700L);
        }

        @Test
        @DisplayName("SnapshotState에서 PointAmount 타입으로 amount를 복원한다")
        void shouldRestorePointAmountFromSnapshotState() {
            UserPointHistory history = UserPointHistory.from(snapshotState(UserPointChangeType.ACCRUAL, 1500L));

            assertThat(history.getAmount()).isEqualTo(PointAmount.of(1500L));
        }
    }

    @Nested
    @DisplayName("PointAmount 타입 적용")
    class PointAmountTypeApplication {

        @Test
        @DisplayName("CreateState에 PointAmount를 직접 전달하면 동일 값으로 생성된다")
        void shouldCreateUserPointHistoryWithPointAmountDirectly() {
            UserPointHistoryCreateState state = createStateWithPointAmount(
                    UserPointChangeType.ACCRUAL, PointAmount.of(2500L)
            );

            UserPointHistory history = UserPointHistory.from(state);

            assertThat(history.getAmount()).isEqualTo(PointAmount.of(2500L));
            assertThat(history.getAmountValue()).isEqualTo(2500L);
        }

        @Test
        @DisplayName("CreateState에 PointAmount.zero()를 전달하면 amount는 zero로 생성된다")
        void shouldCreateUserPointHistoryWithPointAmountZero() {
            UserPointHistoryCreateState state = createStateWithPointAmount(
                    UserPointChangeType.ACCRUAL, PointAmount.zero()
            );

            UserPointHistory history = UserPointHistory.from(state);

            assertThat(history.getAmount()).isEqualTo(PointAmount.zero());
            assertThat(history.getAmountValue()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("isAccrual / isDeduction")
    class Predicates {

        @Test
        @DisplayName("changeType이 ACCRUAL이면 isAccrual은 true, isDeduction은 false를 반환한다")
        void shouldReturnTrueForAccrualWhenChangeTypeIsAccrual() {
            UserPointHistory history = UserPointHistory.from(createState(UserPointChangeType.ACCRUAL, 100L));

            assertThat(history.isAccrual()).isTrue();
            assertThat(history.isDeduction()).isFalse();
        }

        @Test
        @DisplayName("changeType이 DEDUCTION이면 isDeduction은 true, isAccrual은 false를 반환한다")
        void shouldReturnTrueForDeductionWhenChangeTypeIsDeduction() {
            UserPointHistory history = UserPointHistory.from(createState(UserPointChangeType.DEDUCTION, 100L));

            assertThat(history.isDeduction()).isTrue();
            assertThat(history.isAccrual()).isFalse();
        }
    }

    @Nested
    @DisplayName("signedAmount")
    class SignedAmount {

        @Test
        @DisplayName("changeType이 ACCRUAL이면 signedAmount는 양수 amount를 그대로 반환한다")
        void shouldReturnPositiveAmountWhenAccrual() {
            UserPointHistory history = UserPointHistory.from(createState(UserPointChangeType.ACCRUAL, 500L));

            assertThat(history.signedAmount()).isEqualTo(500L);
        }

        @Test
        @DisplayName("changeType이 DEDUCTION이면 signedAmount는 음수 amount를 반환한다")
        void shouldReturnNegativeAmountWhenDeduction() {
            UserPointHistory history = UserPointHistory.from(createState(UserPointChangeType.DEDUCTION, 500L));

            assertThat(history.signedAmount()).isEqualTo(-500L);
        }

        @Test
        @DisplayName("amount가 0이면 signedAmount도 0을 반환한다")
        void shouldReturnZeroWhenAmountIsZero() {
            UserPointHistory history = UserPointHistory.from(createState(UserPointChangeType.ACCRUAL, 0L));

            assertThat(history.signedAmount()).isEqualTo(0L);
        }
    }
}
