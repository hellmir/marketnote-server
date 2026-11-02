package com.personal.marketnote.user.adapter.out.token;

import com.personal.marketnote.user.port.out.authentication.GenerateTokenPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.utility.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GenerateTokenAdapter implements GenerateTokenPort {
    private final JwtUtil jwtUtil;

    @Override
    public String generateAccessToken(String subject, Long userId, List<String> roleIds, AuthVendor authVendor) {
        return jwtUtil.generateAccessToken(subject, userId, roleIds, authVendor);
    }

    @Override
    public String generateRefreshToken(String subject, Long userId, List<String> roleIds, AuthVendor authVendor) {
        return jwtUtil.generateRefreshToken(subject, userId, roleIds, authVendor);
    }
}
