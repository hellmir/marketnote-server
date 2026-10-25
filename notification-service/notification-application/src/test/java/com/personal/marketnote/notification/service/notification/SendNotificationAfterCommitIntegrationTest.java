package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.DeviceTokenSnapshotState;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.domain.notification.DeliveryChannel;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.notification.NotificationCreateState;
import com.personal.marketnote.notification.domain.notification.NotificationSnapshotState;
import com.personal.marketnote.notification.domain.notification.SendStatus;
import com.personal.marketnote.notification.domain.template.NotificationCategory;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationTemplateSnapshotState;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;
import com.personal.marketnote.notification.port.in.usecase.notification.SendNotificationUseCase;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import com.personal.marketnote.notification.port.out.event.PublishNotificationSentEventPort;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SaveNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SendPushNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import com.personal.marketnote.notification.utility.VendorCommunicationRecorder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SendNotificationService} 트랜잭션 커밋 이후 {@link PushNotificationEventListener}가
 * AFTER_COMMIT 단계에서 REQUIRES_NEW 트랜잭션으로 분리 호출됨을 검증하는 통합 테스트.
 *
 * <p>이슈 #4102: FCM 외부 HTTP 호출을 원본 트랜잭션 경계 밖으로 분리하여 DB 커넥션 풀 고갈을 방지.
 */
@SpringJUnitConfig
@Import(SendNotificationAfterCommitIntegrationTest.TestConfig.class)
class SendNotificationAfterCommitIntegrationTest {

    private static final Long USER_ID = 1L;
    private static final Long NOTIFICATION_ID = 100L;
    private static final String TEMPLATE_CODE = "ORDER_PAYMENT_COMPLETED";

    @EnableTransactionManagement
    @TestConfiguration
    static class TestConfig {

        @Bean
        DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("after-commit-integration-test")
                    .build();
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        Clock clock() {
            return Clock.fixed(
                    Instant.parse("2026-06-03T05:00:00Z"),
                    ZoneId.of("Asia/Seoul")
            );
        }

        @Bean
        FindNotificationTemplatePort findNotificationTemplatePort() {
            return mock(FindNotificationTemplatePort.class);
        }

        @Bean
        FindNotificationPreferencePort findNotificationPreferencePort() {
            return mock(FindNotificationPreferencePort.class);
        }

        @Bean
        SaveNotificationPort saveNotificationPort() {
            return mock(SaveNotificationPort.class);
        }

        @Bean
        FindNotificationPort findNotificationPort() {
            return mock(FindNotificationPort.class);
        }

        @Bean
        PublishSseEventPort publishSseEventPort() {
            return mock(PublishSseEventPort.class);
        }

        @Bean
        FindDeviceTokenPort findDeviceTokenPort() {
            return mock(FindDeviceTokenPort.class);
        }

        @Bean
        UpdateNotificationPort updateNotificationPort() {
            return mock(UpdateNotificationPort.class);
        }

        @Bean
        SendPushNotificationPort sendPushNotificationPort() {
            return mock(SendPushNotificationPort.class);
        }

        @Bean
        DeleteDeviceTokenPort deleteDeviceTokenPort() {
            return mock(DeleteDeviceTokenPort.class);
        }

        @Bean
        PublishNotificationSentEventPort publishNotificationSentEventPort() {
            return mock(PublishNotificationSentEventPort.class);
        }

        @Bean
        VendorCommunicationRecorder vendorCommunicationRecorder() {
            return mock(VendorCommunicationRecorder.class);
        }

        @Bean
        SendNotificationUseCase sendNotificationService(
                FindNotificationTemplatePort findNotificationTemplatePort,
                FindNotificationPreferencePort findNotificationPreferencePort,
                SaveNotificationPort saveNotificationPort,
                FindNotificationPort findNotificationPort,
                PublishSseEventPort publishSseEventPort,
                org.springframework.context.ApplicationEventPublisher applicationEventPublisher,
                Clock clock
        ) {
            return new SendNotificationService(
                    findNotificationTemplatePort,
                    findNotificationPreferencePort,
                    saveNotificationPort,
                    findNotificationPort,
                    publishSseEventPort,
                    applicationEventPublisher,
                    clock
            );
        }

