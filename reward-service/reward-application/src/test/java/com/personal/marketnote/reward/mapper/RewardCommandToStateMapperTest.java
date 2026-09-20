package com.personal.marketnote.reward.mapper;

import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointHistoryCreateState;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.port.in.command.point.ConfirmPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.command.point.RegisterUserPointCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RewardCommandToStateMapperTest {

    private static final Long USER_ID = 1L;
    private static final Long SOURCE_ID = 100L;
    private static final LocalDateTime ACCUMULATED_AT = LocalDateTime.of(2026, 4, 13, 10, 0);

    @Nested
    @DisplayName("mapToUserPointHistoryCreateState(ModifyUserPointCommand)")
    class ModifyUserPointMapping {

        @Test
        @DisplayName("DEDUCTION 입력 시 amount는 양수로 저장되고 changeType은 DEDUCTION으로 설정된다")
        void shouldKeepPositiveAmountAndSetDeductionChangeType() {
            ModifyUserPointCommand command = ModifyUserPointCommand.builder()
                    .userId(USER_ID)
                    .userKey("key")
                    .changeType(UserPointChangeType.DEDUCTION)
                    .amount(500L)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(SOURCE_ID)
                    .reason("주문 사용")
                    .build();

            UserPointHistoryCreateState state = RewardCommandToStateMapper.mapToUserPointHistoryCreateState(
                    command, USER_ID, ACCUMULATED_AT
            );

            assertThat(state.getAmount()).isEqualTo(500L);
            assertThat(state.getChangeType()).isEqualTo(UserPointChangeType.DEDUCTION);
            assertThat(state.getIsReflected()).isTrue();
        }

        @Test
        @DisplayName("ACCRUAL 입력 시 amount는 양수로 저장되고 changeType은 ACCRUAL로 설정된다")
        void shouldKeepPositiveAmountAndSetAccrualChangeType() {
            ModifyUserPointCommand command = ModifyUserPointCommand.builder()
                    .userId(USER_ID)
                    .userKey("key")
                    .changeType(UserPointChangeType.ACCRUAL)
                    .amount(1000L)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(SOURCE_ID)
                    .reason("주문 적립")
                    .build();

            UserPointHistoryCreateState state = RewardCommandToStateMapper.mapToUserPointHistoryCreateState(
                    command, USER_ID, ACCUMULATED_AT
            );

            assertThat(state.getAmount()).isEqualTo(1000L);
            assertThat(state.getChangeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        }
    }

    @Nested
    @DisplayName("mapToPendingPointHistoryCreateState(ModifyPendingPointCommand)")
    class ModifyPendingPointMapping {

        @Test
        @DisplayName("DEDUCTION 입력 시 amount는 양수로 저장되고 changeType은 DEDUCTION으로 설정된다")
        void shouldKeepPositiveAmountAndSetDeductionChangeType() {
            ModifyPendingPointCommand command = ModifyPendingPointCommand.builder()
                    .userId(USER_ID)
                    .changeType(UserPointChangeType.DEDUCTION)
                    .amount(300L)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(SOURCE_ID)
                    .reason("결제 부분 취소 차감")
                    .build();

            UserPointHistoryCreateState state = RewardCommandToStateMapper.mapToPendingPointHistoryCreateState(
                    command, USER_ID, ACCUMULATED_AT
            );

            assertThat(state.getAmount()).isEqualTo(300L);
            assertThat(state.getChangeType()).isEqualTo(UserPointChangeType.DEDUCTION);
            assertThat(state.getIsReflected()).isFalse();
        }

        @Test
        @DisplayName("ACCRUAL 입력 시 amount는 양수로 저장되고 changeType은 ACCRUAL로 설정된다")
        void shouldKeepPositiveAmountAndSetAccrualChangeType() {
            ModifyPendingPointCommand command = ModifyPendingPointCommand.builder()
                    .userId(USER_ID)
                    .changeType(UserPointChangeType.ACCRUAL)
                    .amount(700L)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(SOURCE_ID)
                    .reason("결제 적립 예정")
                    .build();

            UserPointHistoryCreateState state = RewardCommandToStateMapper.mapToPendingPointHistoryCreateState(
                    command, USER_ID, ACCUMULATED_AT
            );

            assertThat(state.getAmount()).isEqualTo(700L);
            assertThat(state.getChangeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        }
    }

    @Nested
    @DisplayName("mapToConfirmedPointHistoryCreateState(ConfirmPendingPointCommand)")
    class ConfirmPendingPointMapping {

        @Test
        @DisplayName("확정 이력은 항상 ACCRUAL changeType으로 저장된다")
        void shouldAlwaysSetAccrualChangeType() {
            ConfirmPendingPointCommand command = ConfirmPendingPointCommand.builder()
                    .userId(USER_ID)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(SOURCE_ID)
                    .reason("구매 확정")
                    .build();

            UserPointHistoryCreateState state = RewardCommandToStateMapper.mapToConfirmedPointHistoryCreateState(
                    command, 500L, USER_ID, ACCUMULATED_AT
            );

            assertThat(state.getAmount()).isEqualTo(500L);
            assertThat(state.getChangeType()).isEqualTo(UserPointChangeType.ACCRUAL);
            assertThat(state.getIsReflected()).isTrue();
        }
    }

    @Nested
    @DisplayName("mapToUserPointHistoryCreateState(RegisterUserPointCommand)")
    class RegisterUserPointMapping {

        @Test
        @DisplayName("회원 가입 이력은 ACCRUAL changeType + amount 0으로 저장된다")
        void shouldSetAccrualChangeTypeWithZeroAmount() {
            RegisterUserPointCommand command = RegisterUserPointCommand.of(USER_ID, "user-key");

            UserPointHistoryCreateState state = RewardCommandToStateMapper.mapToUserPointHistoryCreateState(
                    command, ACCUMULATED_AT
            );

            assertThat(state.getAmount()).isEqualTo(0L);
            assertThat(state.getChangeType()).isEqualTo(UserPointChangeType.ACCRUAL);
            assertThat(state.getIsReflected()).isTrue();
        }
    }
}
