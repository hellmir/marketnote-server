package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoAuthenticationException;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoCategoryTreeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AligoCategoryExternalAdapterTest {

    private AligoCategoryExternalAdapter adapter;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        AligoProperties properties = new AligoProperties();
        properties.setBaseUrl("http://localhost");
        properties.setApiKey("test-api-key");
        properties.setUserId("test-user-id");
        properties.setTestMode(true);
        properties.getRetry().setMaxAttempts(1);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.baseUrl(properties.getBaseUrl()).build();
        AligoHttpClient aligoHttpClient = new AligoHttpClient(restClient, objectMapper, properties);
        adapter = new AligoCategoryExternalAdapter(aligoHttpClient);
    }

    @Test
    @DisplayName("카테고리 API 응답을 3단계 트리로 변환한다")
    void shouldBuildCategoryTreeFromApiResponse() {
        // given
        String responseBody = """
                {
                    "code": 0,
                    "message": "정상적으로 호출하였습니다.",
                    "data": {
                        "firstBusinessType": [
                            {"parentCode": "", "code": "001", "name": "건강"}
                        ],
                        "secondBusinessType": [
                            {"parentCode": "001", "code": "001001", "name": "병원"}
                        ],
                        "thirdBusinessType": [
                            {"parentCode": "001001", "code": "00100010001", "name": "종합병원"}
                        ]
                    }
                }
                """;
        mockServer.expect(requestTo("http://localhost/akv10/category/"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        // when
        GetAligoCategoryTreeResult result = adapter.getCategoryTree();

        // then
        assertThat(result.categories()).hasSize(1);

        GetAligoCategoryTreeResult.CategoryResult first = result.categories().get(0);
        assertThat(first.code()).isEqualTo("001");
        assertThat(first.name()).isEqualTo("건강");
        assertThat(first.children()).hasSize(1);

        GetAligoCategoryTreeResult.CategoryResult second = first.children().get(0);
        assertThat(second.code()).isEqualTo("001001");
        assertThat(second.name()).isEqualTo("병원");
        assertThat(second.children()).hasSize(1);

        GetAligoCategoryTreeResult.CategoryResult third = second.children().get(0);
        assertThat(third.code()).isEqualTo("00100010001");
        assertThat(third.name()).isEqualTo("종합병원");
        assertThat(third.children()).isEmpty();

        mockServer.verify();
    }

    @Test
    @DisplayName("API 에러 응답 시 도메인 예외가 발생한다")
    void shouldThrowExceptionOnApiError() {
        // given
        String responseBody = """
                {
                    "code": -101,
                    "message": "인증에 실패했습니다."
                }
                """;
        mockServer.expect(requestTo("http://localhost/akv10/category/"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> adapter.getCategoryTree())
                .isInstanceOf(AligoAuthenticationException.class);

        mockServer.verify();
    }
}