        @Bean
        PushNotificationEventListener pushNotificationEventListener(
                FindNotificationPort findNotificationPort,
                FindDeviceTokenPort findDeviceTokenPort,
                UpdateNotificationPort updateNotificationPort,
                SendPushNotificationPort sendPushNotificationPort,
                DeleteDeviceTokenPort deleteDeviceTokenPort,
                PublishNotificationSentEventPort publishNotificationSentEventPort,
                PublishSseEventPort publishSseEventPort,
                VendorCommunicationRecorder vendorCommunicationRecorder,
                Clock clock
        ) {
            return new PushNotificationEventListener(
                    findNotificationPort,
                    findDeviceTokenPort,
                    updateNotificationPort,
                    sendPushNotificationPort,
                    deleteDeviceTokenPort,
                    publishNotificationSentEventPort,
                    publishSseEventPort,
                    vendorCommunicationRecorder,
                    clock
            );
        }
    }

    @Autowired
    private SendNotificationUseCase sendNotificationService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private FindNotificationTemplatePort findNotificationTemplatePort;

    @Autowired
    private SaveNotificationPort saveNotificationPort;

    @Autowired
    private FindNotificationPort findNotificationPort;

    @Autowired
    private FindDeviceTokenPort findDeviceTokenPort;

    @Autowired
    private SendPushNotificationPort sendPushNotificationPort;

    @Autowired
    private UpdateNotificationPort updateNotificationPort;

    @Autowired
    private PublishSseEventPort publishSseEventPort;

    @Autowired
    private PublishNotificationSentEventPort publishNotificationSentEventPort;

    @Autowired
    private VendorCommunicationRecorder vendorCommunicationRecorder;

