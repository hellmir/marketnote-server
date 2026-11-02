package com.personal.marketnote.user.adapter.in.web.authentication.response;

import org.springframework.http.HttpHeaders;

public record OAuth2LoginResponse(
        HttpHeaders headers
) {
}
