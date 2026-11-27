package com.personal.marketnote.user.adapter.out.oauth;

import com.personal.marketnote.common.adapter.out.VendorAdapter;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.ValueMasker;
import com.personal.marketnote.user.port.out.oauth.Oauth2AccountUnlinkPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.service.exception.UnlinkOauth2AccountFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static com.personal.marketnote.user.service.exception.ExceptionMessage.UNLINK_OAUTH2_ACCOUNT_FAILED_EXCEPTION_MESSAGE;

@VendorAdapter
@Slf4j
public class Oauth2AccountClient implements Oauth2AccountUnlinkPort {
    private static final String KAKAO_UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";
    private static final String GOOGLE_REVOKE_URL = "https://oauth2.googleapis.com/revoke";
    private static final String PROVIDER_KAKAO = "kakao";
    private static final String PROVIDER_GOOGLE = "google";
    private static final String TAG_PROVIDER = "provider";
    private static final String TAG_OIDC_ID = "oidcId";
    private static final String TAG_ACCESS_TOKEN = "accessToken";
    private static final String OPERATION_UNLINK_ACCOUNT = "unlinkAccount";

    private final RestClient restClient;
    private final String kakaoAdminKey;
    private final ExternalCallLogger externalCallLogger;

    public Oauth2AccountClient(
        RestClient.Builder restClientBuilder,
        @Value("${oauth2.kakao.admin-key}") String kakaoAdminKey,
        ExternalCallLogger externalCallLogger
    ) {
        this.restClient = restClientBuilder.build();
        this.kakaoAdminKey = kakaoAdminKey;
        this.externalCallLogger = externalCallLogger;
    }

    @Override
    public void unlinkAccount(AuthVendor vendor, String credential) throws UnlinkOauth2AccountFailedException {
        if (vendor.isMe(AuthVendor.KAKAO)) {
            unlinkKakaoAccount(credential);
            return;
        }

        if (vendor.isMe(AuthVendor.GOOGLE)) {
            unlinkGoogleAccount(credential);
            return;
        }

        log.debug("{} 벤더에 대한 연결 해제 로직이 구현되지 않았습니다.", vendor.name());
    }

    private void unlinkKakaoAccount(String oidcId) throws UnlinkOauth2AccountFailedException {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("target_id_type", "user_id");
        form.add("target_id", oidcId);

        Map<String, String> tags = Map.of(
            TAG_PROVIDER, PROVIDER_KAKAO,
            TAG_OIDC_ID, ValueMasker.mask(oidcId)
        );

        try {
            externalCallLogger.recordVoid(
                ExternalSystem.KAKAO_OAUTH, OPERATION_UNLINK_ACCOUNT, tags,
                () -> {
                    ResponseEntity<String> response = restClient.post()
                        .uri(KAKAO_UNLINK_URL)
                        .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoAdminKey)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .toEntity(String.class);

                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new UnlinkOauth2AccountFailedRuntimeException(
                            String.format(UNLINK_OAUTH2_ACCOUNT_FAILED_EXCEPTION_MESSAGE, AuthVendor.KAKAO.name())
                        );
                    }
                }
            );
        } catch (UnlinkOauth2AccountFailedRuntimeException e) {
            throw new UnlinkOauth2AccountFailedException(e.getMessage(), e);
        }
    }

    private void unlinkGoogleAccount(String accessToken) throws UnlinkOauth2AccountFailedException {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", accessToken);

        Map<String, String> tags = Map.of(
            TAG_PROVIDER, PROVIDER_GOOGLE,
            TAG_ACCESS_TOKEN, ValueMasker.mask(accessToken)
        );

        try {
            externalCallLogger.recordVoid(
                ExternalSystem.GOOGLE_OAUTH, OPERATION_UNLINK_ACCOUNT, tags,
                () -> {
                    ResponseEntity<String> response = restClient.post()
                        .uri(GOOGLE_REVOKE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .toEntity(String.class);

                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new UnlinkOauth2AccountFailedRuntimeException(
                            String.format(UNLINK_OAUTH2_ACCOUNT_FAILED_EXCEPTION_MESSAGE, AuthVendor.GOOGLE.name())
                        );
                    }
                }
            );
        } catch (UnlinkOauth2AccountFailedRuntimeException e) {
            throw new UnlinkOauth2AccountFailedException(e.getMessage(), e);
        }
    }

    /**
     * 람다 내부에서 체크드 {@link UnlinkOauth2AccountFailedException}을 던질 수 없어 사용하는 내부 wrapper.
     * {@link ExternalCallLogger}가 RuntimeException만 캐치/rethrow하므로 ERROR 로그/Timer 기록을 위해
     * 람다 내부에서 본 예외를 throw하고 외부에서 체크드 예외로 다시 변환한다.
     */
    private static class UnlinkOauth2AccountFailedRuntimeException extends RuntimeException {
        UnlinkOauth2AccountFailedRuntimeException(String message) {
            super(message);
        }
    }
}
