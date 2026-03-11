package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserPointByUserIdUseCaseTest {

    @InjectMocks
    private GetUserPointService getUserPointService;

    @Mock
    private FindUserPointPort findUserPointPort;

    private static final Long USER_ID = 1L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 5, 10, 0);

    private UserPoint createUserPoint() {
        return UserPoint.from(UserPointSnapshotState.builder()
                .userId(USER_ID)
                .amount(1000L)
                .addExpectedAmount(0L)
                .expireExpectedAmount(0L)
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build());
    }

    @Test
    @DisplayName("회원 ID로 포인트 정보가 존재하면 포인트를 반환한다")
    void shouldReturnUserPointWhenFoundByUserId() {
        // given
        UserPoint userPoint = createUserPoint();

        when(findUserPointPort.findByUserId(USER_ID)).thenReturn(Optional.of(userPoint));

        // when
        UserPoint result = getUserPointService.getUserPoint(USER_ID);

        // then
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getAmountValue()).isEqualTo(1000L);
        verify(findUserPointPort).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("회원 ID로 포인트 정보가 존재하지 않으면 UserNotFoundException이 발생한다")
    void shouldThrowWhenUserPointNotFoundByUserId() {
        // given
        when(findUserPointPort.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> getUserPointService.getUserPoint(USER_ID))
                .isInstanceOf(UserNotFoundException.class);

        verify(findUserPointPort).findByUserId(USER_ID);
    }
}
