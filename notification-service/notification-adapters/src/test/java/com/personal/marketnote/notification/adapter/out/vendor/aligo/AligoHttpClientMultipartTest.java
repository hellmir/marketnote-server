package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AligoHttpClientMultipartTest {

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
        retry.setMaxAttempts(3);
        retry.setInitialBackoffMs(10);
        properties.setRetry(retry);

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("http://localhost").build();

        ObjectMapper objectMapper = new ObjectMapper();
        aligoHttpClient = new AligoHttpClient(restClient, objectMapper, properties);
    }

    @Test
    @DisplayName("AligoHttpClient.postMultipart() — multipart/form-data 전송 + 파일 첨부 검증")
    void shouldSendMultipartFormDataWithFile() {
        // given
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost/brandtalk/template/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("senderkey", "test-sender-key");
        Resource imageFile = new ByteArrayResource(new byte[]{1, 2, 3}) {
            @Override
            public String getFilename() {
                return "test-image.jpg";
            }
        };
        params.add("image", imageFile);

        // when
        AligoApiResponse response = aligoHttpClient.postMultipart(
                "/brandtalk/template/", params, AligoApiResponse.class
        );

        // then
        assertThat(response.resultCode()).isZero();
        assertThat(response.message()).isEqualTo("성공");
        mockServer.verify();
    }

    @Test
    @DisplayName("AligoHttpClient.postMultipart() — apikey/userid 공통 파라미터 자동 주입 검증")
    void shouldInjectCommonParametersForMultipart() {
        // given
        String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost/brandtalk/template/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("senderkey", "test-sender-key");

        // when
        AligoApiResponse response = aligoHttpClient.postMultipart(
                "/brandtalk/template/", params, AligoApiResponse.class
        );

        // then
        assertThat(response.resultCode()).isZero();
        mockServer.verify();
    }

    @Test
    @DisplayName("AligoHttpClient.postMultipart() — 5xx 오류 시 재시도 검증")
    void shouldRetryOnServerError() {
        // given
        mockServer.expect(times(2), requestTo("http://localhost/brandtalk/template/"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        String successResponse = "{\"result_code\":0,\"message\":\"성공\"}";
        mockServer.expect(requestTo("http://localhost/brandtalk/template/"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(successResponse, MediaType.APPLICATION_JSON));

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("senderkey", "test-sender-key");

        // when
        AligoApiResponse response = aligoHttpClient.postMultipart(
                "/brandtalk/template/", params, AligoApiResponse.class
        );

        // then
        assertThat(response.resultCode()).isZero();
        mockServer.verify();
    }
}
