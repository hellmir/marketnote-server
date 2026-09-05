package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.DeviceTokenSnapshotState;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.domain.notification.*;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.preference.NotificationPreferenceSnapshotState;
import com.personal.marketnote.notification.domain.template.*;
import com.personal.marketnote.notification.port.in.command.SendBatchNotificationCommand;
import com.personal.marketnote.notification.port.in.result.notification.SendBatchNotificationResult;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import com.personal.marketnote.notification.port.out.notification.SaveNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SendPushNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import com.personal.marketnote.notification.port.out.result.SendBatchPushNotificationResult;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import com.personal.marketnote.notification.domain.notification.InvalidNotificationException;

@ExtendWith(MockitoExtension.class)
class SendBatchNotificationUseCaseTest {

    @InjectMocks
    private SendBatchNotificationService sendBatchNotificationService;

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
    private com.personal.marketnote.notification.port.out.event.PublishNotificationSentEventPort publishNotificationSentEventPort;

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-09-03T05:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Captor
    private ArgumentCaptor<List<Notification>> notificationListCaptor;

    @Captor
    private ArgumentCaptor<List<SendPushNotificationCommand>> pushCommandListCaptor;

    private static final String TEMPLATE_CODE = "NOTICE_BROADCAST";

    @Nested
    @DisplayName("템플릿 조회")
    class TemplateNotFound {

