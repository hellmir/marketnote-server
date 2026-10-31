package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessage;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageRecipientCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkFailoverType;
import com.personal.marketnote.notification.domain.aligo.BrandTalkKakaoTarget;
import com.personal.marketnote.notification.port.out.aligo.SendBrandTalkExternalPort;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AligoBrandTalkExternalAdapterTest {

    private AligoBrandTalkExternalAdapter adapter;
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
        adapter = new AligoBrandTalkExternalAdapter(aligoHttpClient);
    }

    private AligoBrandTalkMessage buildMessage(int recipientCount, boolean failoverEnabled, BrandTalkFailoverType failoverType) {
        List<AligoBrandTalkMessageRecipientCreateState> recipients = new java.util.ArrayList<>();
        for (int i = 0; i < recipientCount; i++) {
            recipients.add(
                    AligoBrandTalkMessageRecipientCreateState.builder()
                            .receiver("0101234000" + i)
                            .receiverMessage("{\"#{name}\":\"user" + i + "\"}")
                            .build()
            );
        }

        AligoBrandTalkMessageCreateState.AligoBrandTalkMessageCreateStateBuilder stateBuilder =
                AligoBrandTalkMessageCreateState.builder()
                        .senderKey("SK")
                        .sender("01011112222")
                        .kakaoTarget(BrandTalkKakaoTarget.M)
                        .advertYn(false)
                        .templateCode("TPL001")
                        .failoverEnabled(failoverEnabled)
                        .failoverSubject(failoverEnabled ? "대체 제목" : null)
                        .failoverType(failoverEnabled ? failoverType : null)
                        .requestedAt(LocalDateTime.of(2026, 4, 20, 12, 0))
                        .recipients(recipients);

        return AligoBrandTalkMessage.from(stateBuilder.build());
    }

    private String successResponseBody() {
        return """
                {
                    "result_code": 0,
                    "message": "성공",
                    "info": {
                        "type": "BW",
                        "mid": "MID001",
                        "scnt": 1,
                        "fcnt": 0,
                        "unit_cost": 12,
                        "total_cost": 12
                    }
                }
                """;
    }

    @Nested
    @DisplayName("send")
    class Send {

        @Test
        @DisplayName("수신자가 receiver_N, receiver_N_message 인덱스 키로 매핑된다")
        void shouldMapRecipientsByIndex() {
            // given
            mockServer.expect(requestTo("http://localhost/brandtalk/template/send/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(content().string(Matchers.containsString("receiver_1")))
                    .andExpect(content().string(Matchers.containsString("receiver_1_message")))
                    .andExpect(content().string(Matchers.containsString("receiver_2")))
                    .andExpect(content().string(Matchers.containsString("receiver_2_message")))
                    .andRespond(withSuccess(successResponseBody(), MediaType.APPLICATION_JSON));

            AligoBrandTalkMessage message = buildMessage(2, false, null);

            // when
            SendBrandTalkExternalPort.BrandTalkSendResponse response =
                    adapter.send(message, List.of(), List.of());

            // then
            assertThat(response.mid()).isEqualTo("MID001");
            assertThat(response.successCount()).isEqualTo(1);
            mockServer.verify();
        }

        @Test
        @DisplayName("failover 활성 시 failover=Y 파라미터가 포함된다")
        void shouldAddFailoverFlagWhenEnabled() {
            // given
            mockServer.expect(requestTo("http://localhost/brandtalk/template/send/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().string(Matchers.containsString("failover")))
                    .andRespond(withSuccess(successResponseBody(), MediaType.APPLICATION_JSON));

            AligoBrandTalkMessage message = buildMessage(1, true, BrandTalkFailoverType.SMS);

            // when
            adapter.send(message, List.of(), List.of());

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("MMS failover에 이미지가 4개 이상이어도 최대 3개만 failover_mmsN으로 전송된다")
        void shouldLimitMmsImagesToThree() {
            // given
            mockServer.expect(requestTo("http://localhost/brandtalk/template/send/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().string(Matchers.containsString("failover_mms1")))
                    .andExpect(content().string(Matchers.containsString("failover_mms2")))
                    .andExpect(content().string(Matchers.containsString("failover_mms3")))
                    .andExpect(content().string(Matchers.not(Matchers.containsString("failover_mms4"))))
                    .andRespond(withSuccess(successResponseBody(), MediaType.APPLICATION_JSON));

            AligoBrandTalkMessage message = buildMessage(1, true, BrandTalkFailoverType.MMS);

            List<byte[]> images = List.of(
                    new byte[]{1}, new byte[]{2}, new byte[]{3}, new byte[]{4}, new byte[]{5}
            );
            List<String> names = List.of("a.png", "b.png", "c.png", "d.png", "e.png");

            // when
            adapter.send(message, images, names);

            // then
            mockServer.verify();
        }
    }
}
