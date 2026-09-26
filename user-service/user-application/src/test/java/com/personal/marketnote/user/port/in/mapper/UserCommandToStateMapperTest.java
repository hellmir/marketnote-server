package com.personal.marketnote.user.port.in.mapper;

import com.personal.marketnote.user.domain.user.UserCreateState;
import com.personal.marketnote.user.port.in.command.SignUpCommand;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserCommandToStateMapperTest {

    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Test
    @DisplayName("닉네임이 정상 값이면 그대로 매핑한다")
    void mapToState_validNickname_keepsValue() {
        // given
        SignUpCommand command = SignUpCommand.builder()
                .email("user@test.com")
                .password("password1!")
                .nickname("tester")
                .build();
        when(passwordEncoder.encode("password1!")).thenReturn("encoded");

        // when
        UserCreateState state = UserCommandToStateMapper.mapToState(
                command, AuthVendor.NATIVE, null, List.of(), "REF", passwordEncoder
        );

        // then
        assertThat(state.getNickname()).isEqualTo("tester");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("닉네임이 null 또는 빈 문자열이면 null로 정규화한다")
    void mapToState_blankNickname_normalizedToNull(String nickname) {
        // given
        SignUpCommand command = SignUpCommand.builder()
                .email("user@test.com")
                .password("password1!")
                .nickname(nickname)
                .build();
        when(passwordEncoder.encode("password1!")).thenReturn("encoded");

        // when
        UserCreateState state = UserCommandToStateMapper.mapToState(
                command, AuthVendor.NATIVE, null, List.of(), "REF", passwordEncoder
        );

        // then
        assertThat(state.getNickname()).isNull();
    }
}
