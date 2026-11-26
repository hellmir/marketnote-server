package com.personal.marketnote.notification.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.DeviceTokenSnapshotState;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.domain.notification.DeliveryChannel;
import com.personal.marketnote.notification.domain.notification.FcmSendFailedException;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.notification.NotificationSnapshotState;
import com.personal.marketnote.notification.domain.notification.SendStatus;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationSenderType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorName;
import com.personal.marketnote.notification.port.in.command.vendorcommunication.NotificationVendorCommunicationHistoryCommand;
import com.personal.marketnote.notification.port.in.usecase.vendorcommunication.NotificationRecordVendorCommunicationHistoryUseCase;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendorCommunicationRecorder 테스트")
class VendorCommunicationRecorderTest {

    @Mock
    private NotificationRecordVendorCommunicationHistoryUseCase notificationRecordVendorCommunicationHistoryUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private VendorCommunicationRecorder recorder;

    @BeforeEach
    void setUp() {
        recorder = new VendorCommunicationRecorder(
                notificationRecordVendorCommunicationHistoryUseCase, objectMapper
        );
    }

    @Test
    @DisplayName("recordFcmSendResult 호출 시 성공 결과는 PUSH_NOTIFICATION/FCM/REQUEST/SERVER 메타와 exception=null 로 기록한다")
    void shouldRecordFcmSuccessResult() {
        // given
        Notification notification = buildNotification(100L);
        DeviceToken deviceToken = buildDeviceToken(10L, "abcdefghij1234567890token");
        SendPushNotificationResult result = SendPushNotificationResult.success("MSG-001");

        // when
        recorder.recordFcmSendResult(notification, deviceToken, "제목", "본문", "https://app/landing", result);

        // then
        ArgumentCaptor<NotificationVendorCommunicationHistoryCommand> captor =
                ArgumentCaptor.forClass(NotificationVendorCommunicationHistoryCommand.class);
        verify(notificationRecordVendorCommunicationHistoryUseCase).record(captor.capture());
        NotificationVendorCommunicationHistoryCommand cmd = captor.getValue();
        assertThat(cmd.getTargetType()).isEqualTo(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION);
        assertThat(cmd.getTargetId()).isEqualTo("100");
        assertThat(cmd.getVendorName()).isEqualTo(NotificationVendorName.FCM);
        assertThat(cmd.getCommunicationType()).isEqualTo(NotificationVendorCommunicationType.REQUEST);
        assertThat(cmd.getSender()).isEqualTo(NotificationVendorCommunicationSenderType.SERVER);
        assertThat(cmd.getException()).isNull();
        assertThat(cmd.getAttemptCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("recordFcmSendResult 호출 시 실패 결과의 errorCode 가 exception 으로 기록된다")
    void shouldRecordFcmFailureResultWithErrorCode() {
        // given
        Notification notification = buildNotification(101L);
        DeviceToken deviceToken = buildDeviceToken(11L, "abcdefghij1234567890token");
        SendPushNotificationResult result = SendPushNotificationResult.failure("ERR_QUOTA_EXCEEDED");

        // when
        recorder.recordFcmSendResult(notification, deviceToken, "제목", "본문", null, result);

        // then
        ArgumentCaptor<NotificationVendorCommunicationHistoryCommand> captor =
                ArgumentCaptor.forClass(NotificationVendorCommunicationHistoryCommand.class);
        verify(notificationRecordVendorCommunicationHistoryUseCase).record(captor.capture());
        assertThat(captor.getValue().getException()).isEqualTo("ERR_QUOTA_EXCEEDED");
    }

    @Test
    @DisplayName("recordFcmSendResult 호출 시 payloadJson 의 request/response 구조와 messageId/success 값이 채워진다")
    void shouldRecordPayloadStructure() {
        // given
        Notification notification = buildNotification(102L);
        DeviceToken deviceToken = buildDeviceToken(12L, "abcdefghij1234567890token");
        SendPushNotificationResult result = SendPushNotificationResult.success("MSG-PAYLOAD");

        // when
        recorder.recordFcmSendResult(
                notification, deviceToken, "제목", "본문", "https://landing", result
        );

        // then
        ArgumentCaptor<NotificationVendorCommunicationHistoryCommand> captor =
                ArgumentCaptor.forClass(NotificationVendorCommunicationHistoryCommand.class);
        verify(notificationRecordVendorCommunicationHistoryUseCase).record(captor.capture());
        JsonNode payload = captor.getValue().getPayloadJson();
        assertThat(payload.get("request").get("deviceTokenId").asLong()).isEqualTo(12L);
        assertThat(payload.get("request").get("title").asText()).isEqualTo("제목");
        assertThat(payload.get("request").get("body").asText()).isEqualTo("본문");
        assertThat(payload.get("request").get("landingUrl").asText()).isEqualTo("https://landing");
        assertThat(payload.get("request").get("platform").asText()).isEqualTo("ANDROID");
        assertThat(payload.get("response").get("success").asBoolean()).isTrue();
        assertThat(payload.get("response").get("messageId").asText()).isEqualTo("MSG-PAYLOAD");
        assertThat(captor.getValue().getPayload()).isEqualTo(payload.toString());
    }

    @Test
    @DisplayName("recordFcmSendResult 호출 시 길이 8 초과 토큰은 앞 4자/마스크/뒤 4자 형태로 마스킹된다")
    void shouldMaskLongDeviceToken() {
        // given
        Notification notification = buildNotification(103L);
        DeviceToken deviceToken = buildDeviceToken(13L, "abcdefghij1234567890XYZW");
        SendPushNotificationResult result = SendPushNotificationResult.success("MSG-MASK");

        // when
        recorder.recordFcmSendResult(notification, deviceToken, "T", "B", null, result);

        // then
        ArgumentCaptor<NotificationVendorCommunicationHistoryCommand> captor =
                ArgumentCaptor.forClass(NotificationVendorCommunicationHistoryCommand.class);
        verify(notificationRecordVendorCommunicationHistoryUseCase).record(captor.capture());
        String maskedToken = captor.getValue().getPayloadJson().get("request").get("deviceToken").asText();
        assertThat(maskedToken).isEqualTo("abcd***XYZW");
    }

    @Test
    @DisplayName("recordFcmSendResult 호출 시 길이 8 이하 토큰은 전체가 별표로 마스킹된다")
    void shouldMaskShortDeviceTokenAllStars() {
        // given
        Notification notification = buildNotification(104L);
        DeviceToken deviceToken = buildDeviceToken(14L, "abc12345");
        SendPushNotificationResult result = SendPushNotificationResult.success("MSG-SHORT");

        // when
        recorder.recordFcmSendResult(notification, deviceToken, "T", "B", null, result);

        // then
        ArgumentCaptor<NotificationVendorCommunicationHistoryCommand> captor =
                ArgumentCaptor.forClass(NotificationVendorCommunicationHistoryCommand.class);
        verify(notificationRecordVendorCommunicationHistoryUseCase).record(captor.capture());
        String maskedToken = captor.getValue().getPayloadJson().get("request").get("deviceToken").asText();
        assertThat(maskedToken).isEqualTo("********");
    }

    @Test
    @DisplayName("recordFcmSendException 호출 시 success=false 와 exceptionType, 메시지가 기록된다")
    void shouldRecordFcmSendException() {
        // given
        Notification notification = buildNotification(200L);
        DeviceToken deviceToken = buildDeviceToken(20L, "abcdefghij1234567890token");
        FcmSendFailedException exception = new FcmSendFailedException("FCM 전송 실패");

        // when
        recorder.recordFcmSendException(notification, deviceToken, "제목", "본문", "https://landing", exception);

        // then
        ArgumentCaptor<NotificationVendorCommunicationHistoryCommand> captor =
                ArgumentCaptor.forClass(NotificationVendorCommunicationHistoryCommand.class);
        verify(notificationRecordVendorCommunicationHistoryUseCase).record(captor.capture());
        NotificationVendorCommunicationHistoryCommand cmd = captor.getValue();
        assertThat(cmd.getException()).isEqualTo(exception.getMessage());
        JsonNode payload = cmd.getPayloadJson();
        assertThat(payload.get("response").get("success").asBoolean()).isFalse();
        assertThat(payload.get("response").get("exceptionType").asText()).isEqualTo("FcmSendFailedException");
    }

    @Test
    @DisplayName("recordFcmSendResult 호출 시 record 단계 예외가 발생해도 호출자에게 전파되지 않는다")
    void shouldSwallowExceptionDuringRecord() {
        // given
        Notification notification = buildNotification(300L);
        DeviceToken deviceToken = buildDeviceToken(30L, "abcdefghij1234567890token");
        SendPushNotificationResult result = SendPushNotificationResult.success("MSG-EX");
        willThrow(new RuntimeException("DB error"))
                .given(notificationRecordVendorCommunicationHistoryUseCase)
                .record(any(NotificationVendorCommunicationHistoryCommand.class));

        // when
        recorder.recordFcmSendResult(notification, deviceToken, "T", "B", null, result);

        // then
        verify(notificationRecordVendorCommunicationHistoryUseCase).record(any(NotificationVendorCommunicationHistoryCommand.class));
    }

    @Test
    @DisplayName("recordFcmSendException 호출 시 record 단계 예외가 발생해도 호출자에게 전파되지 않는다")
    void shouldSwallowExceptionDuringRecordOnException() {
        // given
        Notification notification = buildNotification(301L);
        DeviceToken deviceToken = buildDeviceToken(31L, "abcdefghij1234567890token");
        FcmSendFailedException exception = new FcmSendFailedException("FCM 전송 실패");
        willThrow(new RuntimeException("DB error"))
                .given(notificationRecordVendorCommunicationHistoryUseCase)
                .record(any(NotificationVendorCommunicationHistoryCommand.class));

        // when
        recorder.recordFcmSendException(notification, deviceToken, "T", "B", null, exception);

        // then
        verify(notificationRecordVendorCommunicationHistoryUseCase).record(any(NotificationVendorCommunicationHistoryCommand.class));
    }

    @Test
    @DisplayName("recordFcmSendResult 호출 시 notification.id 가 null 이면 targetId 도 null 이다")
    void shouldHandleNullNotificationId() {
        // given
        Notification notification = buildNotification(null);
        DeviceToken deviceToken = buildDeviceToken(40L, "abcdefghij1234567890token");
        SendPushNotificationResult result = SendPushNotificationResult.success("MSG-NULL");
        willAnswer(invocation -> null)
                .given(notificationRecordVendorCommunicationHistoryUseCase)
                .record(any(NotificationVendorCommunicationHistoryCommand.class));

        // when
        recorder.recordFcmSendResult(notification, deviceToken, "T", "B", null, result);

        // then
        ArgumentCaptor<NotificationVendorCommunicationHistoryCommand> captor =
                ArgumentCaptor.forClass(NotificationVendorCommunicationHistoryCommand.class);
        verify(notificationRecordVendorCommunicationHistoryUseCase).record(captor.capture());
        assertThat(captor.getValue().getTargetId()).isNull();
    }

    @Test
    @DisplayName("recordFcmSendResult 호출 시 deviceToken 이 null 이면 record 호출 없이 logFailure 경로만 실행된다")
    void shouldNotRecordWhenDeviceTokenNull() {
        // given
        Notification notification = buildNotification(500L);
        SendPushNotificationResult result = SendPushNotificationResult.success("MSG-NPE");

        // when
        recorder.recordFcmSendResult(notification, null, "T", "B", null, result);

        // then
        verify(notificationRecordVendorCommunicationHistoryUseCase, never())
                .record(any(NotificationVendorCommunicationHistoryCommand.class));
    }

    private Notification buildNotification(Long id) {
        return Notification.from(
                NotificationSnapshotState.builder()
                        .id(id)
                        .userId(1L)
                        .notificationType(NotificationType.NOTICE)
                        .title("제목")
                        .body("본문")
                        .data("{}")
                        .deliveryChannel(DeliveryChannel.PUSH_ONLY)
                        .isRead(false)
                        .landingUrl("https://app/landing")
                        .sendStatus(SendStatus.PENDING)
                        .scheduledAt(LocalDateTime.of(2026, 4, 28, 9, 0))
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private DeviceToken buildDeviceToken(Long id, String token) {
        return DeviceToken.from(
                DeviceTokenSnapshotState.builder()
                        .id(id)
                        .userId(1L)
                        .token(token)
                        .platform(Platform.ANDROID)
                        .deviceId("DEVICE-1")
                        .lastUsedAt(LocalDateTime.of(2026, 4, 28, 8, 0))
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
