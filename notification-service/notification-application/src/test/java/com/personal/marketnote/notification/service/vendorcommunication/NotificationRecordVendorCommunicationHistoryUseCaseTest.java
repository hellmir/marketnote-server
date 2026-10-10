package com.personal.marketnote.notification.service.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationHistory;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationSenderType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorName;
import com.personal.marketnote.notification.port.in.command.vendorcommunication.NotificationVendorCommunicationHistoryCommand;
import com.personal.marketnote.notification.port.out.vendorcommunication.SaveNotificationVendorCommunicationHistoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationRecordVendorCommunicationHistoryUseCase 테스트")
class NotificationRecordVendorCommunicationHistoryUseCaseTest {

    @InjectMocks
    private NotificationRecordVendorCommunicationHistoryService service;

    @Mock
    private SaveNotificationVendorCommunicationHistoryPort saveNotificationVendorCommunicationHistoryPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("FCM 푸시 성공 이력을 exception=null로 저장한다")
    void shouldRecordSuccessHistory() throws Exception {
        JsonNode payloadJson = objectMapper.readTree("{\"response\":{\"success\":true}}");
        NotificationVendorCommunicationHistoryCommand command = baseCommandBuilder(payloadJson)
                .exception(null)
                .build();
        given(saveNotificationVendorCommunicationHistoryPort.save(any(NotificationVendorCommunicationHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        NotificationVendorCommunicationHistory result = service.record(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getException()).isNull();
        assertThat(result.getTargetType()).isEqualTo(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION);
        assertThat(result.getVendorName()).isEqualTo(NotificationVendorName.FCM);
        verify(saveNotificationVendorCommunicationHistoryPort).save(any(NotificationVendorCommunicationHistory.class));
    }

    @Test
    @DisplayName("FCM 푸시 실패 이력을 exception과 함께 저장한다")
    void shouldRecordFailureHistoryWithException() throws Exception {
        JsonNode payloadJson = objectMapper.readTree("{\"response\":{\"success\":false}}");
        NotificationVendorCommunicationHistoryCommand command = baseCommandBuilder(payloadJson)
                .exception("INTERNAL")
                .build();
        given(saveNotificationVendorCommunicationHistoryPort.save(any(NotificationVendorCommunicationHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        NotificationVendorCommunicationHistory result = service.record(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getException()).isEqualTo("INTERNAL");
    }

    @Test
    @DisplayName("FCM 예외 발생 이력을 예외 메시지와 함께 저장한다")
    void shouldRecordExceptionHistory() throws Exception {
        JsonNode payloadJson = objectMapper.readTree("{\"response\":{\"exception\":\"FCM timeout\"}}");
        NotificationVendorCommunicationHistoryCommand command = baseCommandBuilder(payloadJson)
                .exception("FcmSendFailedException")
                .build();
        given(saveNotificationVendorCommunicationHistoryPort.save(any(NotificationVendorCommunicationHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        NotificationVendorCommunicationHistory result = service.record(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getException()).isEqualTo("FcmSendFailedException");
    }

    @Test
    @DisplayName("payload_json이 도메인 객체에 그대로 전달되어 저장된다")
    void shouldPassPayloadJsonAsIs() throws Exception {
        JsonNode payloadJson = objectMapper.readTree(
                "{\"request\":{\"platform\":\"ANDROID\"},\"response\":{\"messageId\":\"msg-1\"}}"
        );
        NotificationVendorCommunicationHistoryCommand command = baseCommandBuilder(payloadJson).build();
        given(saveNotificationVendorCommunicationHistoryPort.save(any(NotificationVendorCommunicationHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        service.record(command);

        ArgumentCaptor<NotificationVendorCommunicationHistory> captor =
                ArgumentCaptor.forClass(NotificationVendorCommunicationHistory.class);
        verify(saveNotificationVendorCommunicationHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getPayloadJson()).isEqualTo(payloadJson);
        assertThat(captor.getValue().getPayload()).contains("\"messageId\":\"msg-1\"");
    }

    private NotificationVendorCommunicationHistoryCommand.NotificationVendorCommunicationHistoryCommandBuilder baseCommandBuilder(
            JsonNode payloadJson
    ) {
        return NotificationVendorCommunicationHistoryCommand.builder()
                .targetType(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION)
                .targetId("100")
                .vendorName(NotificationVendorName.FCM)
                .communicationType(NotificationVendorCommunicationType.REQUEST)
                .sender(NotificationVendorCommunicationSenderType.SERVER)
                .payload(payloadJson.toString())
                .payloadJson(payloadJson);
    }
}
