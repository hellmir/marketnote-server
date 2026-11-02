package com.personal.marketnote.user.adapter.out.token;

import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.user.port.out.authentication.ParseRefreshTokenPort;
import com.personal.marketnote.user.utility.jwt.JwtUtil;
import com.personal.marketnote.user.utility.jwt.claims.TokenClaims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenParserAdapter implements ParseRefreshTokenPort {
    private final JwtUtil jwtUtil;

    @Override
    public Long extractUserId(String refreshToken) {
        try {
            TokenClaims claims = jwtUtil.parseRefreshToken(refreshToken);
            return claims.getUserId();
        } catch (Exception e) {
            throw new InvalidRefreshTokenException(e.getMessage());
        }
    }
}