        @Test
        @DisplayName("템플릿이 존재하지 않으면 NotificationTemplateNotFoundException이 발생한다")
        void shouldThrowWhenTemplateNotFound() {
            // given
            SendBatchNotificationCommand command = createCommand(List.of(1L, 2L));
            when(findNotificationTemplatePort.findActiveByTemplateCode(TEMPLATE_CODE))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sendBatchNotificationService.sendBatchNotification(command))
                    .isInstanceOf(NotificationTemplateNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("입력 검증")
    class InputValidation {

        @Test
        @DisplayName("userIds가 10,000명을 초과하면 InvalidNotificationException이 발생한다")
        void shouldThrowWhenUserIdsExceedMaxBatchSize() {
            // given
            List<Long> largeUserIds = java.util.stream.LongStream.rangeClosed(1, 10_001)
                    .boxed().toList();
            SendBatchNotificationCommand command = createCommand(largeUserIds);
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateFound(template);

            // when & then
            assertThatThrownBy(() -> sendBatchNotificationService.sendBatchNotification(command))
                    .isInstanceOf(InvalidNotificationException.class)
                    .hasMessageContaining("10000");
        }
    }

    @Nested
    @DisplayName("정상 발송")
    class SuccessfulSending {

        @Test
        @DisplayName("3명에게 대량 발송 시 모든 사용자에게 발송 성공한다")
        void shouldSendToAllUsersSuccessfully() {
            // given
            List<Long> userIds = List.of(1L, 2L, 3L);
            SendBatchNotificationCommand command = createCommand(userIds);
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateFound(template);
            setupSaveAllReturnsWithIds();
            setupDeviceTokensForUsers(userIds, 1);
            setupBatchPushSuccess(3);

            // when
            SendBatchNotificationResult result = sendBatchNotificationService.sendBatchNotification(command);

            // then
            assertThat(result.totalUserCount()).isEqualTo(3);
            assertThat(result.sentUserCount()).isEqualTo(3);
            assertThat(result.skippedUserCount()).isZero();
            assertThat(result.failedUserCount()).isZero();
            assertThat(result.totalDeviceSentCount()).isEqualTo(3);
            assertThat(result.totalDeviceFailedCount()).isZero();
        }

        @Test
        @DisplayName("각 사용자가 2개 디바이스를 보유하면 6건 발송 성공한다")
        void shouldSendToAllDevicesPerUser() {
            // given
            List<Long> userIds = List.of(1L, 2L);
            SendBatchNotificationCommand command = createCommand(userIds);
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateFound(template);
            setupSaveAllReturnsWithIds();
            setupDeviceTokensForUsers(userIds, 2);
            setupBatchPushSuccess(4);

            // when
            SendBatchNotificationResult result = sendBatchNotificationService.sendBatchNotification(command);

            // then
            assertThat(result.totalUserCount()).isEqualTo(2);
            assertThat(result.sentUserCount()).isEqualTo(2);
            assertThat(result.totalDeviceSentCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("빈 userId 목록이면 빈 결과를 반환한다")
        void shouldReturnEmptyResultForEmptyUserIds() {
            // given
            SendBatchNotificationCommand command = createCommand(List.of());
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateFound(template);

            // when
            SendBatchNotificationResult result = sendBatchNotificationService.sendBatchNotification(command);

            // then
            assertThat(result.totalUserCount()).isZero();
            assertThat(result.sentUserCount()).isZero();
            verifyNoInteractions(findDeviceTokenPort);
            verifyNoInteractions(sendPushNotificationPort);
        }
    }

    @Nested
    @DisplayName("수신 동의 필터링")
    class ConsentFiltering {

        @Test
        @DisplayName("MANDATORY 카테고리는 수신 설정 조회 없이 전원 발송한다")
        void shouldSendToAllForMandatory() {
            // given
            List<Long> userIds = List.of(1L, 2L, 3L);
            SendBatchNotificationCommand command = createCommand(userIds);
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateFound(template);
            setupSaveAllReturnsWithIds();
            setupDeviceTokensForUsers(userIds, 1);
            setupBatchPushSuccess(3);

            // when
            SendBatchNotificationResult result = sendBatchNotificationService.sendBatchNotification(command);

            // then
            assertThat(result.sentUserCount()).isEqualTo(3);
            assertThat(result.skippedUserCount()).isZero();
            verify(findNotificationPreferencePort, never())
                    .findEnabledByUserIdsAndNotificationType(anyList(), any());
        }

        @Test
        @DisplayName("PROMOTIONAL 카테고리에서 수신 거부 사용자는 SKIPPED 처리된다")
        void shouldSkipDisabledUsersForPromotional() {
            // given
            List<Long> userIds = List.of(1L, 2L, 3L);
            SendBatchNotificationCommand command = createCommand(userIds);
            NotificationTemplate template = createTemplate(NotificationCategory.PROMOTIONAL);
            setupTemplateFound(template);

            List<NotificationPreference> enabledPrefs = List.of(
                    createPreference(1L, true),
                    createPreference(3L, true)
            );
            when(findNotificationPreferencePort.findEnabledByUserIdsAndNotificationType(
                    userIds, NotificationType.ORDER_PAYMENT_COMPLETED))
                    .thenReturn(enabledPrefs);

            setupSaveAllReturnsWithIds();
            setupDeviceTokensForUsers(List.of(1L, 3L), 1);
            setupBatchPushSuccess(2);

            // when
            SendBatchNotificationResult result = sendBatchNotificationService.sendBatchNotification(command);

            // then
            assertThat(result.totalUserCount()).isEqualTo(3);
            assertThat(result.sentUserCount()).isEqualTo(2);
            assertThat(result.skippedUserCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("PROMOTIONAL 카테고리에서 전원 수신 거부하면 전원 SKIPPED 처리된다")
        void shouldSkipAllWhenNoneConsented() {
            // given
            List<Long> userIds = List.of(1L, 2L);
            SendBatchNotificationCommand command = createCommand(userIds);
            NotificationTemplate template = createTemplate(NotificationCategory.PROMOTIONAL);
            setupTemplateFound(template);

            when(findNotificationPreferencePort.findEnabledByUserIdsAndNotificationType(
                    userIds, NotificationType.ORDER_PAYMENT_COMPLETED))
                    .thenReturn(List.of());

            setupSaveAllReturnsWithIds();

            // when
            SendBatchNotificationResult result = sendBatchNotificationService.sendBatchNotification(command);

            // then
            assertThat(result.totalUserCount()).isEqualTo(2);
            assertThat(result.skippedUserCount()).isEqualTo(2);
            assertThat(result.sentUserCount()).isZero();
            verifyNoInteractions(findDeviceTokenPort);
            verifyNoInteractions(sendPushNotificationPort);
        }
    }

    @Nested
    @DisplayName("광고성 정책 적용")
    class PromotionalPolicy {

        @Test
        @DisplayName("PROMOTIONAL 카테고리 알림에 광고 라벨이 적용된다")
        void shouldApplyAdLabelForPromotional() {
            // given
            List<Long> userIds = List.of(1L);
            SendBatchNotificationCommand command = createCommand(userIds);
            NotificationTemplate template = createTemplate(NotificationCategory.PROMOTIONAL);
            setupTemplateFound(template);
            setupConsentForUsers(userIds);
            setupSaveAllReturnsWithIds();
            setupDeviceTokensForUsers(userIds, 1);
            setupBatchPushSuccess(1);

            // when
            sendBatchNotificationService.sendBatchNotification(command);

            // then
            verify(saveNotificationPort).saveAll(notificationListCaptor.capture());
            List<Notification> saved = notificationListCaptor.getValue();
            assertThat(saved).hasSize(1);
            assertThat(saved.get(0).getTitle()).startsWith("(광고) ");
            assertThat(saved.get(0).getBody()).endsWith("\n[수신거부:더보기>설정]");
        }
    }

    @Nested
    @DisplayName("야간 발송 지연")
    class NightTimeDelay {

        @Test
        @DisplayName("PROMOTIONAL 카테고리 야간 시간대면 SCHEDULED 상태로 저장하고 FCM 발송하지 않는다")
        void shouldScheduleForPromotionalAtNight() {
            // given
            Clock nightClock = Clock.fixed(
                    Instant.parse("2026-09-03T13:00:00Z"),
                    ZoneId.of("Asia/Seoul")
            );
            when(clock.instant()).thenReturn(nightClock.instant());
            when(clock.getZone()).thenReturn(nightClock.getZone());

            List<Long> userIds = List.of(1L);
            SendBatchNotificationCommand command = createCommand(userIds);
            NotificationTemplate template = createTemplate(NotificationCategory.PROMOTIONAL);
            setupTemplateFound(template);
            setupConsentForUsers(userIds);
            setupSaveAllReturnsWithIds();

            // when
            SendBatchNotificationResult result = sendBatchNotificationService.sendBatchNotification(command);

            // then
            verify(saveNotificationPort).saveAll(notificationListCaptor.capture());
            List<Notification> saved = notificationListCaptor.getValue();
            assertThat(saved.get(0).getSendStatus()).isEqualTo(SendStatus.SCHEDULED);
            assertThat(result.sentUserCount()).isZero();
            verifyNoInteractions(sendPushNotificationPort);
        }
    }

    @Nested
    @DisplayName("FCM 발송 결과 처리")
    class PushResultHandling {

        @Test
        @DisplayName("부분 실패 시 성공 디바이스는 SENT, 전원 실패 사용자는 FAILED 처리된다")
        void shouldHandlePartialFailure() {
            // given
            List<Long> userIds = List.of(1L, 2L);
            SendBatchNotificationCommand command = createCommand(userIds);
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateFound(template);
            setupSaveAllReturnsWithIds();

            List<DeviceToken> tokens = List.of(
                    createDeviceToken(10L, 1L, "token-1", Platform.ANDROID),
                    createDeviceToken(20L, 2L, "token-2", Platform.IOS)
            );
            when(findDeviceTokenPort.findActiveByUserIds(userIds)).thenReturn(tokens);

            SendBatchPushNotificationResult batchResult = new SendBatchPushNotificationResult(
                    1, 1,
                    List.of(new SendBatchPushNotificationResult.FailedToken("token-2", "INTERNAL", false))
            );
            when(sendPushNotificationPort.sendBatch(anyList())).thenReturn(batchResult);

            // when
            SendBatchNotificationResult result = sendBatchNotificationService.sendBatchNotification(command);

            // then
            assertThat(result.totalDeviceSentCount()).isEqualTo(1);
            assertThat(result.totalDeviceFailedCount()).isEqualTo(1);
            assertThat(result.sentUserCount()).isEqualTo(1);
            assertThat(result.failedUserCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("tokenInvalid 토큰은 삭제된다")
        void shouldDeleteInvalidTokens() {
            // given
            List<Long> userIds = List.of(1L);
            SendBatchNotificationCommand command = createCommand(userIds);
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateFound(template);
            setupSaveAllReturnsWithIds();

            List<DeviceToken> tokens = List.of(
                    createDeviceToken(10L, 1L, "invalid-token", Platform.ANDROID)
            );
            when(findDeviceTokenPort.findActiveByUserIds(userIds)).thenReturn(tokens);

            SendBatchPushNotificationResult batchResult = new SendBatchPushNotificationResult(
                    0, 1,
                    List.of(new SendBatchPushNotificationResult.FailedToken("invalid-token", "UNREGISTERED", true))
            );
            when(sendPushNotificationPort.sendBatch(anyList())).thenReturn(batchResult);

            // when
            sendBatchNotificationService.sendBatchNotification(command);

            // then
            verify(deleteDeviceTokenPort).deleteById(10L);
        }

        @Test
        @DisplayName("디바이스 토큰이 없는 사용자는 FAILED 처리된다")
        void shouldFailUsersWithoutDeviceTokens() {
            // given
            List<Long> userIds = List.of(1L, 2L);
            SendBatchNotificationCommand command = createCommand(userIds);
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateFound(template);
            setupSaveAllReturnsWithIds();

            List<DeviceToken> tokens = List.of(
                    createDeviceToken(10L, 1L, "token-1", Platform.ANDROID)
            );
            when(findDeviceTokenPort.findActiveByUserIds(userIds)).thenReturn(tokens);
            setupBatchPushSuccess(1);

            // when
            SendBatchNotificationResult result = sendBatchNotificationService.sendBatchNotification(command);

            // then
            assertThat(result.sentUserCount()).isEqualTo(1);
            assertThat(result.failedUserCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("배송 채널 분기")
    class DeliveryChannelRouting {

        @Test
        @DisplayName("IN_APP_ONLY 채널이면 FCM 발송을 수행하지 않는다")
        void shouldNotSendPushForInAppOnly() {
            // given
            List<Long> userIds = List.of(1L, 2L);
            SendBatchNotificationCommand command = new SendBatchNotificationCommand(
                    userIds, TEMPLATE_CODE, Map.of(), "IN_APP_ONLY", null);
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateFound(template);
            setupSaveAllReturnsWithIds();

            // when
            SendBatchNotificationResult result = sendBatchNotificationService.sendBatchNotification(command);

            // then
            assertThat(result.totalUserCount()).isEqualTo(2);
            verifyNoInteractions(findDeviceTokenPort);
            verifyNoInteractions(sendPushNotificationPort);
        }
    }

    @Nested
    @DisplayName("Notification 상태 업데이트")
    class NotificationStatusUpdate {

        @Test
        @DisplayName("발송 완료 후 Notification 상태가 업데이트된다")
        void shouldUpdateNotificationStatusAfterSending() {
            // given
            List<Long> userIds = List.of(1L);
            SendBatchNotificationCommand command = createCommand(userIds);
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateFound(template);
            setupSaveAllReturnsWithIds();
            setupDeviceTokensForUsers(userIds, 1);
            setupBatchPushSuccess(1);

            // when
            sendBatchNotificationService.sendBatchNotification(command);

            // then
            verify(updateNotificationPort).updateAll(notificationListCaptor.capture());
            List<Notification> updated = notificationListCaptor.getValue();
            assertThat(updated).hasSize(1);
            assertThat(updated.get(0).getSendStatus()).isEqualTo(SendStatus.SENT);
        }
    }

    // --- Helper Methods ---

    private SendBatchNotificationCommand createCommand(List<Long> userIds) {
        return new SendBatchNotificationCommand(
                userIds, TEMPLATE_CODE, Map.of(), "PUSH_ONLY", null
        );
    }

    private NotificationTemplate createTemplate(NotificationCategory category) {
        return NotificationTemplate.from(
                NotificationTemplateSnapshotState.builder()
                        .id(1L)
                        .templateCode(TEMPLATE_CODE)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .notificationCategory(category)
                        .title("알림 제목")
                        .bodyTemplate("알림 본문")
                        .urlTemplate("/test")
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }

    private void setupTemplateFound(NotificationTemplate template) {
        when(findNotificationTemplatePort.findActiveByTemplateCode(TEMPLATE_CODE))
                .thenReturn(Optional.of(template));
    }

    private void setupSaveAllReturnsWithIds() {
        when(saveNotificationPort.saveAll(anyList()))
                .thenAnswer(invocation -> {
                    List<Notification> notifications = invocation.getArgument(0);
                    long idCounter = 100L;
                    return notifications.stream()
                            .map(n -> withId(n, idCounter + notifications.indexOf(n)))
                            .toList();
                });
    }

    private void setupDeviceTokensForUsers(List<Long> userIds, int devicesPerUser) {
        List<DeviceToken> tokens = new java.util.ArrayList<>();
        long tokenIdCounter = 10L;
        for (Long userId : userIds) {
            for (int i = 0; i < devicesPerUser; i++) {
                tokens.add(createDeviceToken(tokenIdCounter++, userId,
                        "token-" + userId + "-" + i, Platform.ANDROID));
            }
        }
        when(findDeviceTokenPort.findActiveByUserIds(userIds)).thenReturn(tokens);
    }

    private void setupBatchPushSuccess(int count) {
        SendBatchPushNotificationResult batchResult = new SendBatchPushNotificationResult(
                count, 0, List.of()
        );
        when(sendPushNotificationPort.sendBatch(anyList())).thenReturn(batchResult);
    }

    private void setupConsentForUsers(List<Long> userIds) {
        List<NotificationPreference> prefs = userIds.stream()
                .map(userId -> createPreference(userId, true))
                .toList();
        when(findNotificationPreferencePort.findEnabledByUserIdsAndNotificationType(
                userIds, NotificationType.ORDER_PAYMENT_COMPLETED))
                .thenReturn(prefs);
    }

    private NotificationPreference createPreference(Long userId, boolean enabled) {
        return NotificationPreference.from(
                NotificationPreferenceSnapshotState.builder()
                        .id(userId)
                        .userId(userId)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .enabled(enabled)
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }

    private DeviceToken createDeviceToken(Long id, Long userId, String token, Platform platform) {
        return DeviceToken.from(
                DeviceTokenSnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .token(token)
                        .platform(platform)
                        .deviceId("device-" + id)
                        .lastUsedAt(LocalDateTime.now())
                        .status(EntityStatus.ACTIVE)
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
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }
}
