package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.domain.notification.*;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.template.*;
import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;
import com.personal.marketnote.notification.port.in.result.notification.SendNotificationResult;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SaveNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SendPushNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationUseCaseTest {

    @InjectMocks
    private SendNotificationService sendNotificationService;

    @Mock
    private FindNotificationTemplatePort findNotificationTemplatePort;

    @Mock
    private FindNotificationPreferencePort findNotificationPreferencePort;

    @Mock
    private FindDeviceTokenPort findDeviceTokenPort;

    @Mock
    private SaveNotificationPort saveNotificationPort;

    @Mock
    private UpdateNotificationPort updateNotificationPort;

    @Mock
    private SendPushNotificationPort sendPushNotificationPort;

    @Mock
    private DeleteDeviceTokenPort deleteDeviceTokenPort;

    @Mock
    private FindNotificationPort findNotificationPort;

    @Mock
    private PublishSseEventPort publishSseEventPort;

    @Mock
    private com.personal.marketnote.notification.port.out.event.PublishNotificationSentEventPort publishNotificationSentEventPort;

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-06-03T05:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    private static final Long USER_ID = 1L;
    private static final String TEMPLATE_CODE = "ORDER_PAYMENT_COMPLETED";

    @Nested
    @DisplayName("템플릿 조회")
    class TemplateNotFound {

        @Test
        @DisplayName("템플릿이 존재하지 않으면 NotificationTemplateNotFoundException이 발생한다")
        void shouldThrowWhenTemplateNotFound() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            when(findNotificationTemplatePort.findActiveByTemplateCode(TEMPLATE_CODE))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sendNotificationService.sendNotification(command))
                    .isInstanceOf(NotificationTemplateNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("카테고리별 수신 동의 검사")
    class ConsentCheck {

        @Test
        @DisplayName("MANDATORY 카테고리는 수신 설정 조회 없이 발송 성공한다")
        void shouldSendWithoutConsentCheckForMandatory() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateAndSave(template);
            setupDeviceTokensAndPush(1, true);

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("SENT");
            verifyNoInteractions(findNotificationPreferencePort);
        }

        @Test
        @DisplayName("INFORMATIONAL 카테고리는 수신 설정 조회 없이 발송 성공한다")
        void shouldSendWithoutConsentCheckForInformational() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.INFORMATIONAL);
            setupTemplateAndSave(template);
            setupDeviceTokensAndPush(1, true);

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("SENT");
            verifyNoInteractions(findNotificationPreferencePort);
        }

        @Test
        @DisplayName("PROMOTIONAL 카테고리에서 수신 동의 상태면 발송 성공하고 광고 라벨이 적용된다")
        void shouldSendWithAdLabelForConsentedPromotional() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.PROMOTIONAL);
            setupTemplateFound(template);
            setupPreferenceEnabled();
            when(saveNotificationPort.save(any(Notification.class)))
                    .thenAnswer(invocation -> {
                        Notification notification = invocation.getArgument(0);
                        assertThat(notification.getTitle()).startsWith("(광고) ");
                        assertThat(notification.getBody()).endsWith("\n[수신거부:더보기>설정]");
                        return withId(notification, 100L);
                    });
            setupDeviceTokensAndPush(1, true);

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("SENT");
        }

        @Test
        @DisplayName("PROMOTIONAL 카테고리에서 수신 미동의(enabled=false)이면 SKIPPED 반환한다")
        void shouldReturnSkippedForDisabledPromotional() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.PROMOTIONAL);
            setupTemplateFound(template);
            setupPreferenceDisabled();
            when(saveNotificationPort.save(any(Notification.class)))
                    .thenAnswer(invocation -> withId(invocation.getArgument(0), 100L));

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("SKIPPED");
            assertThat(result.sentDeviceCount()).isZero();
            verifyNoInteractions(sendPushNotificationPort);
        }

        @Test
        @DisplayName("PROMOTIONAL 카테고리에서 수신 설정 미존재이면 SKIPPED 반환한다")
        void shouldReturnSkippedWhenPreferenceNotFound() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.PROMOTIONAL);
            setupTemplateFound(template);
            when(findNotificationPreferencePort.findByUserIdAndNotificationType(USER_ID, NotificationType.ORDER_PAYMENT_COMPLETED))
                    .thenReturn(Optional.empty());
            when(saveNotificationPort.save(any(Notification.class)))
                    .thenAnswer(invocation -> withId(invocation.getArgument(0), 100L));

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("SKIPPED");
        }
    }

    @Nested
    @DisplayName("야간 광고성 알림 지연")
    class NightTimeDelay {

        @Test
        @DisplayName("PROMOTIONAL 카테고리 야간 시간대면 SCHEDULED 상태로 저장한다")
        void shouldScheduleForPromotionalAtNight() {
            // given
            Clock nightClock = Clock.fixed(
                    Instant.parse("2026-06-03T13:00:00Z"),
                    ZoneId.of("Asia/Seoul")
            );
            when(clock.instant()).thenReturn(nightClock.instant());
            when(clock.getZone()).thenReturn(nightClock.getZone());

            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.PROMOTIONAL);
            setupTemplateFound(template);
            setupPreferenceEnabled();
            when(saveNotificationPort.save(any(Notification.class)))
                    .thenAnswer(invocation -> {
                        Notification notification = invocation.getArgument(0);
                        assertThat(notification.getSendStatus()).isEqualTo(SendStatus.SCHEDULED);
                        return withId(notification, 100L);
                    });

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("SCHEDULED");
            verifyNoInteractions(sendPushNotificationPort);
        }
    }

    @Nested
    @DisplayName("FCM 푸시 발송")
    class PushSending {

        @Test
        @DisplayName("2개 디바이스 모두 발송 성공하면 SENT 상태와 sentDeviceCount=2를 반환한다")
        void shouldReturnSentWhenAllDevicesSucceed() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateAndSave(template);
            setupDeviceTokensAndPush(2, true);

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("SENT");
            assertThat(result.sentDeviceCount()).isEqualTo(2);
            assertThat(result.failedDeviceCount()).isZero();
        }

        @Test
        @DisplayName("2개 디바이스 중 1개 실패하면 SENT 상태와 failedDeviceCount=1을 반환한다")
        void shouldReturnSentWhenPartiallySucceeds() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateAndSave(template);
            List<DeviceToken> tokens = List.of(
                    createDeviceToken(1L, "token1", Platform.ANDROID),
                    createDeviceToken(2L, "token2", Platform.IOS)
            );
            when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.success("msg1"))
                    .thenReturn(SendPushNotificationResult.failure("INTERNAL"));

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("SENT");
            assertThat(result.sentDeviceCount()).isEqualTo(1);
            assertThat(result.failedDeviceCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("전부 실패하면 FAILED 상태를 반환한다")
        void shouldReturnFailedWhenAllFail() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateAndSave(template);
            List<DeviceToken> tokens = List.of(createDeviceToken(1L, "token1", Platform.ANDROID));
            when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.failure("INTERNAL"));

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("FAILED");
            assertThat(result.failedDeviceCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("tokenInvalid 시 해당 디바이스 토큰을 삭제한다")
        void shouldDeleteInvalidToken() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateAndSave(template);
            List<DeviceToken> tokens = List.of(createDeviceToken(1L, "invalid-token", Platform.ANDROID));
            when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.tokenInvalid("UNREGISTERED"));

            // when
            sendNotificationService.sendNotification(command);

            // then
            verify(deleteDeviceTokenPort).deleteById(1L);
        }

        @Test
        @DisplayName("활성 디바이스 토큰이 없으면 FAILED 상태를 반환한다")
        void shouldReturnFailedWhenNoDeviceTokens() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateAndSave(template);
            when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(List.of());

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("FAILED");
            verifyNoInteractions(sendPushNotificationPort);
        }
    }

    @Nested
    @DisplayName("배송 채널별 분기")
    class DeliveryChannelRouting {

        @Test
        @DisplayName("IN_APP_ONLY 채널이면 FCM 발송을 수행하지 않고 PENDING 상태를 유지한다")
        void shouldNotSendPushForInAppOnly() {
            // given
            SendNotificationCommand command = createCommand("IN_APP_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateFound(template);
            when(saveNotificationPort.save(any(Notification.class)))
                    .thenAnswer(invocation -> withId(invocation.getArgument(0), 100L));

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("PENDING");
            verifyNoInteractions(findDeviceTokenPort);
            verifyNoInteractions(sendPushNotificationPort);
        }
    }

    @Nested
    @DisplayName("템플릿 렌더링")
    class TemplateRendering {

        @Test
        @DisplayName("템플릿 변수가 정상적으로 렌더링된다")
        void shouldRenderTemplateVariables() {
            // given
            SendNotificationCommand command = new SendNotificationCommand(
                    USER_ID, TEMPLATE_CODE,
                    Map.of("orderNumber", "ORD-001", "amount", "50,000원"),
                    "PUSH_ONLY", null
            );
            NotificationTemplate template = createTemplateWithBody(
                    NotificationCategory.MANDATORY,
                    "주문 완료",
                    "주문번호 {orderNumber}이 {amount}에 결제되었습니다.",
                    "/order/{orderNumber}"
            );
            setupTemplateFound(template);
            when(saveNotificationPort.save(any(Notification.class)))
                    .thenAnswer(invocation -> {
                        Notification notification = invocation.getArgument(0);
                        assertThat(notification.getTitle()).isEqualTo("주문 완료");
                        assertThat(notification.getBody()).isEqualTo("주문번호 ORD-001이 50,000원에 결제되었습니다.");
                        assertThat(notification.getLandingUrl()).isEqualTo("/order/ORD-001");
                        return withId(notification, 100L);
                    });
            setupDeviceTokensAndPush(1, true);

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("SENT");
        }
    }

    // --- Helper Methods ---

    private SendNotificationCommand createCommand(String deliveryChannel) {
        return new SendNotificationCommand(
                USER_ID, TEMPLATE_CODE, Map.of(), deliveryChannel, null
        );
    }

    private NotificationTemplate createTemplate(NotificationCategory category) {
        return createTemplateWithBody(category, "알림 제목", "알림 본문", "/test");
    }

    private NotificationTemplate createTemplateWithBody(NotificationCategory category,
                                                         String title, String bodyTemplate, String urlTemplate) {
        return NotificationTemplate.from(
                NotificationTemplateSnapshotState.builder()
                        .id(1L)
                        .templateCode(TEMPLATE_CODE)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .notificationCategory(category)
                        .title(title)
                        .bodyTemplate(bodyTemplate)
                        .urlTemplate(urlTemplate)
                        .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }

    private void setupTemplateFound(NotificationTemplate template) {
        when(findNotificationTemplatePort.findActiveByTemplateCode(TEMPLATE_CODE))
                .thenReturn(Optional.of(template));
    }

    private void setupTemplateAndSave(NotificationTemplate template) {
        setupTemplateFound(template);
        when(saveNotificationPort.save(any(Notification.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 100L));
    }

    private void setupPreferenceEnabled() {
        NotificationPreference preference = mock(NotificationPreference.class);
        when(preference.isEnabled()).thenReturn(true);
        when(findNotificationPreferencePort.findByUserIdAndNotificationType(USER_ID, NotificationType.ORDER_PAYMENT_COMPLETED))
                .thenReturn(Optional.of(preference));
    }

    private void setupPreferenceDisabled() {
        NotificationPreference preference = mock(NotificationPreference.class);
        when(preference.isEnabled()).thenReturn(false);
        when(findNotificationPreferencePort.findByUserIdAndNotificationType(USER_ID, NotificationType.ORDER_PAYMENT_COMPLETED))
                .thenReturn(Optional.of(preference));
    }

    private void setupDeviceTokensAndPush(int count, boolean success) {
        List<DeviceToken> tokens = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            tokens.add(createDeviceToken((long) i, "token" + i, Platform.ANDROID));
        }
        when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(tokens);
        if (success) {
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.success("msg-id"));
        }
    }

    private DeviceToken createDeviceToken(Long id, String token, Platform platform) {
        return DeviceToken.from(
                com.personal.marketnote.notification.domain.device.DeviceTokenSnapshotState.builder()
                        .id(id)
                        .userId(USER_ID)
                        .token(token)
                        .platform(platform)
                        .deviceId("device-" + id)
                        .lastUsedAt(LocalDateTime.now())
                        .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }

    private Notification withId(Notification notification, Long id) {
        return Notification.from(
                NotificationSnapshotState.builder()
                        .id(id)
                        .userId(notification.getUserId())
                        .notificationType(notification.getNotificationType())
                        .title(notification.getTitle())
                        .body(notification.getBody())
                        .data(notification.getData())
                        .deliveryChannel(notification.getDeliveryChannel())
                        .isRead(notification.isRead())
                        .landingUrl(notification.getLandingUrl())
                        .sendStatus(notification.getSendStatus())
                        .scheduledAt(notification.getScheduledAt())
                        .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }
}
