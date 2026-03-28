package com.personal.marketnote.user.adapter.out.oauth;

import com.personal.marketnote.common.adapter.out.VendorAdapter;
import com.personal.marketnote.common.domain.exception.token.InvalidAccessTokenException;
import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.common.domain.exception.token.UnsupportedCodeException;
import com.personal.marketnote.user.exception.GoogleOAuth2ResponseParsingException;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2AuthenticationInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2UserInfo;
import com.personal.marketnote.user.security.token.dto.external.OAuth2GrantedToken;
import com.personal.marketnote.user.security.token.support.TokenProcessor;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URI;

import static com.personal.marketnote.common.security.token.utility.TokenConstant.AUTHENTICATION_SCHEME;
import static com.personal.marketnote.common.security.token.utility.TokenConstant.SUB_CLAIM_KEY;

@VendorAdapter
@Profile("qa.test | prod")
@Slf4j
public class RestClientGoogleTokenProcessor implements TokenProcessor {
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";
    private static final String GOOGLE_ME_URL = "https://www.googleapis.com/userinfo/v2/me";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    public RestClientGoogleTokenProcessor(
            RestClient.Builder restClientBuilder,
            @Value("${oauth2.google.client-id}") String clientId,
            @Value("${oauth2.google.client-secret}") String clientSecret
    ) {
        this.restClient = restClientBuilder.build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public GrantedTokenInfo grantToken(String code, String redirectUri) throws UnsupportedCodeException {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("code", code);
        requestBody.add("redirect_uri", redirectUri);
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);

        ResponseEntity<OAuth2GrantedToken> responseEntity = restClient.post()
                .uri(GOOGLE_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestBody)
                .retrieve()
                .toEntity(OAuth2GrantedToken.class);

        if (responseEntity.getStatusCode().is4xxClientError()) {
            throw new UnsupportedCodeException("Code is invalid");
        }

        OAuth2GrantedToken responseBody = responseEntity.getBody();

        return GrantedTokenInfo.builder()
                .accessToken(responseBody.getAccessToken())
                .refreshToken(responseBody.getRefreshToken())
                .id(extractIdFromIdToken(responseBody.getIdToken()))
                .authVendor(AuthVendor.GOOGLE)
                .build();
    }

    private String extractIdFromIdToken(String idToken) {
        if (log.isDebugEnabled()) {
            log.debug("idToken={}", idToken);
        }

        ResponseEntity<String> response = restClient.get()
                .uri(URI.create(GOOGLE_TOKEN_INFO_URL + "?id_token=" + idToken))
                .retrieve()
                .toEntity(String.class);

        return new JSONObject(response.getBody()).getString(SUB_CLAIM_KEY);
    }

    @Override
    public OAuth2AuthenticationInfo authenticate(String accessToken) throws InvalidAccessTokenException {
        ResponseEntity<String> responseEntity = restClient.get()
                .uri(GOOGLE_ME_URL)
                .header(HttpHeaders.AUTHORIZATION, AUTHENTICATION_SCHEME + accessToken)
                .retrieve()
                .toEntity(String.class);

        if (responseEntity.getStatusCode().is4xxClientError()) {
            throw new InvalidAccessTokenException("Access Token is not valid");
        }

        JSONObject responseBody = new JSONObject(responseEntity.getBody());

        return OAuth2AuthenticationInfo.builder()
                .id(responseBody.getString("id"))
                .authVendor(AuthVendor.GOOGLE)
                .build();
    }

    @Override
    public OAuth2UserInfo retrieveUserInfo(String accessToken) throws InvalidAccessTokenException {
        ResponseEntity<String> responseEntity = restClient.get()
                .uri(GOOGLE_USER_INFO_URL)
                .header(HttpHeaders.AUTHORIZATION, AUTHENTICATION_SCHEME + accessToken)
                .retrieve()
                .toEntity(String.class);

        if (responseEntity.getStatusCode().is4xxClientError()) {
            if (log.isDebugEnabled()) {
                log.debug("status code: {}, body:\n{}", responseEntity.getStatusCode(), responseEntity.getBody());
            }
            throw new InvalidAccessTokenException("Access Token is not valid");
        }

        JSONObject jsonObject = new JSONObject(responseEntity.getBody());

        try {
            return OAuth2UserInfo.builder()
                    .id(jsonObject.getString("id"))
                    .name(jsonObject.getString("name"))
                    .build();
        } catch (JSONException e) {
            throw new GoogleOAuth2ResponseParsingException(jsonObject.toString(), e);
        }
    }

    @Override
    public GrantedTokenInfo refreshToken(String refreshToken) throws InvalidRefreshTokenException {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "refresh_token");
        requestBody.add("refresh_token", refreshToken);
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);

        ResponseEntity<OAuth2GrantedToken> responseEntity = restClient.post()
                .uri(GOOGLE_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestBody)
                .retrieve()
                .toEntity(OAuth2GrantedToken.class);

        if (responseEntity.getStatusCode().is4xxClientError()) {
            throw new InvalidRefreshTokenException("Refresh token is not valid");
        }

        OAuth2GrantedToken responseBody = responseEntity.getBody();

        return GrantedTokenInfo.builder()
                .accessToken(responseBody.getAccessToken())
                .refreshToken(responseBody.getRefreshToken())
                .id(extractIdFromIdToken(responseBody.getIdToken()))
                .authVendor(AuthVendor.GOOGLE)
                .build();
    }

    @Override
    public AuthVendor getAuthVendor() {
        return AuthVendor.GOOGLE;
    }
}
