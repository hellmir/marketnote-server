package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplateCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButtonLinkType;
import com.personal.marketnote.notification.domain.aligo.BrandTalkButton;
import com.personal.marketnote.notification.domain.aligo.BrandTalkButtonCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselCommerce;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselCommerceCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselItem;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselItemCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCommerceInfo;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCommerceInfoCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCoupon;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCouponCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateType;
import com.personal.marketnote.notification.domain.aligo.BrandTalkWideItem;
import com.personal.marketnote.notification.domain.aligo.BrandTalkWideItemCreateState;
import com.personal.marketnote.notification.port.in.result.aligo.GetBrandTalkTemplateListResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AligoBrandTalkTemplateExternalAdapterTest {

    private AligoBrandTalkTemplateExternalAdapter adapter;
    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper;

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

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("http://localhost").build();

        AligoHttpClient aligoHttpClient = new AligoHttpClient(restClient, objectMapper, properties);
        adapter = new AligoBrandTalkTemplateExternalAdapter(aligoHttpClient, objectMapper);
    }

    @Nested
    @DisplayName("registerTemplate")
    class RegisterTemplate {

        @Test
        @DisplayName("기본 템플릿 필수 필드가 multipart 파라미터로 매핑된다")
        void shouldMapBasicTemplateFields() {
            // given
            String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
            mockServer.expect(requestTo("http://localhost/brandtalk/template/create/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(
                    AligoBrandTalkTemplateCreateState.builder()
                            .senderKey("SENDER-KEY")
                            .templateCode("TPL001")
                            .templateName("템플릿명")
                            .templateType(BrandTalkTemplateType.TEXT)
                            .templateContent("본문")
                            .adult(false)
                            .build()
            );

            // when
            adapter.registerTemplate(template, null, null, List.of(), List.of());

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("와이드 아이템 리스트의 sub_wide_item 인덱스가 1부터 순서대로 직렬화된다")
        void shouldSerializeSubWideItemIndexesInOrder() throws JsonProcessingException {
            // given
            String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
            mockServer.expect(requestTo("http://localhost/brandtalk/template/create/"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            BrandTalkWideItem main = BrandTalkWideItem.from(
                    BrandTalkWideItemCreateState.builder().title("메인").build()
            );
            BrandTalkWideItem sub1 = BrandTalkWideItem.from(
                    BrandTalkWideItemCreateState.builder().title("서브1").build()
            );
            BrandTalkWideItem sub2 = BrandTalkWideItem.from(
                    BrandTalkWideItemCreateState.builder().title("서브2").build()
            );

            AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(
                    AligoBrandTalkTemplateCreateState.builder()
                            .senderKey("SK")
                            .templateCode("TPL002")
                            .templateName("WIL 템플릿")
                            .templateType(BrandTalkTemplateType.WIDE_ITEM_LIST)
                            .templateContent("본문")
                            .templateHeader("헤더")
                            .mainWideItem(main)
                            .subWideItems(List.of(sub1, sub2))
                            .build()
            );

            // when
            adapter.registerTemplate(template, null, null, List.of(), List.of());

            // then
            mockServer.verify();
            String mainJson = objectMapper.writeValueAsString(
                    java.util.Map.of("title", "메인")
            );
            String sub1Json = objectMapper.writeValueAsString(
                    java.util.Map.of("title", "서브1")
            );
            String sub2Json = objectMapper.writeValueAsString(
                    java.util.Map.of("title", "서브2")
            );
            assertThat(mainJson).contains("메인");
            assertThat(sub1Json).contains("서브1");
            assertThat(sub2Json).contains("서브2");
        }

        @Test
        @DisplayName("캐러셀 아이템과 버튼이 carousel_listN/carousel_list_buttonN으로 직렬화된다")
        void shouldSerializeCarouselItemsAndButtons() {
            // given
            String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
            mockServer.expect(requestTo("http://localhost/brandtalk/template/create/"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            BrandTalkButton button = BrandTalkButton.from(
                    BrandTalkButtonCreateState.builder()
                            .name("자세히 보기")
                            .linkType(AligoTemplateButtonLinkType.WL)
                            .linkM("https://m.example.com")
                            .linkP("https://www.example.com")
                            .build()
            );

            BrandTalkCarouselItem item = BrandTalkCarouselItem.from(
                    BrandTalkCarouselItemCreateState.builder()
                            .header("타이틀")
                            .content("내용")
                            .buttons(List.of(button))
                            .build()
            );

            AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(
                    AligoBrandTalkTemplateCreateState.builder()
                            .senderKey("SK")
                            .templateCode("TPL003")
                            .templateName("캐러셀")
                            .templateType(BrandTalkTemplateType.CAROUSEL_FEED)
                            .templateContent("본문")
                            .carouselHeader("헤더")
                            .carouselItems(List.of(item))
                            .build()
            );

            // when
            adapter.registerTemplate(template, null, null, List.of(), List.of());

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("커머스 템플릿의 commerceInfo가 JSON 문자열로 직렬화된다")
        void shouldSerializeCommerceInfoAsJson() throws JsonProcessingException {
            // given
            String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
            mockServer.expect(requestTo("http://localhost/brandtalk/template/create/"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            BrandTalkCommerceInfo commerceInfo = BrandTalkCommerceInfo.from(
                    BrandTalkCommerceInfoCreateState.builder()
                            .title("상품명")
                            .regularPrice("10000")
                            .discountPrice("8000")
                            .discountRate("20")
                            .build()
            );

            AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(
                    AligoBrandTalkTemplateCreateState.builder()
                            .senderKey("SK")
                            .templateCode("TPL004")
                            .templateName("커머스")
                            .templateType(BrandTalkTemplateType.COMMERCE)
                            .templateContent("본문")
                            .commerceInfo(commerceInfo)
                            .build()
            );

            // when
            adapter.registerTemplate(template, new byte[]{1, 2}, "image.jpg", List.of(), List.of());

            // then
            mockServer.verify();
            String expected = objectMapper.writeValueAsString(new java.util.LinkedHashMap<String, String>() {{
                put("title", "상품명");
                put("regularPrice", "10000");
                put("discountPrice", "8000");
                put("discountRate", "20");
            }});
            assertThat(expected).contains("regularPrice").contains("discountPrice");
        }

        @Test
        @DisplayName("이미지 파일이 ByteArrayResource로 multipart 폼에 추가된다")
        void shouldAddImageAsByteArrayResource() {
            // given
            String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
            mockServer.expect(requestTo("http://localhost/brandtalk/template/create/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(
                    AligoBrandTalkTemplateCreateState.builder()
                            .senderKey("SK")
                            .templateCode("TPL005")
                            .templateName("IMG")
                            .templateType(BrandTalkTemplateType.IMAGE)
                            .templateContent("본문")
                            .build()
            );

            // when
            adapter.registerTemplate(
                    template, new byte[]{10, 20, 30}, "image.png", List.of(), List.of()
            );

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("JSON 직렬화 실패 시 AligoApiRequestFailedException으로 변환된다")
        void shouldThrowAligoApiRequestFailedExceptionOnJsonSerializationFailure() throws JsonProcessingException {
            // given
            ObjectMapper failingMapper = Mockito.mock(ObjectMapper.class);
            Mockito.when(failingMapper.writeValueAsString(Mockito.any()))
                    .thenThrow(new JsonProcessingException("직렬화 실패") {
                    });

            AligoProperties properties = new AligoProperties();
            properties.setBaseUrl("http://localhost");
            properties.setApiKey("k");
            properties.setUserId("u");
            AligoProperties.Retry retry = new AligoProperties.Retry();
            retry.setMaxAttempts(1);
            retry.setInitialBackoffMs(10);
            properties.setRetry(retry);

            RestClient.Builder builder = RestClient.builder();
            MockRestServiceServer.bindTo(builder).build();
            RestClient restClient = builder.baseUrl("http://localhost").build();
            AligoHttpClient httpClient = new AligoHttpClient(restClient, new ObjectMapper(), properties);

            AligoBrandTalkTemplateExternalAdapter failingAdapter =
                    new AligoBrandTalkTemplateExternalAdapter(httpClient, failingMapper);

            BrandTalkButton button = BrandTalkButton.from(
                    BrandTalkButtonCreateState.builder()
                            .name("btn")
                            .linkType(AligoTemplateButtonLinkType.WL)
                            .linkM("https://m")
                            .linkP("https://p")
                            .build()
            );
            AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(
                    AligoBrandTalkTemplateCreateState.builder()
                            .senderKey("SK")
                            .templateCode("TPL999")
                            .templateName("BTN")
                            .templateType(BrandTalkTemplateType.TEXT)
                            .templateContent("본문")
                            .buttons(List.of(button))
                            .build()
            );

            // when & then
            assertThatThrownBy(() -> failingAdapter.registerTemplate(template, null, null, List.of(), List.of()))
                    .isInstanceOf(AligoApiRequestFailedException.class);
        }
    }

    @Nested
    @DisplayName("getTemplateList")
    class GetTemplateList {

        @Test
        @DisplayName("페이징 응답이 ExternalListResponse로 매핑된다")
        void shouldMapPagedResponse() {
            // given
            String responseBody = """
                    {
                        "result_code": 0,
                        "message": "성공",
                        "info": {
                            "current_page": 1,
                            "page_size": 10,
                            "total": 25
                        },
                        "list": [
                            {
                                "tpl_code": "TPL001",
                                "tpl_name": "템플릿1",
                                "tpl_type": "TEXT",
                                "tpl_status": "A",
                                "send_variable": ["#{name}"],
                                "create_at": "2026-10-31"
                            }
                        ]
                    }
                    """;
            mockServer.expect(requestTo("http://localhost/brandtalk/template/list/"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            // when
            GetBrandTalkTemplateListResult.ExternalListResponse response =
                    adapter.getTemplateList("SK", 1, 10);

            // then
            assertThat(response.pageInfo().currentPage()).isEqualTo(1);
            assertThat(response.pageInfo().pageSize()).isEqualTo(10);
            assertThat(response.pageInfo().total()).isEqualTo(25);
            assertThat(response.templates()).hasSize(1);
            assertThat(response.templates().get(0).templateCode()).isEqualTo("TPL001");
            assertThat(response.templates().get(0).sendVariable()).containsExactly("#{name}");
            mockServer.verify();
        }
    }
}
