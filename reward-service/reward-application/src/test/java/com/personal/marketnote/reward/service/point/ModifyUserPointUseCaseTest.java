package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.reward.domain.point.*;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.exception.UserPointNotFoundException;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import com.personal.marketnote.reward.port.out.point.SaveUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModifyUserPointUseCaseTest {

    @InjectMocks
    private ModifyUserPointService modifyUserPointService;

    @Mock
    private GetUserPointUseCase getUserPointUseCase;

    @Mock
    private FindUserPointPort findUserPointPort;

    @Mock
    private UpdateUserPointPort updateUserPointPort;

    @Mock
    private SaveUserPointHistoryPort saveUserPointHistoryPort;

    private static final Long USER_ID = 1L;
    private static final String USER_KEY = "test-user-key";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 5, 10, 0);

    private UserPoint createUserPoint(Long amount) {
        return UserPoint.from(UserPointSnapshotState.builder()
                .userId(USER_ID)
                .userKey(USER_KEY)
                .amount(amount)
                .addExpectedAmount(0L)
                .expireExpectedAmount(0L)
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build());
    }

    private ModifyUserPointCommand createAccrualCommandWithUserId(Long amount) {
        return ModifyUserPointCommand.builder()
                .userId(USER_ID)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount(amount)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(100L)
                .reason("상품 구매 적립")
                .build();
    }

    private ModifyUserPointCommand createDeductionCommandWithUserId(Long amount) {
        return ModifyUserPointCommand.builder()
                .userId(USER_ID)
                .changeType(UserPointChangeType.DEDUCTION)
                .amount(amount)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(100L)
                .reason("포인트 사용")
                .build();
    }

    private ModifyUserPointCommand createAccrualCommandWithUserKey(Long amount) {
        return ModifyUserPointCommand.builder()
                .userKey(USER_KEY)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount(amount)
                .sourceType(UserPointSourceType.ATTENDENCE)
                .sourceId(200L)
                .reason("출석 보상")
                .build();
    }

    @Nested
    @DisplayName("포인트 적립")
    class AccrualTest {

        @Test
        @DisplayName("userId로 포인트를 적립하면 기존 금액에 적립 금액이 더해진다")
        void shouldAccruePointByUserId() {
            // given
            UserPoint userPoint = createUserPoint(1000L);
            ModifyUserPointCommand command = createAccrualCommandWithUserId(500L);

            when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);
            when(updateUserPointPort.update(any(UserPoint.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            UpdateUserPointResult result = modifyUserPointService.modify(command);

            // then
            assertThat(result.amount()).isEqualTo(1500L);
            assertThat(result.userId()).isEqualTo(USER_ID);

            verify(getUserPointUseCase).getUserPoint(USER_ID);
            verify(updateUserPointPort).update(any(UserPoint.class));
            verify(saveUserPointHistoryPort).save(any(UserPointHistory.class));
        }

        @Test
        @DisplayName("적립 시 일반 조회를 사용하고 잠금 조회는 호출하지 않는다")
        void shouldNotUseFindByUserIdForUpdateOnAccrual() {
            // given
            UserPoint userPoint = createUserPoint(1000L);
            ModifyUserPointCommand command = createAccrualCommandWithUserId(500L);

            when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);
            when(updateUserPointPort.update(any(UserPoint.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            modifyUserPointService.modify(command);

            // then
            verify(getUserPointUseCase).getUserPoint(USER_ID);
            verifyNoInteractions(findUserPointPort);
        }

        @Test
        @DisplayName("userKey로 포인트를 적립하면 기존 금액에 적립 금액이 더해진다")
        void shouldAccruePointByUserKey() {
            // given
            UserPoint userPoint = createUserPoint(1000L);
            ModifyUserPointCommand command = createAccrualCommandWithUserKey(300L);

            when(getUserPointUseCase.getUserPoint(USER_KEY)).thenReturn(userPoint);
            when(updateUserPointPort.update(any(UserPoint.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            UpdateUserPointResult result = modifyUserPointService.modify(command);

            // then
            assertThat(result.amount()).isEqualTo(1300L);

            verify(getUserPointUseCase).getUserPoint(USER_KEY);
            verify(getUserPointUseCase, never()).getUserPoint(USER_ID);
        }
    }

    @Nested
    @DisplayName("포인트 차감")
    class DeductionTest {

        @Test
        @DisplayName("포인트를 차감하면 기존 금액에서 차감 금액이 빠진다")
        void shouldDeductPoint() {
            // given
            UserPoint userPoint = createUserPoint(1000L);
            ModifyUserPointCommand command = createDeductionCommandWithUserId(300L);

            when(findUserPointPort.findByUserIdForUpdate(USER_ID)).thenReturn(Optional.of(userPoint));
            when(updateUserPointPort.update(any(UserPoint.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            UpdateUserPointResult result = modifyUserPointService.modify(command);

            // then
            assertThat(result.amount()).isEqualTo(700L);
        }

        @Test
        @DisplayName("차감 시 잠금 조회(findByUserIdForUpdate)를 사용하고 일반 조회는 호출하지 않는다")
        void shouldUseFindByUserIdForUpdateOnDeduction() {
            // given
            UserPoint userPoint = createUserPoint(1000L);
            ModifyUserPointCommand command = createDeductionCommandWithUserId(300L);

            when(findUserPointPort.findByUserIdForUpdate(USER_ID)).thenReturn(Optional.of(userPoint));
            when(updateUserPointPort.update(any(UserPoint.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            modifyUserPointService.modify(command);

            // then
            verify(findUserPointPort).findByUserIdForUpdate(USER_ID);
            verifyNoInteractions(getUserPointUseCase);
        }

        @Test
        @DisplayName("차감 시 잠금 조회 결과가 없으면 UserPointNotFoundException이 발생한다")
        void shouldThrowWhenUserPointNotFoundOnDeductionWithLock() {
            // given
            ModifyUserPointCommand command = createDeductionCommandWithUserId(300L);

            when(findUserPointPort.findByUserIdForUpdate(USER_ID)).thenReturn(Optional.empty());

            // expect
            assertThatThrownBy(() -> modifyUserPointService.modify(command))
                    .isInstanceOf(UserPointNotFoundException.class);

            verify(findUserPointPort).findByUserIdForUpdate(USER_ID);
            verifyNoInteractions(getUserPointUseCase);
            verify(updateUserPointPort, never()).update(any());
            verify(saveUserPointHistoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("포인트 이력 저장")
    class HistoryTest {

        @Test
        @DisplayName("적립 시 이력의 금액은 양수로 저장된다")
        void shouldSaveAccrualHistoryWithPositiveAmount() {
            // given
            UserPoint userPoint = createUserPoint(1000L);
            ModifyUserPointCommand command = createAccrualCommandWithUserId(500L);

            when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);
            when(updateUserPointPort.update(any(UserPoint.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            modifyUserPointService.modify(command);

            // then
            ArgumentCaptor<UserPointHistory> captor = ArgumentCaptor.forClass(UserPointHistory.class);
            verify(saveUserPointHistoryPort).save(captor.capture());
            UserPointHistory capturedHistory = captor.getValue();

            assertThat(capturedHistory.getAmount()).isEqualTo(500L);
            assertThat(capturedHistory.getIsReflected()).isTrue();
            assertThat(capturedHistory.getSourceType()).isEqualTo(UserPointSourceType.ORDER);
        }

        @Test
        @DisplayName("차감 시 이력의 금액은 음수로 저장된다")
        void shouldSaveDeductionHistoryWithNegativeAmount() {
            // given
            UserPoint userPoint = createUserPoint(1000L);
            ModifyUserPointCommand command = createDeductionCommandWithUserId(300L);

            when(findUserPointPort.findByUserIdForUpdate(USER_ID)).thenReturn(Optional.of(userPoint));
            when(updateUserPointPort.update(any(UserPoint.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            modifyUserPointService.modify(command);

            // then
            ArgumentCaptor<UserPointHistory> captor = ArgumentCaptor.forClass(UserPointHistory.class);
            verify(saveUserPointHistoryPort).save(captor.capture());
            UserPointHistory capturedHistory = captor.getValue();

            assertThat(capturedHistory.getAmount()).isEqualTo(-300L);
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionTest {

        @Test
        @DisplayName("userId로 조회 시 회원 포인트가 존재하지 않으면 UserNotFoundException이 발생한다")
        void shouldThrowWhenUserPointNotFoundByUserId() {
            // given
            ModifyUserPointCommand command = createAccrualCommandWithUserId(500L);

            when(getUserPointUseCase.getUserPoint(USER_ID))
                    .thenThrow(new UserNotFoundException("회원 포인트 정보를 찾을 수 없습니다. userId=" + USER_ID));

            // expect
            assertThatThrownBy(() -> modifyUserPointService.modify(command))
                    .isInstanceOf(UserNotFoundException.class);

            verify(updateUserPointPort, never()).update(any());
            verify(saveUserPointHistoryPort, never()).save(any());
        }

        @Test
        @DisplayName("userKey로 조회 시 회원 포인트가 존재하지 않으면 UserNotFoundException이 발생한다")
        void shouldThrowWhenUserPointNotFoundByUserKey() {
            // given
            ModifyUserPointCommand command = createAccrualCommandWithUserKey(500L);

            when(getUserPointUseCase.getUserPoint(USER_KEY))
                    .thenThrow(new UserNotFoundException("회원 포인트 정보를 찾을 수 없습니다. userKey=" + USER_KEY));

            // expect
            assertThatThrownBy(() -> modifyUserPointService.modify(command))
                    .isInstanceOf(UserNotFoundException.class);

            verify(updateUserPointPort, never()).update(any());
            verify(saveUserPointHistoryPort, never()).save(any());
        }

        @Test
        @DisplayName("포인트 이력 저장 시 중복이면 DuplicateUserPointHistoryException이 발생한다")
        void shouldThrowWhenDuplicatePointHistory() {
            // given
            UserPoint userPoint = createUserPoint(1000L);
            ModifyUserPointCommand command = createAccrualCommandWithUserId(500L);

            when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);
            when(updateUserPointPort.update(any(UserPoint.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                    .thenThrow(new DuplicateUserPointHistoryException(
                            USER_ID, UserPointSourceType.ORDER, 100L, "상품 구매 적립"));

            // expect
            assertThatThrownBy(() -> modifyUserPointService.modify(command))
                    .isInstanceOf(DuplicateUserPointHistoryException.class)
                    .hasMessageContaining("이미 처리된 포인트 변경입니다");

            verify(getUserPointUseCase).getUserPoint(USER_ID);
            verify(updateUserPointPort).update(any(UserPoint.class));
            verify(saveUserPointHistoryPort).save(any(UserPointHistory.class));
        }
    }
}
