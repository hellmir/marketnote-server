package com.personal.marketnote.user.adapter.in.web.authentication.response;

import lombok.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@ToString
public class LoginResponse {
    private final boolean isNewUser;
    private final String accessToken;
    private final String refreshToken;
    private final String username;
}
