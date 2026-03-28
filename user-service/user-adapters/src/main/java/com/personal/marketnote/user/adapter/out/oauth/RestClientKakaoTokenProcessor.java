package com.personal.marketnote.user.adapter.out.oauth;

import com.personal.marketnote.common.adapter.out.VendorAdapter;
import com.personal.marketnote.common.domain.exception.token.InvalidAccessTokenException;
import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.common.domain.exception.token.UnsupportedCodeException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2AuthenticationInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2UserInfo;
import com.personal.marketnote.user.security.token.dto.external.OAuth2GrantedToken;
import com.personal.marketnote.user.security.token.support.TokenProcessor;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Base64;

import static com.personal.marketnote.common.security.token.utility.TokenConstant.AUTHENTICATION_SCHEME;
import static com.personal.marketnote.common.security.token.utility.TokenConstant.SUB_CLAIM_KEY;

@VendorAdapter
@Profile("qa.test | prod")
@Slf4j
public class RestClientKakaoTokenProcessor implements TokenProcessor {
    private static final String KAKAO_TOKEN_REQUEST_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_OIDC_USER_INFO_URL = "https://kapi.kakao.com/v1/oidc/userinfo";
    private static final String KAKAO_ME_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String TOKEN_REQUEST_HEADER_KEY_GRANT_TYPE = "grant_type";
    private static final String TOKEN_REQUEST_HEADER_VALUE_GRANT_TYPE = "authorization_code";
    private static final String TOKEN_REQUEST_HEADER_KEY_CLIENT_ID = "client_id";
    private static final String TOKEN_REQUEST_HEADER_KEY_CLIENT_SECRET = "client_secret";
    private static final String TOKEN_REQUEST_HEADER_KEY_REDIRECT_URI = "redirect_uri";
    private static final String TOKEN_REQUEST_HEADER_KEY_CODE = "code";

    private final RestClient restClient;
    private final String kakaoClientId;
    private final String kakaoClientSecret;

    public RestClientKakaoTokenProcessor(
            RestClient.Builder restClientBuilder,
            @Value("${oauth2.kakao.client-id}") String kakaoClientId,
            @Value("${oauth2.kakao.client-secret}") String kakaoClientSecret
    ) {
        this.restClient = restClientBuilder.build();
        this.kakaoClientId = kakaoClientId;
        this.kakaoClientSecret = "empty".equals(kakaoClientSecret)
                ? null
                : kakaoClientSecret;
    }

    @Override
    public GrantedTokenInfo grantToken(String code, String redirectUri) throws UnsupportedCodeException {
        log.debug("redirect uri: {}", redirectUri);

        MultiValueMap<String, String> requestBody = buildTokenRequestBody(code, redirectUri);

        ResponseEntity<OAuth2GrantedToken> responseEntity = restClient.post()
                .uri(KAKAO_TOKEN_REQUEST_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestBody)
                .retrieve()
                .toEntity(OAuth2GrantedToken.class);

        if (responseEntity.getStatusCode().is4xxClientError()) {
            throw new UnsupportedCodeException("Code is not supported.");
        }

        OAuth2GrantedToken responseBody = responseEntity.getBody();

        if (log.isDebugEnabled()) {
            log.debug("Response Body:\n{}", responseBody);
        }

        if (FormatValidator.hasValue(responseBody)) {
            return GrantedTokenInfo.builder()
                    .accessToken(responseBody.getAccessToken())
                    .refreshToken(responseBody.getRefreshToken())
                    .id(extractIdFromIdToken(responseBody.getIdToken()))
                    .authVendor(AuthVendor.KAKAO)
                    .build();
        }

        return GrantedTokenInfo.builder()
                .build();
    }

    private MultiValueMap<String, String> buildTokenRequestBody(String code, String redirectUri) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(TOKEN_REQUEST_HEADER_KEY_GRANT_TYPE, TOKEN_REQUEST_HEADER_VALUE_GRANT_TYPE);
        params.add(TOKEN_REQUEST_HEADER_KEY_CLIENT_ID, kakaoClientId);

