package com.personal.marketnote.user.security.token.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.domain.exception.token.InvalidAccessTokenException;
import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.common.domain.exception.token.UnsupportedCodeException;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2AuthenticationInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2UserInfo;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class JsonTokenSupport implements TokenSupport {
    private final ObjectMapper objectMapper;

    @Override
    public GrantedTokenInfo grantToken(String code, String redirectUri, AuthVendor authVendor) throws UnsupportedCodeException {
        String trimmedCode = code.substring(0, 20);
        String token = URLEncoder.encode(String.format("{ \"id\": \"%s\", \"name\": \"안유진\", \"profileImageUrl\": \"https://dimg.donga.com/wps/NEWS/IMAGE/2020/12/09/104244741.2.jpg\"}", trimmedCode), StandardCharsets.UTF_8);
        return GrantedTokenInfo.builder()
                .accessToken(token)
                .refreshToken(token)
                .id(trimmedCode)
                .authVendor(AuthVendor.NATIVE)
                .build();
    }

    @Override
    public OAuth2AuthenticationInfo authenticate(String accessToken) throws InvalidAccessTokenException {
        try {
            Map<String, String> result = this.objectMapper.readValue(accessToken, new TypeReference<>() {
            });

            return OAuth2AuthenticationInfo.builder()
                    .id(result.get("id"))
                    .build();
        } catch (JsonProcessingException e) {
            throw new InvalidAccessTokenException(e.getMessage(), e);
        }
    }

    @Override
    public OAuth2UserInfo retrieveUserInfo(String accessToken) throws InvalidAccessTokenException {
        try {
            return this.objectMapper.readValue(URLDecoder.decode(accessToken, StandardCharsets.UTF_8), OAuth2UserInfo.class);
        } catch (JsonProcessingException e) {
            throw new InvalidAccessTokenException("Invalid Access token", e);
        }
    }

    @Override
    public GrantedTokenInfo refreshToken(String refreshToken) throws InvalidRefreshTokenException {
        return GrantedTokenInfo.builder()
                .accessToken(refreshToken)
                .refreshToken(refreshToken)
                .id(authenticate(refreshToken).id())
                .authVendor(AuthVendor.NATIVE)
                .build();
    }
}
