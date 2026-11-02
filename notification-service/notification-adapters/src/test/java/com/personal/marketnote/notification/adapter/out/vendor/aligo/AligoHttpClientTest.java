package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import com.personal.marketnote.notification.domain.aligo.AligoAuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AligoHttpClientTest {

    private AligoHttpClient aligoHttpClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        AligoProperties properties = new AligoProperties();
        properties.setBaseUrl("http://localhost");
        properties.setApiKey("test-api-key");
        properties.setUserId("test-user-id");
        properties.setTimeout(5000);
        properties.setTestMode(true);
        AligoProperties.Retry retry = new AligoProperties.Retry();
        retry.setMaxAttempts(1);
        retry.setInitialBackoffMs(10);
        properties.setRetry(retry);

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("http://localhost").build();

        ObjectMapper objectMapper = new ObjectMapper();
        aligoHttpClient = new AligoHttpClient(restClient, objectMapper, properties);
    }

    @Test
    @DisplayName("AligoHttpClient가 apikey/userid 공통 파라미터를 자동 주입한다")
    void shouldInjectCommonParameters() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost/send/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");

        AligoApiResponse response = aligoHttpClient.post("/send/", params, AligoApiResponse.class);

        assertThat(response.resultCode()).isZero();
        assertThat(response.message()).isEqualTo("성공");
        mockServer.verify();
    }

    @Test
    @DisplayName("AligoHttpClient가 form-urlencoded POST 본문을 올바르게 생성한다")
    void shouldSendFormEncodedPostBody() {
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost/send/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("receiver", "01012345678");
        params.add("msg", "테스트 메시지");

        AligoApiResponse response = aligoHttpClient.post("/send/", params, AligoApiResponse.class);

        assertThat(response.resultCode()).isZero();
        mockServer.verify();
    }

    @Test
    @DisplayName("알리고 API가 에러 코드를 반환하면 도메인 예외를 던진다")
    void shouldThrowDomainExceptionOnApiError() {
        String responseBody = "{\"result_code\":-101,\"message\":\"인증 오류\"}";
        mockServer.expect(requestTo("http://localhost/send/"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        assertThatThrownBy(() -> aligoHttpClient.post("/send/", params, AligoApiResponse.class))
                .isInstanceOf(AligoAuthenticationException.class);
        mockServer.verify();
    }

    @Test
    @DisplayName("응답 파싱 실패 시 AligoApiRequestFailedException으로 래핑한다")
    void shouldWrapParsingErrorAsAligoApiRequestFailedException() {
        String responseBody = "invalid json";
        mockServer.expect(requestTo("http://localhost/send/"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        assertThatThrownBy(() -> aligoHttpClient.post("/send/", params, AligoApiResponse.class))
                .isInstanceOf(AligoApiRequestFailedException.class);
        mockServer.verify();
    }
}
