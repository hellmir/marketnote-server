package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import com.personal.marketnote.notification.domain.aligo.AligoMessage;
import com.personal.marketnote.notification.domain.aligo.AligoMessageCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipientCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoMessageType;
import com.personal.marketnote.notification.port.out.aligo.SendAligoAlimTalkExternalPort;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AligoAlimTalkExternalAdapterTest {

    private AligoAlimTalkExternalAdapter adapter;
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
        adapter = new AligoAlimTalkExternalAdapter(aligoHttpClient);
    }

    private AligoMessage buildMessage(
            List<AligoMessageRecipientCreateState> recipients,
            String failoverSubject, String failoverBody
    ) {
        return AligoMessage.from(
                AligoMessageCreateState.builder()
                        .senderKey("SK")
                        .templateCode("TPL001")
                        .sender("01011112222")
                        .messageType(AligoMessageType.AT)
                        .content("안녕하세요, #{name}님")
                        .title("제목")
                        .buttons(null)
                        .failoverSubject(failoverSubject)
                        .failoverMessageBody(failoverBody)
                        .requestedAt(LocalDateTime.of(2026, 4, 20, 12, 0))
                        .recipients(recipients)
                        .build()
        );
    }

    private String successResponseBody() {
        return """
                {
                    "result_code": 0,
                    "message": "성공",
                    "info": {
                        "type": "AT",
                        "mid": "MID001",
                        "scnt": 1,
                        "fcnt": 0,
                        "mdid": "MDID001",
                        "smid": null
                    }
                }
                """;
    }

    @Nested
    @DisplayName("send")
    class Send {

        @Test
        @DisplayName("수신자가 receiver_N/subject_N/message_N 인덱스 키로 매핑된다")
        void shouldMapRecipientsSubjectMessageByIndex() {
            // given
            mockServer.expect(requestTo("http://localhost/akv10/alimtalk/send/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(Matchers.containsString("receiver_1")))
                    .andExpect(content().string(Matchers.containsString("subject_1")))
                    .andExpect(content().string(Matchers.containsString("message_1")))
                    .andExpect(content().string(Matchers.containsString("receiver_2")))
                    .andExpect(content().string(Matchers.containsString("subject_2")))
                    .andExpect(content().string(Matchers.containsString("message_2")))
                    .andRespond(withSuccess(successResponseBody(), MediaType.APPLICATION_JSON));

            List<AligoMessageRecipientCreateState> recipients = List.of(
                    AligoMessageRecipientCreateState.builder().receiver("01011112221").build(),
                    AligoMessageRecipientCreateState.builder().receiver("01011112222").build()
            );
            AligoMessage message = buildMessage(recipients, null, null);

            // when
            SendAligoAlimTalkExternalPort.AligoAlimTalkSendResponse response = adapter.send(message);

            // then
            assertThat(response.mid()).isEqualTo("MID001");
            mockServer.verify();
        }

        @Test
        @DisplayName("failover 콘텐츠가 있으면 fsubject_N/fmessage_N 파라미터가 추가된다")
        void shouldAddFailoverSubjectAndMessageWhenPresent() {
            // given
            mockServer.expect(requestTo("http://localhost/akv10/alimtalk/send/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().string(Matchers.containsString("fsubject_1")))
                    .andExpect(content().string(Matchers.containsString("fmessage_1")))
                    .andRespond(withSuccess(successResponseBody(), MediaType.APPLICATION_JSON));

            List<AligoMessageRecipientCreateState> recipients = List.of(
                    AligoMessageRecipientCreateState.builder().receiver("01011112221").build()
            );
            AligoMessage message = buildMessage(recipients, "대체 제목", "대체 본문");

            // when
            adapter.send(message);

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("recvName이 존재할 때만 recvname_N이 추가된다")
        void shouldAddRecvNameOnlyWhenPresent() {
            // given
            mockServer.expect(requestTo("http://localhost/akv10/alimtalk/send/"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().string(Matchers.containsString("recvname_1")))
                    .andExpect(content().string(Matchers.not(Matchers.containsString("recvname_2"))))
                    .andRespond(withSuccess(successResponseBody(), MediaType.APPLICATION_JSON));

            List<AligoMessageRecipientCreateState> recipients = List.of(
                    AligoMessageRecipientCreateState.builder()
                            .receiver("01011112221").recvName("홍길동").build(),
                    AligoMessageRecipientCreateState.builder()
                            .receiver("01011112222").build()
            );
            AligoMessage message = buildMessage(recipients, null, null);

            // when
            adapter.send(message);

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("응답 info 필드가 없으면 AligoApiRequestFailedException이 발생한다")
        void shouldThrowWhenResponseInfoMissing() {
            // given
            String responseBody = "{\"result_code\":0,\"message\":\"성공\"}";
            mockServer.expect(requestTo("http://localhost/akv10/alimtalk/send/"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

            List<AligoMessageRecipientCreateState> recipients = List.of(
                    AligoMessageRecipientCreateState.builder().receiver("01011112221").build()
            );
            AligoMessage message = buildMessage(recipients, null, null);

            // when & then
            assertThatThrownBy(() -> adapter.send(message))
                    .isInstanceOf(AligoApiRequestFailedException.class);
            mockServer.verify();
        }
    }
}
