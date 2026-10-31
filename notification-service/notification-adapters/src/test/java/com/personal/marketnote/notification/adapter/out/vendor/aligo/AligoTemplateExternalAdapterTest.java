package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButton;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButtonLinkType;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateEmType;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateType;
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

class AligoTemplateExternalAdapterTest {

    private AligoTemplateExternalAdapter adapter;
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
        adapter = new AligoTemplateExternalAdapter(aligoHttpClient, objectMapper);
    }

    private AligoTemplate basicTemplate() {
        return AligoTemplate.from(
                AligoTemplateCreateState.builder()
                        .senderKey("SK")
                        .templateCode("TPL001")
                        .templateName("템플릿명")
                        .templateContent("본문")
                        .templateType(AligoTemplateType.BA)
                        .templateEmType(AligoTemplateEmType.NONE)
                        .build()
        );
    }

    @Nested
    @DisplayName("registerTemplate")
    class RegisterTemplate {

        @Test
        @DisplayName("/akv10/template/add/ 엔드포인트로 form-urlencoded 요청을 전송한다")
        void shouldPostToAddEndpoint() {
            // given
            String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
            mockServer.expect(requestTo("http://localhost/akv10/template/add/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            // when
            adapter.registerTemplate(basicTemplate());

            // then
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("updateTemplate")
    class UpdateTemplate {

        @Test
        @DisplayName("/akv10/template/modify/ 엔드포인트를 호출한다")
        void shouldPostToModifyEndpoint() {
            // given
            String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
            mockServer.expect(requestTo("http://localhost/akv10/template/modify/"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            // when
            adapter.updateTemplate(basicTemplate());

            // then
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("registerTemplate with buttons")
    class RegisterTemplateWithButtons {

        @Test
        @DisplayName("버튼 리스트가 ordering/linkType/플랫폼별 링크를 포함하여 JSON으로 직렬화된다")
        void shouldSerializeButtonsWithOrderingAndLinks() {
            // given
            String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
            mockServer.expect(requestTo("http://localhost/akv10/template/add/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("tpl_button")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("ordering")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("linkType")))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            AligoTemplateButton button = AligoTemplateButton.builder()
                    .ordering(1)
                    .name("자세히 보기")
                    .linkType(AligoTemplateButtonLinkType.WL)
                    .linkMo("https://m.example.com")
                    .linkPc("https://www.example.com")
                    .build();

            AligoTemplate template = AligoTemplate.from(
                    AligoTemplateCreateState.builder()
                            .senderKey("SK")
                            .templateCode("TPL002")
                            .templateName("버튼")
                            .templateContent("본문")
                            .templateType(AligoTemplateType.BA)
                            .templateEmType(AligoTemplateEmType.NONE)
                            .buttons(List.of(button))
                            .build()
            );

            // when
            adapter.registerTemplate(template);

            // then
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getTemplateList")
    class GetTemplateList {

        @Test
        @DisplayName("응답의 template_emphasize_type이 없으면 NONE으로 매핑된다")
        void shouldDefaultEmTypeToNoneWhenResponseMissing() {
            // given
            String responseBody = """
                    {
                        "result_code": 0,
                        "message": "성공",
                        "list": [
                            {
                                "senderkey": "SK",
                                "templt_code": "TPL001",
                                "templt_name": "이름",
                                "templt_content": "본문",
                                "template_type": "BA",
                                "inspStatus": "REG",
                                "status": "R",
                                "comments": null
                            }
                        ]
                    }
                    """;
            mockServer.expect(requestTo("http://localhost/akv10/template/list/"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            // when
            List<AligoTemplate> result = adapter.getTemplateList("SK");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTemplateEmType()).isEqualTo(AligoTemplateEmType.NONE);
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("requestInspection / deleteTemplate")
    class InspectionAndDelete {

        @Test
        @DisplayName("requestInspection은 senderkey/tpl_code를 /akv10/template/request/로 전송한다")
        void shouldRequestInspection() {
            // given
            String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
            mockServer.expect(requestTo("http://localhost/akv10/template/request/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("senderkey=SK")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("tpl_code=TPL100")))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            // when
            adapter.requestInspection("SK", "TPL100");

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("deleteTemplate은 senderkey/tpl_code를 /akv10/template/del/로 전송한다")
        void shouldDeleteTemplate() {
            // given
            String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
            mockServer.expect(requestTo("http://localhost/akv10/template/del/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("senderkey=SK")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("tpl_code=TPL200")))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            // when
            adapter.deleteTemplate("SK", "TPL200");

            // then
            mockServer.verify();
        }
    }
}
