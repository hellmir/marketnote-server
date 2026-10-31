package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.port.out.aligo.GetAligoMessageHistoryExternalPort.AligoMessageHistoryResult;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AligoMessageHistoryExternalAdapterTest {

    private AligoMessageHistoryExternalAdapter adapter;
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
        adapter = new AligoMessageHistoryExternalAdapter(aligoHttpClient);
    }

    @Nested
    @DisplayName("getMessageHistory")
    class GetMessageHistory {

        @Test
        @DisplayName("startDate/endDate가 null이면 startdate/enddate 파라미터가 포함되지 않는다")
        void shouldNotIncludeDatesWhenNull() {
            // given
            String responseBody = """
                    {
                        "result_code": 0,
                        "message": "성공",
                        "total": 0,
                        "list": null
                    }
                    """;
            mockServer.expect(requestTo("http://localhost/akv10/history/list/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().string(Matchers.containsString("page=1")))
                    .andExpect(content().string(Matchers.containsString("limit=10")))
                    .andExpect(content().string(Matchers.not(Matchers.containsString("startdate"))))
                    .andExpect(content().string(Matchers.not(Matchers.containsString("enddate"))))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            // when
            AligoMessageHistoryResult result = adapter.getMessageHistory(1, 10, null, null);

            // then
            assertThat(result.totalCount()).isZero();
            assertThat(result.messages()).isEmpty();
            mockServer.verify();
        }

        @Test
        @DisplayName("응답 list가 비어있으면 빈 요약 리스트를 반환한다")
        void shouldReturnEmptySummariesWhenListEmpty() {
            // given
            String responseBody = """
                    {
                        "result_code": 0,
                        "message": "성공",
                        "total": 0,
                        "list": null
                    }
                    """;
            mockServer.expect(requestTo("http://localhost/akv10/history/list/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().string(Matchers.containsString("startdate=20260101")))
                    .andExpect(content().string(Matchers.containsString("enddate=20260131")))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            // when
            AligoMessageHistoryResult result = adapter.getMessageHistory(1, 10, "20260101", "20260131");

            // then
            assertThat(result.totalCount()).isZero();
            assertThat(result.messages()).isEmpty();
            mockServer.verify();
        }
    }
}
