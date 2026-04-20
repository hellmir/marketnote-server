package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.UserPointNotFoundException;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingSharedPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModifyPendingSharedPointUseCaseTest {

    private static final UUID SHARER_KEY = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final Long RESOLVED_USER_ID = 42L;

    @InjectMocks
    private ModifyPendingSharedPointService modifyPendingSharedPointService;

    @Mock
    private FindUserPointPort findUserPointPort;

    @Mock
    private ModifyPendingPointUseCase modifyPendingPointUseCase;

    private UserPoint createUserPoint() {
        return UserPoint.from(UserPointSnapshotState.builder()
                .userId(RESOLVED_USER_ID)
                .amount(1000L)
                .addExpectedAmount(0L)
                .expireExpectedAmount(0L)
                .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .build());
    }

    private ModifyPendingSharedPointCommand createCommand(Long buyerId, UserPointChangeType changeType, Long amount) {
        return ModifyPendingSharedPointCommand.builder()
                .buyerId(buyerId)
                .sharerKey(SHARER_KEY)
                .changeType(changeType)
                .amount(amount)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(100L)
                .reason("링크 공유 회원 상품 구매")
                .build();
    }

    @Test
    @DisplayName("sharerKey로 UserPoint를 조회하여 userId를 해소한 후 적립 예정 포인트를 변경한다")
    void shouldResolveUserIdFromSharerKeyAndDelegateToModifyPending() {
        // given
        UserPoint userPoint = createUserPoint();
        when(findUserPointPort.findByUserKey(SHARER_KEY.toString())).thenReturn(Optional.of(userPoint));

        UpdateUserPointResult expectedResult = UpdateUserPointResult.from(userPoint);
        when(modifyPendingPointUseCase.modifyPending(any(ModifyPendingPointCommand.class))).thenReturn(expectedResult);

        ModifyPendingSharedPointCommand command = createCommand(999L, UserPointChangeType.ACCRUAL, 500L);

        // when
        UpdateUserPointResult result = modifyPendingSharedPointService.modifyPending(command);

        // then
        assertThat(result.userId()).isEqualTo(RESOLVED_USER_ID);

        ArgumentCaptor<ModifyPendingPointCommand> captor = ArgumentCaptor.forClass(ModifyPendingPointCommand.class);
        verify(modifyPendingPointUseCase).modifyPending(captor.capture());

        ModifyPendingPointCommand delegatedCommand = captor.getValue();
        assertThat(delegatedCommand.userId()).isEqualTo(RESOLVED_USER_ID);
        assertThat(delegatedCommand.changeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        assertThat(delegatedCommand.amount()).isEqualTo(500L);
        assertThat(delegatedCommand.sourceType()).isEqualTo(UserPointSourceType.ORDER);
        assertThat(delegatedCommand.sourceId()).isEqualTo(100L);
        assertThat(delegatedCommand.reason()).isEqualTo("링크 공유 회원 상품 구매");
    }

    @Test
    @DisplayName("sharerKey에 해당하는 UserPoint가 없으면 UserPointNotFoundException이 발생한다")
    void shouldThrowWhenUserPointNotFoundBySharerKey() {
        // given
        when(findUserPointPort.findByUserKey(SHARER_KEY.toString())).thenReturn(Optional.empty());

        ModifyPendingSharedPointCommand command = createCommand(999L, UserPointChangeType.ACCRUAL, 500L);

        // expect
        assertThatThrownBy(() -> modifyPendingSharedPointService.modifyPending(command))
                .isInstanceOf(UserPointNotFoundException.class);

        verify(modifyPendingPointUseCase, never()).modifyPending(any());
    }

    @Test
    @DisplayName("구매자와 공유자가 동일인이면 적립 예정 포인트를 변경하지 않고 null을 반환한다")
    void shouldReturnNullWhenBuyerAndSharerAreSamePerson() {
        // given
        UserPoint userPoint = createUserPoint();
        when(findUserPointPort.findByUserKey(SHARER_KEY.toString())).thenReturn(Optional.of(userPoint));

        ModifyPendingSharedPointCommand command = createCommand(RESOLVED_USER_ID, UserPointChangeType.ACCRUAL, 500L);

        // when
        UpdateUserPointResult result = modifyPendingSharedPointService.modifyPending(command);

        // then
        assertThat(result).isNull();
        verify(modifyPendingPointUseCase, never()).modifyPending(any());
    }

    @Test
    @DisplayName("buyerId가 null이면 동일인 검증을 건너뛰고 적립 예정 포인트를 변경한다")
    void shouldProceedWhenBuyerIdIsNull() {
        // given
        UserPoint userPoint = createUserPoint();
        when(findUserPointPort.findByUserKey(SHARER_KEY.toString())).thenReturn(Optional.of(userPoint));

        UpdateUserPointResult expectedResult = UpdateUserPointResult.from(userPoint);
        when(modifyPendingPointUseCase.modifyPending(any(ModifyPendingPointCommand.class))).thenReturn(expectedResult);

        ModifyPendingSharedPointCommand command = createCommand(null, UserPointChangeType.ACCRUAL, 500L);

        // when
        UpdateUserPointResult result = modifyPendingSharedPointService.modifyPending(command);

        // then
        assertThat(result.userId()).isEqualTo(RESOLVED_USER_ID);
        verify(modifyPendingPointUseCase).modifyPending(any(ModifyPendingPointCommand.class));
    }

    @Test
    @DisplayName("차감 타입일 때 위임 커맨드의 changeType이 DEDUCTION이다")
    void shouldDelegateDeductionChangeType() {
        // given
        UserPoint userPoint = createUserPoint();
        when(findUserPointPort.findByUserKey(SHARER_KEY.toString())).thenReturn(Optional.of(userPoint));

        UpdateUserPointResult expectedResult = UpdateUserPointResult.from(userPoint);
        when(modifyPendingPointUseCase.modifyPending(any(ModifyPendingPointCommand.class))).thenReturn(expectedResult);

        ModifyPendingSharedPointCommand command = createCommand(999L, UserPointChangeType.DEDUCTION, 300L);

        // when
        modifyPendingSharedPointService.modifyPending(command);

        // then
        ArgumentCaptor<ModifyPendingPointCommand> captor = ArgumentCaptor.forClass(ModifyPendingPointCommand.class);
        verify(modifyPendingPointUseCase).modifyPending(captor.capture());

        ModifyPendingPointCommand delegatedCommand = captor.getValue();
        assertThat(delegatedCommand.changeType()).isEqualTo(UserPointChangeType.DEDUCTION);
        assertThat(delegatedCommand.amount()).isEqualTo(300L);
    }
}