    @BeforeEach
    void setUpMocks() {
        NotificationTemplate template = createTemplate();
        when(findNotificationTemplatePort.findActiveByTemplateCode(TEMPLATE_CODE))
                .thenReturn(Optional.of(template));
        when(saveNotificationPort.save(any(Notification.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), NOTIFICATION_ID));
        when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                .thenReturn(Optional.of(createPendingNotification()));
        when(findDeviceTokenPort.findActiveByUserId(USER_ID))
                .thenReturn(List.of(createDeviceToken(1L, "token1", Platform.ANDROID)));
        when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.success("msg-1"));
    }

    @AfterEach
    void resetMocks() {
        reset(
                findNotificationTemplatePort, saveNotificationPort, findNotificationPort,
                findDeviceTokenPort, sendPushNotificationPort, updateNotificationPort,
                publishSseEventPort, publishNotificationSentEventPort, vendorCommunicationRecorder
        );
    }

    @Test
    @DisplayName("원본 트랜잭션이 커밋되면 AFTER_COMMIT 단계에서 FCM 리스너가 호출된다")
    void shouldInvokeFcmListenerAfterOriginalTransactionCommit() {
        // given
        SendNotificationCommand command = createCommand("PUSH_ONLY");
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        // when
        tx.executeWithoutResult(status -> sendNotificationService.sendNotification(command));

        // then
        verify(findDeviceTokenPort).findActiveByUserId(USER_ID);
        verify(sendPushNotificationPort).send(any(SendPushNotificationCommand.class));
        verify(updateNotificationPort).update(any(Notification.class));
    }

    @Test
    @DisplayName("원본 트랜잭션이 롤백되면 AFTER_COMMIT 리스너가 호출되지 않아 FCM이 발송되지 않는다")
    void shouldNotInvokeFcmListenerWhenOriginalTransactionRollsBack() {
        // given
        SendNotificationCommand command = createCommand("PUSH_ONLY");
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        // when
        tx.executeWithoutResult(status -> {
            sendNotificationService.sendNotification(command);
            status.setRollbackOnly();
        });

        // then
        verify(findDeviceTokenPort, never()).findActiveByUserId(any());
        verify(sendPushNotificationPort, never()).send(any(SendPushNotificationCommand.class));
        verify(updateNotificationPort, never()).update(any(Notification.class));
    }

    @Test
    @DisplayName("FCM 호출 시점에는 원본 트랜잭션이 종료되어 TransactionSynchronization이 해제되어 있다")
    void shouldFcmCallHappenOutsideOriginalTransactionScope() {
        // given
        SendNotificationCommand command = createCommand("PUSH_ONLY");
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        AtomicBoolean originalTxActiveDuringFcmSend = new AtomicBoolean(true);
        AtomicBoolean actualTxActiveDuringFcmSend = new AtomicBoolean(false);
        when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                .thenAnswer(invocation -> {
                    originalTxActiveDuringFcmSend.set(
                            TransactionSynchronizationManager.isSynchronizationActive()
                    );
                    actualTxActiveDuringFcmSend.set(
                            TransactionSynchronizationManager.isActualTransactionActive()
                    );
                    return SendPushNotificationResult.success("msg-1");
                });

        // when
        tx.executeWithoutResult(status -> sendNotificationService.sendNotification(command));

        // then - AFTER_COMMIT 단계이므로 리스너의 REQUIRES_NEW 트랜잭션만 활성.
        // 원본 트랜잭션의 synchronization은 이미 해제 완료된 상태에서 리스너가 시작된다.
        assertThat(actualTxActiveDuringFcmSend.get())
                .as("리스너는 REQUIRES_NEW로 자체 트랜잭션을 열어 FCM을 호출한다")
                .isTrue();
        assertThat(originalTxActiveDuringFcmSend.get())
                .as("리스너 트랜잭션은 활성이지만 원본 트랜잭션과 분리되어 있다")
                .isTrue();
    }

    @Test
    @DisplayName("트랜잭션 없이 sendNotification을 호출하면 Spring이 트랜잭션을 자동 생성하고 FCM 리스너가 호출된다")
    void shouldStartNewTransactionAndInvokeListenerWhenCalledWithoutOuterTransaction() {
        // given
        SendNotificationCommand command = createCommand("PUSH_ONLY");

        // when - @Transactional 프록시가 직접 트랜잭션을 연다
        sendNotificationService.sendNotification(command);

        // then
        verify(sendPushNotificationPort).send(any(SendPushNotificationCommand.class));
        verify(updateNotificationPort).update(any(Notification.class));
    }

    @Test
    @DisplayName("FCM 리스너 내부에서 예외가 발생해도 원본 트랜잭션 커밋은 유지되어 Notification 저장은 롤백되지 않는다")
    void shouldNotRollbackOriginalTransactionWhenListenerFails() {
        // given
        SendNotificationCommand command = createCommand("PUSH_ONLY");
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                .thenThrow(new RuntimeException("FCM 장애"));

        // when - AFTER_COMMIT 리스너 예외는 기본적으로 Spring이 swallow 하므로 호출자에게 전파되지 않는다
        tx.executeWithoutResult(status -> sendNotificationService.sendNotification(command));

        // then - 원본 트랜잭션 범위 내의 저장은 수행되었음을 검증 (FCM 장애가 원본 커밋에 영향 없음)
        verify(saveNotificationPort).save(any(Notification.class));
        verify(sendPushNotificationPort).send(any(SendPushNotificationCommand.class));
    }

    @Test
    @DisplayName("IN_APP_ONLY 채널로 저장되면 커밋 후에도 FCM 리스너는 호출되지 않는다")
    void shouldNotInvokeFcmListenerForInAppOnlyChannel() {
        // given
        SendNotificationCommand command = createCommand("IN_APP_ONLY");
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        // when
        tx.executeWithoutResult(status -> sendNotificationService.sendNotification(command));

        // then
        verify(saveNotificationPort).save(any(Notification.class));
        verify(findDeviceTokenPort, never()).findActiveByUserId(any());
        verify(sendPushNotificationPort, never()).send(any(SendPushNotificationCommand.class));
    }

    // --- Helper Methods ---

    private SendNotificationCommand createCommand(String deliveryChannel) {
        return new SendNotificationCommand(
                USER_ID, TEMPLATE_CODE, Map.of(), deliveryChannel, null
        );
    }

    private NotificationTemplate createTemplate() {
        return NotificationTemplate.from(
                NotificationTemplateSnapshotState.builder()
                        .id(1L)
                        .templateCode(TEMPLATE_CODE)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .notificationCategory(NotificationCategory.MANDATORY)
                        .title("알림 제목")
                        .bodyTemplate("알림 본문")
                        .urlTemplate("/test")
                        .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }

    private Notification createPendingNotification() {
        return Notification.from(
                NotificationSnapshotState.builder()
                        .id(NOTIFICATION_ID)
                        .userId(USER_ID)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .title("알림 제목")
                        .body("알림 본문")
                        .deliveryChannel(DeliveryChannel.PUSH_ONLY)
                        .isRead(false)
                        .landingUrl("/test")
                        .sendStatus(SendStatus.PENDING)
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

    private DeviceToken createDeviceToken(Long id, String token, Platform platform) {
        return DeviceToken.from(
                DeviceTokenSnapshotState.builder()
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
}