        if (FormatValidator.hasValue(kakaoClientSecret)) {
            params.add(TOKEN_REQUEST_HEADER_KEY_CLIENT_SECRET, kakaoClientSecret);
        }

        params.add(TOKEN_REQUEST_HEADER_KEY_REDIRECT_URI, redirectUri);
        params.add(TOKEN_REQUEST_HEADER_KEY_CODE, code);
        params.add("client_secret", kakaoClientSecret);

        return params;
    }

    private String extractIdFromIdToken(String idToken) {
        if (log.isDebugEnabled()) {
            log.debug("idToken={}", idToken);
        }

        String payload = idToken.split("\\.")[1];
        byte[] decoded = Base64.getDecoder().decode(payload);
        char[] chars = new char[decoded.length];

        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) decoded[i];
        }

        return new JSONObject(String.valueOf(chars)).getString(SUB_CLAIM_KEY);
    }

    @Override
    public OAuth2AuthenticationInfo authenticate(String accessToken) throws InvalidAccessTokenException {
        ResponseEntity<String> responseEntity = restClient.get()
                .uri(KAKAO_OIDC_USER_INFO_URL)
                .header(HttpHeaders.AUTHORIZATION, AUTHENTICATION_SCHEME + accessToken)
                .retrieve()
                .toEntity(String.class);

        if (responseEntity.getStatusCode().is4xxClientError()) {
            throw new InvalidAccessTokenException("Access token not valid");
        }

        JSONObject responseBody = new JSONObject(responseEntity.getBody());

        return OAuth2AuthenticationInfo.builder()
                .id(responseBody.getString(SUB_CLAIM_KEY))
                .authVendor(AuthVendor.KAKAO)
                .build();
    }

    @Override
    public OAuth2UserInfo retrieveUserInfo(String accessToken) throws InvalidAccessTokenException {
        ResponseEntity<String> responseEntity = restClient.get()
                .uri(KAKAO_ME_URL)
                .header(HttpHeaders.AUTHORIZATION, AUTHENTICATION_SCHEME + accessToken)
                .retrieve()
                .toEntity(String.class);

        if (responseEntity.getStatusCode().is4xxClientError()) {
            throw new InvalidAccessTokenException("Access Token is not valid");
        }

        JSONObject responseBody = new JSONObject(responseEntity.getBody());
        JSONObject kakaoAccount = responseBody.getJSONObject("kakao_account");

        return OAuth2UserInfo.builder()
                .id(String.valueOf(responseBody.getLong("id")))
                .name(kakaoAccount.isNull("name") ? null : kakaoAccount.getString("name"))
                .build();
    }

    @Override
    public GrantedTokenInfo refreshToken(String refreshToken) throws InvalidRefreshTokenException {
        MultiValueMap<String, String> bodyParams = new LinkedMultiValueMap<>();
        bodyParams.add("grant_type", "refresh_token");
        bodyParams.add("client_id", kakaoClientId);
        bodyParams.add("refresh_token", refreshToken);
        bodyParams.add("client_secret", kakaoClientSecret);

        ResponseEntity<OAuth2GrantedToken> responseEntity = restClient.post()
                .uri(KAKAO_TOKEN_REQUEST_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(bodyParams)
                .retrieve()
                .toEntity(OAuth2GrantedToken.class);

        if (responseEntity.getStatusCode().is4xxClientError()) {
            throw new InvalidRefreshTokenException("Refresh token not valid");
        }

        OAuth2GrantedToken responseBody = responseEntity.getBody();

        return GrantedTokenInfo.builder()
                .accessToken(responseBody.getAccessToken())
                .refreshToken(responseBody.getRefreshToken())
                .id(extractIdFromIdToken(responseBody.getIdToken()))
                .authVendor(AuthVendor.KAKAO)
                .build();
    }

    @Override
    public AuthVendor getAuthVendor() {
        return AuthVendor.KAKAO;
    }
}
