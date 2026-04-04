package com.personal.marketnote.user.service.user;

import com.personal.marketnote.user.port.in.result.CheckNicknameResult;
import com.personal.marketnote.user.port.out.profanity.FindProfanityWordPort;
import com.personal.marketnote.user.port.out.user.FindUserPort;
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
class CheckNicknameUseCaseTest {
    @Mock
    private FindUserPort findUserPort;
    @Mock
    private FindProfanityWordPort findProfanityWordPort;

    @InjectMocks
    private CheckNicknameService checkNicknameService;

    @Test
    @DisplayName("사용 가능한 닉네임이면 isDuplicated가 false를 반환한다")
    void checkNickname_whenAvailable_returnsNotDuplicated() {
        // given
        String nickname = "향긋한스윗피";

        when(findUserPort.existsByNickname(nickname)).thenReturn(false);
        when(findProfanityWordPort.containsProfanity(nickname)).thenReturn(false);

        // when
        CheckNicknameResult result = checkNicknameService.checkNickname(nickname);

        // then
        assertThat(result.isDuplicated()).isFalse();
        assertThat(result.containsProfanity()).isFalse();
        verify(findUserPort).existsByNickname(nickname);
        verify(findProfanityWordPort).containsProfanity(nickname);
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임이면 isDuplicated가 true를 반환한다")
    void checkNickname_whenDuplicated_returnsDuplicated() {
        // given
        String nickname = "몬치치";

        when(findUserPort.existsByNickname(nickname)).thenReturn(true);
        when(findProfanityWordPort.containsProfanity(nickname)).thenReturn(false);

        // when
        CheckNicknameResult result = checkNicknameService.checkNickname(nickname);

        // then
        assertThat(result.isDuplicated()).isTrue();
        assertThat(result.containsProfanity()).isFalse();
        verify(findUserPort).existsByNickname(nickname);
        verify(findProfanityWordPort).containsProfanity(nickname);
    }

    @Test
    @DisplayName("닉네임에 금칙어가 포함되면 containsProfanity가 true를 반환한다")
    void checkNickname_whenContainsProfanity_returnsContainsProfanity() {
        // given
        String nickname = "나는바보야";

        when(findUserPort.existsByNickname(nickname)).thenReturn(false);
        when(findProfanityWordPort.containsProfanity(nickname)).thenReturn(true);

        // when
        CheckNicknameResult result = checkNicknameService.checkNickname(nickname);

        // then
        assertThat(result.containsProfanity()).isTrue();
        assertThat(result.isDuplicated()).isFalse();
        verify(findUserPort).existsByNickname(nickname);
        verify(findProfanityWordPort).containsProfanity(nickname);
    }

    @Test
    @DisplayName("닉네임이 중복이면서 금칙어도 포함되면 isDuplicated와 containsProfanity 모두 true를 반환한다")
    void checkNickname_whenDuplicatedAndContainsProfanity_returnsBothTrue() {
        // given
        String nickname = "나는바보야";

        when(findUserPort.existsByNickname(nickname)).thenReturn(true);
        when(findProfanityWordPort.containsProfanity(nickname)).thenReturn(true);

        // when
        CheckNicknameResult result = checkNicknameService.checkNickname(nickname);

        // then
        assertThat(result.isDuplicated()).isTrue();
        assertThat(result.containsProfanity()).isTrue();
        verify(findUserPort).existsByNickname(nickname);
        verify(findProfanityWordPort).containsProfanity(nickname);
    }
}
