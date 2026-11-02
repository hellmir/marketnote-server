package com.personal.marketnote.user.adapter.in.web.authentication.request;

import jakarta.servlet.http.HttpServletRequest;

public record OAuth2LoginRequest(
        String authVendor,
        String code,
        String state,
        HttpServletRequest request
) {
}
