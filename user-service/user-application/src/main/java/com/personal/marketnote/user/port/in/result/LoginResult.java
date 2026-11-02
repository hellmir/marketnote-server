package com.personal.marketnote.user.port.in.result;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@ToString
public class LoginResult {
    private final boolean isNewUser;
    private final String accessToken;
    private final String refreshToken;
    private final String nickname;

    public static LoginResult of(boolean isNewUser, @NotNull String accessToken, String refreshToken, String nickname) {
        return new LoginResult(isNewUser, accessToken, refreshToken, nickname);
    }
}
