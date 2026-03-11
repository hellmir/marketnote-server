package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import com.personal.marketnote.reward.exception.DuplicateUserPointException;
import com.personal.marketnote.reward.port.in.command.point.RegisterUserPointCommand;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import com.personal.marketnote.reward.port.out.point.SaveUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.SaveUserPointPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserPointUseCaseTest {

    @InjectMocks
    private RegisterUserPointService registerUserPointService;

    @Mock
    private SaveUserPointPort saveUserPointPort;

    @Mock
    private SaveUserPointHistoryPort saveUserPointHistoryPort;

    @Mock
    private FindUserPointPort findUserPointPort;

    private static final Long USER_ID = 1L;
    private static final String USER_KEY = "test-user-key";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 5, 10, 0);

    private RegisterUserPointCommand createCommand() {
        return RegisterUserPointCommand.of(USER_ID, USER_KEY);
    }

    private UserPoint createSavedUserPoint() {
        return UserPoint.from(UserPointSnapshotState.builder()
                .userId(USER_ID)
                .userKey(USER_KEY)
                .amount(0L)
                .addExpectedAmount(0L)
                .expireExpectedAmount(0L)
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build());
    }

    @Test
    @DisplayName("신규 회원 포인트를 등록하면 포인트와 이력이 저장된다")
    void shouldRegisterUserPointAndHistorySuccessfully() {
        // given
        RegisterUserPointCommand command = createCommand();
        UserPoint savedUserPoint = createSavedUserPoint();

        when(findUserPointPort.existsByUserId(USER_ID)).thenReturn(false);
        when(saveUserPointPort.save(any(UserPoint.class))).thenReturn(savedUserPoint);
        when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        registerUserPointService.register(command);

        // then
        verify(findUserPointPort).existsByUserId(USER_ID);
        verify(saveUserPointPort).save(any(UserPoint.class));
        verify(saveUserPointHistoryPort).save(any(UserPointHistory.class));
    }

    @Test
    @DisplayName("신규 회원 포인트 등록 시 초기 금액은 0이다")
    void shouldRegisterWithZeroInitialAmount() {
        // given
        RegisterUserPointCommand command = createCommand();
        UserPoint savedUserPoint = createSavedUserPoint();

        when(findUserPointPort.existsByUserId(USER_ID)).thenReturn(false);
        when(saveUserPointPort.save(any(UserPoint.class))).thenReturn(savedUserPoint);
        when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        registerUserPointService.register(command);

        // then
        ArgumentCaptor<UserPoint> pointCaptor = ArgumentCaptor.forClass(UserPoint.class);
        verify(saveUserPointPort).save(pointCaptor.capture());
        UserPoint capturedPoint = pointCaptor.getValue();

        assertThat(capturedPoint.getUserId()).isEqualTo(USER_ID);
        assertThat(capturedPoint.getAmountValue()).isZero();
        assertThat(capturedPoint.getAddExpectedAmount()).isZero();
        assertThat(capturedPoint.getExpireExpectedAmount()).isZero();
    }

    @Test
    @DisplayName("신규 회원 포인트 등록 시 이력의 출처 유형은 USER이다")
    void shouldSaveHistoryWithUserSourceType() {
        // given
        RegisterUserPointCommand command = createCommand();
        UserPoint savedUserPoint = createSavedUserPoint();

        when(findUserPointPort.existsByUserId(USER_ID)).thenReturn(false);
        when(saveUserPointPort.save(any(UserPoint.class))).thenReturn(savedUserPoint);
        when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        registerUserPointService.register(command);

        // then
        ArgumentCaptor<UserPointHistory> historyCaptor = ArgumentCaptor.forClass(UserPointHistory.class);
        verify(saveUserPointHistoryPort).save(historyCaptor.capture());
        UserPointHistory capturedHistory = historyCaptor.getValue();

        assertThat(capturedHistory.getUserId()).isEqualTo(USER_ID);
        assertThat(capturedHistory.getAmount()).isZero();
        assertThat(capturedHistory.getIsReflected()).isTrue();
    }

    @Test
    @DisplayName("이미 포인트가 존재하는 회원이면 DuplicateUserPointException이 발생한다")
    void shouldThrowWhenUserPointAlreadyExists() {
        // given
        RegisterUserPointCommand command = createCommand();

        when(findUserPointPort.existsByUserId(USER_ID)).thenReturn(true);

        // expect
        assertThatThrownBy(() -> registerUserPointService.register(command))
                .isInstanceOf(DuplicateUserPointException.class);

        verify(saveUserPointPort, never()).save(any());
        verify(saveUserPointHistoryPort, never()).save(any());
    }

    @Test
    @DisplayName("중복 검증 실패 시 포인트 저장과 이력 저장이 수행되지 않는다")
    void shouldNotSaveAnythingWhenDuplicateValidationFails() {
        // given
        RegisterUserPointCommand command = createCommand();

        when(findUserPointPort.existsByUserId(USER_ID)).thenReturn(true);

        // expect
        assertThatThrownBy(() -> registerUserPointService.register(command))
                .isInstanceOf(DuplicateUserPointException.class);

        verifyNoInteractions(saveUserPointPort);
        verifyNoInteractions(saveUserPointHistoryPort);
    }
}
