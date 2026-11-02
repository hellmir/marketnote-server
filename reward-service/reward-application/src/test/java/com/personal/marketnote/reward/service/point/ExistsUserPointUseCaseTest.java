package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExistsUserPointUseCaseTest {

    @InjectMocks
    private GetUserPointService getUserPointService;

    @Mock
    private FindUserPointPort findUserPointPort;

    private static final String USER_KEY = "test-user-key";

    @Test
    @DisplayName("회원 키로 포인트가 존재하면 true를 반환한다")
    void shouldReturnTrueWhenUserPointExists() {
        // given
        when(findUserPointPort.existsByUserKey(USER_KEY)).thenReturn(true);

        // when
        boolean result = getUserPointService.existsUserPoint(USER_KEY);

        // then
        assertThat(result).isTrue();
        verify(findUserPointPort).existsByUserKey(USER_KEY);
    }

    @Test
    @DisplayName("회원 키로 포인트가 존재하지 않으면 false를 반환한다")
    void shouldReturnFalseWhenUserPointNotExists() {
        // given
        when(findUserPointPort.existsByUserKey(USER_KEY)).thenReturn(false);

        // when
        boolean result = getUserPointService.existsUserPoint(USER_KEY);

        // then
        assertThat(result).isFalse();
        verify(findUserPointPort).existsByUserKey(USER_KEY);
    }
}
