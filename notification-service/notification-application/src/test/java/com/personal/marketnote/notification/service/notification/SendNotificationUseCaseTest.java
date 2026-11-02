package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.notification.NotificationSnapshotState;
import com.personal.marketnote.notification.domain.notification.SendStatus;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.template.*;
import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;
import com.personal.marketnote.notification.port.in.event.PushNotificationRequestedEvent;
import com.personal.marketnote.notification.port.in.result.notification.SendNotificationResult;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SaveNotificationPort;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private SaveNotificationPort saveNotificationPort;

    @Mock
    private FindNotificationPort findNotificationPort;

    @Mock
    private PublishSseEventPort publishSseEventPort;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

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
        @DisplayName("MANDATORY 카테고리는 수신 설정 조회 없이 PENDING 상태로 저장 후 FCM 이벤트를 발행한다")
        void shouldPublishEventWithoutConsentCheckForMandatory() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateAndSave(template);

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("PENDING");
            verifyNoInteractions(findNotificationPreferencePort);
            verify(applicationEventPublisher).publishEvent(any(PushNotificationRequestedEvent.class));
        }

        @Test
        @DisplayName("INFORMATIONAL 카테고리는 수신 설정 조회 없이 PENDING 상태로 저장 후 FCM 이벤트를 발행한다")
        void shouldPublishEventWithoutConsentCheckForInformational() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.INFORMATIONAL);
            setupTemplateAndSave(template);

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("PENDING");
            verifyNoInteractions(findNotificationPreferencePort);
            verify(applicationEventPublisher).publishEvent(any(PushNotificationRequestedEvent.class));
        }

        @Test
        @DisplayName("PROMOTIONAL 카테고리에서 수신 동의 상태면 광고 라벨 적용 후 PENDING 상태로 저장한다")
        void shouldSaveWithAdLabelForConsentedPromotional() {
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

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("PENDING");
            verify(applicationEventPublisher).publishEvent(any(PushNotificationRequestedEvent.class));
        }

        @Test
        @DisplayName("PROMOTIONAL 카테고리에서 수신 미동의(enabled=false)이면 SKIPPED 반환하고 이벤트를 발행하지 않는다")
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
            verifyNoInteractions(applicationEventPublisher);
        }

        @Test
        @DisplayName("PROMOTIONAL 카테고리에서 수신 설정 미존재이면 SKIPPED 반환하고 이벤트를 발행하지 않는다")
        void shouldReturnSkippedWhenPreferenceNotFound() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.PROMOTIONAL);
            setupTemplateFound(template);
            when(findNotificationPreferencePort.findByUserIdAndNotificationType(
                    USER_ID, NotificationType.ORDER_PAYMENT_COMPLETED
            )).thenReturn(Optional.empty());
            when(saveNotificationPort.save(any(Notification.class)))
                    .thenAnswer(invocation -> withId(invocation.getArgument(0), 100L));

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("SKIPPED");
            verifyNoInteractions(applicationEventPublisher);
        }
    }

    @Nested
    @DisplayName("야간 광고성 알림 지연")
    class NightTimeDelay {

        @Test
        @DisplayName("PROMOTIONAL 카테고리 야간 시간대면 SCHEDULED 상태로 저장하고 이벤트를 발행하지 않는다")
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
            verifyNoInteractions(applicationEventPublisher);
        }
    }

    @Nested
    @DisplayName("배송 채널별 분기")
    class DeliveryChannelRouting {

        @Test
        @DisplayName("IN_APP_ONLY 채널이면 FCM 이벤트를 발행하지 않고 PENDING 상태를 유지한다")
        void shouldNotPublishEventForInAppOnly() {
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
            verifyNoInteractions(applicationEventPublisher);
        }
    }

    @Nested
    @DisplayName("FCM 발송 이벤트 발행")
    class PushEventPublish {

        @Test
        @DisplayName("PUSH_ONLY 채널이면 저장 후 PushNotificationRequestedEvent를 발행한다")
        void shouldPublishEventForPushOnly() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateAndSave(template);

            // when
            sendNotificationService.sendNotification(command);

            // then
            ArgumentCaptor<PushNotificationRequestedEvent> captor =
                    ArgumentCaptor.forClass(PushNotificationRequestedEvent.class);
            verify(applicationEventPublisher).publishEvent(captor.capture());

            PushNotificationRequestedEvent event = captor.getValue();
            assertThat(event.notificationId()).isEqualTo(100L);
            assertThat(event.userId()).isEqualTo(USER_ID);
            assertThat(event.title()).isEqualTo("알림 제목");
            assertThat(event.body()).isEqualTo("알림 본문");
            assertThat(event.landingUrl()).isEqualTo("/test");
        }

        @Test
        @DisplayName("이벤트 발행 시점에는 FCM 호출이 발생하지 않는다(트랜잭션 내부에서 외부 API 호출 분리)")
        void shouldNotInvokeFcmInsideServiceTransaction() {
            // given
            SendNotificationCommand command = createCommand("PUSH_ONLY");
            NotificationTemplate template = createTemplate(NotificationCategory.MANDATORY);
            setupTemplateAndSave(template);

            // when
            SendNotificationResult result = sendNotificationService.sendNotification(command);

            // then
            assertThat(result.sendStatus()).isEqualTo("PENDING");
            verify(applicationEventPublisher).publishEvent(any(PushNotificationRequestedEvent.class));
        }
    }

    @Nested
    @DisplayName("템플릿 렌더링")
    class TemplateRendering {

        @Test
        @DisplayName("템플릿 변수가 정상적으로 렌더링되고 이벤트에 반영된다")
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

            // when
            sendNotificationService.sendNotification(command);

            // then
            ArgumentCaptor<PushNotificationRequestedEvent> captor =
                    ArgumentCaptor.forClass(PushNotificationRequestedEvent.class);
            verify(applicationEventPublisher).publishEvent(captor.capture());
            PushNotificationRequestedEvent event = captor.getValue();
            assertThat(event.title()).isEqualTo("주문 완료");
            assertThat(event.body()).isEqualTo("주문번호 ORD-001이 50,000원에 결제되었습니다.");
            assertThat(event.landingUrl()).isEqualTo("/order/ORD-001");
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

    private NotificationTemplate createTemplateWithBody(
            NotificationCategory category, String title, String bodyTemplate, String urlTemplate
    ) {
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
        when(findNotificationPreferencePort.findByUserIdAndNotificationType(
                USER_ID, NotificationType.ORDER_PAYMENT_COMPLETED
        )).thenReturn(Optional.of(preference));
    }

    private void setupPreferenceDisabled() {
        NotificationPreference preference = mock(NotificationPreference.class);
        when(preference.isEnabled()).thenReturn(false);
        when(findNotificationPreferencePort.findByUserIdAndNotificationType(
                USER_ID, NotificationType.ORDER_PAYMENT_COMPLETED
        )).thenReturn(Optional.of(preference));
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
