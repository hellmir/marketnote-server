package com.personal.marketnote.user.adapter.out.oauth;

import com.personal.marketnote.common.adapter.out.VendorAdapter;
import com.personal.marketnote.user.port.out.oauth.Oauth2AccountUnlinkPort;
import com.personal.marketnote.user.service.exception.UnlinkOauth2AccountFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import static com.personal.marketnote.user.service.exception.ExceptionMessage.UNLINK_GOOGLE_ACCOUNT_FAILED_EXCEPTION_MESSAGE;
import static com.personal.marketnote.user.service.exception.ExceptionMessage.UNLINK_KAKAO_ACCOUNT_FAILED_EXCEPTION_MESSAGE;

@VendorAdapter
@Slf4j
public class Oauth2AccountClient implements Oauth2AccountUnlinkPort {
    private static final String KAKAO_UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";
    private static final String GOOGLE_REVOKE_URL = "https://oauth2.googleapis.com/revoke";

    private final RestClient restClient;
    private final String kakaoAdminKey;

    public Oauth2AccountClient(
            RestClient.Builder restClientBuilder,
            @Value("${oauth2.kakao.admin-key}") String kakaoAdminKey
    ) {
        this.restClient = restClientBuilder.build();
        this.kakaoAdminKey = kakaoAdminKey;
    }

    @Override
    public void unlinkKakaoAccount(String oidcId) throws UnlinkOauth2AccountFailedException {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("target_id_type", "user_id");
        form.add("target_id", oidcId);

        ResponseEntity<String> response = restClient.post()
                .uri(KAKAO_UNLINK_URL)
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoAdminKey)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toEntity(String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new UnlinkOauth2AccountFailedException(UNLINK_KAKAO_ACCOUNT_FAILED_EXCEPTION_MESSAGE);
        }
    }

    @Override
    public void unlinkGoogleAccount(String accessToken) throws UnlinkOauth2AccountFailedException {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", accessToken);

        ResponseEntity<String> response = restClient.post()
                .uri(GOOGLE_REVOKE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toEntity(String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new UnlinkOauth2AccountFailedException(UNLINK_GOOGLE_ACCOUNT_FAILED_EXCEPTION_MESSAGE);
        }
    }
}
