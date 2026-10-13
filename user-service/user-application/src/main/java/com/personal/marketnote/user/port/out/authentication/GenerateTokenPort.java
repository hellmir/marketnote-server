package com.personal.marketnote.user.port.out.authentication;

import com.personal.marketnote.user.security.token.vendor.AuthVendor;

import java.util.List;

public interface GenerateTokenPort {
    String generateAccessToken(String subject, Long userId, List<String> roleIds, AuthVendor authVendor);

    String generateRefreshToken(String subject, Long userId, List<String> roleIds, AuthVendor authVendor);
}
