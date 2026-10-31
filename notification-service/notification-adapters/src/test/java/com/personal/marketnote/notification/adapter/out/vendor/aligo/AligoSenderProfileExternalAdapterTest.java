package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AligoSenderProfileExternalAdapterTest {

    private AligoSenderProfileExternalAdapter adapter;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        AligoProperties properties = new AligoProperties();
        properties.setBaseUrl("http://localhost");
        properties.setApiKey("test-api-key");
        properties.setUserId("test-user-id");
        properties.setTestMode(true);
        AligoProperties.Retry retry = new AligoProperties.Retry();
        retry.setMaxAttempts(1);
        retry.setInitialBackoffMs(10);
        properties.setRetry(retry);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("http://localhost").build();

        AligoHttpClient aligoHttpClient = new AligoHttpClient(restClient, objectMapper, properties);
        adapter = new AligoSenderProfileExternalAdapter(aligoHttpClient);
    }

    @Nested
    @DisplayName("requestAuthentication")
    class RequestAuthentication {

        @Test
        @DisplayName("인증 요청 응답에서 senderkey 필드를 추출한다")
        void shouldExtractSenderKey() {
            // given
            String responseBody = """
                    {
                        "result_code": 0,
                        "message": "성공",
                        "senderkey": "SENDER-KEY-001"
                    }
                    """;
            mockServer.expect(requestTo("http://localhost/akv10/profile/auth/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(Matchers.containsString("plusid=%40plus")))
                    .andExpect(content().string(Matchers.containsString("phonenumber=01011112222")))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            // when
            String senderKey = adapter.requestAuthentication("@plus", "01011112222");

            // then
            assertThat(senderKey).isEqualTo("SENDER-KEY-001");
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("registerProfile")
    class RegisterProfile {

        @Test
        @DisplayName("senderkey/categorycode를 /akv10/profile/add/로 전송한다")
        void shouldSendSenderKeyAndCategoryCode() {
            // given
            String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
            mockServer.expect(requestTo("http://localhost/akv10/profile/add/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().string(Matchers.containsString("senderkey=SK")))
                    .andExpect(content().string(Matchers.containsString("categorycode=001001")))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            // when
            adapter.registerProfile("SK", "001001");

            // then
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getProfileList")
    class GetProfileList {

        @Test
        @DisplayName("응답 list의 senderProfileStatus 문자열이 enum으로 변환된다")
        void shouldConvertStatusToEnum() {
            // given
            String responseBody = """
                    {
                        "result_code": 0,
                        "message": "성공",
                        "list": [
                            {
                                "senderkey": "SK1",
                                "plusid": "@plus",
                                "name": "프로필1",
                                "categorycode": "001001",
                                "profileInspectionStatus": "A",
                                "senderProfileStatus": "A",
                                "alim_use_yn": "Y",
                                "uuid": "uuid-1"
                            },
                            {
                                "senderkey": "SK2",
                                "plusid": "@plus2",
                                "name": "프로필2",
                                "categorycode": "001002",
                                "profileInspectionStatus": "A",
                                "senderProfileStatus": "S",
                                "alim_use_yn": "N",
                                "uuid": "uuid-2"
                            }
                        ]
                    }
                    """;
            mockServer.expect(requestTo("http://localhost/akv10/profile/list/"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            // when
            List<AligoSenderProfile> profiles = adapter.getProfileList();

            // then
            assertThat(profiles).hasSize(2);
            assertThat(profiles.get(0).getSenderProfileStatus()).isEqualTo(AligoSenderProfileStatus.A);
            assertThat(profiles.get(1).getSenderProfileStatus()).isEqualTo(AligoSenderProfileStatus.S);
            mockServer.verify();
        }
    }
}
