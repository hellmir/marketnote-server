package com.personal.marketnote.user.utility.jwt.claims;

import com.personal.marketnote.user.utility.jwt.JwtTokenType;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class RefreshTokenClaims {
    private final LocalDateTime expirationAt;
    private final LocalDateTime issuedAt;
    private final JwtTokenType tokenType;
}
