package com.personal.marketnote.notification.domain.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationVendorCommunicationHistory 도메인 테스트")
class NotificationVendorCommunicationHistoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("CreateState로 생성 시 모든 필드가 올바르게 매핑된다")
    void shouldMapAllFieldsFromCreateState() throws Exception {
        JsonNode payloadJson = objectMapper.readTree("{\"messageId\":\"msg-1\"}");
        NotificationVendorCommunicationHistoryCreateState state =
                NotificationVendorCommunicationHistoryCreateState.builder()
                        .targetType(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION)
                        .targetId("100")
                        .vendorName(NotificationVendorName.FCM)
                        .communicationType(NotificationVendorCommunicationType.REQUEST)
                        .sender(NotificationVendorCommunicationSenderType.SERVER)
                        .exception(null)
                        .payload("{\"title\":\"알림\"}")
                        .payloadJson(payloadJson)
                        .build();

        NotificationVendorCommunicationHistory history = NotificationVendorCommunicationHistory.from(state);

        assertThat(history.getTargetType()).isEqualTo(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION);
        assertThat(history.getTargetId()).isEqualTo("100");
        assertThat(history.getVendorName()).isEqualTo(NotificationVendorName.FCM);
        assertThat(history.getCommunicationType()).isEqualTo(NotificationVendorCommunicationType.REQUEST);
        assertThat(history.getSender()).isEqualTo(NotificationVendorCommunicationSenderType.SERVER);
        assertThat(history.getException()).isNull();
        assertThat(history.getPayload()).isEqualTo("{\"title\":\"알림\"}");
        assertThat(history.getPayloadJson()).isEqualTo(payloadJson);
        assertThat(history.getId()).isNull();
        assertThat(history.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("payload_json이 null이어도 생성 가능하다")
    void shouldAllowNullPayloadJson() {
        NotificationVendorCommunicationHistoryCreateState state =
                NotificationVendorCommunicationHistoryCreateState.builder()
                        .targetType(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION)
                        .targetId("200")
                        .vendorName(NotificationVendorName.FCM)
                        .communicationType(NotificationVendorCommunicationType.RESPONSE)
                        .sender(NotificationVendorCommunicationSenderType.VENDOR)
                        .exception(null)
                        .payload("{\"success\":true}")
                        .payloadJson(null)
                        .build();

        NotificationVendorCommunicationHistory history = NotificationVendorCommunicationHistory.from(state);

        assertThat(history.getPayloadJson()).isNull();
        assertThat(history.getPayload()).isEqualTo("{\"success\":true}");
    }

    @Test
    @DisplayName("exception 필드가 null이면 성공 이력으로 판단한다")
    void shouldBeSuccessWhenExceptionIsNull() {
        NotificationVendorCommunicationHistory history = NotificationVendorCommunicationHistory.from(
                NotificationVendorCommunicationHistoryCreateState.builder()
                        .targetType(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION)
                        .targetId("300")
                        .vendorName(NotificationVendorName.FCM)
                        .communicationType(NotificationVendorCommunicationType.RESPONSE)
                        .sender(NotificationVendorCommunicationSenderType.VENDOR)
                        .exception(null)
                        .payload("{}")
                        .build()
        );

        assertThat(history.isSuccess()).isTrue();
        assertThat(history.isFailure()).isFalse();
    }

    @Test
    @DisplayName("exception 필드가 존재하면 실패 이력으로 판단한다")
    void shouldBeFailureWhenExceptionExists() {
        NotificationVendorCommunicationHistory history = NotificationVendorCommunicationHistory.from(
                NotificationVendorCommunicationHistoryCreateState.builder()
                        .targetType(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION)
                        .targetId("400")
                        .vendorName(NotificationVendorName.FCM)
                        .communicationType(NotificationVendorCommunicationType.REQUEST)
                        .sender(NotificationVendorCommunicationSenderType.SERVER)
                        .exception("UNREGISTERED")
                        .payload("{}")
                        .build()
        );

        assertThat(history.isSuccess()).isFalse();
        assertThat(history.isFailure()).isTrue();
    }

    @Test
    @DisplayName("SnapshotState로 복원 시 id/createdAt 포함 모든 필드가 매핑된다")
    void shouldRestoreFromSnapshotState() throws Exception {
        JsonNode payloadJson = objectMapper.readTree("{\"errorCode\":\"INTERNAL\"}");
        java.time.LocalDateTime createdAt = java.time.LocalDateTime.of(2026, 4, 15, 10, 0);

        NotificationVendorCommunicationHistorySnapshotState snapshot =
                NotificationVendorCommunicationHistorySnapshotState.builder()
                        .id(777L)
                        .targetType(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION)
                        .targetId("500")
                        .vendorName(NotificationVendorName.FCM)
                        .communicationType(NotificationVendorCommunicationType.RESPONSE)
                        .sender(NotificationVendorCommunicationSenderType.VENDOR)
                        .exception("INTERNAL")
                        .payload("{\"success\":false}")
                        .payloadJson(payloadJson)
                        .createdAt(createdAt)
                        .build();

        NotificationVendorCommunicationHistory history = NotificationVendorCommunicationHistory.from(snapshot);

        assertThat(history.getId()).isEqualTo(777L);
        assertThat(history.getCreatedAt()).isEqualTo(createdAt);
        assertThat(history.getException()).isEqualTo("INTERNAL");
        assertThat(history.getPayloadJson()).isEqualTo(payloadJson);
        assertThat(history.isFailure()).isTrue();
    }
}
