package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.NoticeRegisteredEvent;
import com.personal.marketnote.notification.port.in.command.SendBatchNotificationCommand;
import com.personal.marketnote.notification.port.in.usecase.notification.SendBatchNotificationUseCase;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeRegisteredNotificationConsumerTest {

    @InjectMocks
    private NoticeRegisteredNotificationConsumer consumer;

    @Mock
    private SendBatchNotificationUseCase sendBatchNotificationUseCase;

    @Mock
    private FindNotificationPreferencePort findNotificationPreferencePort;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long postId, String title) {
        return buildRecord(postId, title, false);
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long postId, String title, boolean isImportant) {
        NoticeRegisteredEvent event = new NoticeRegisteredEvent(postId, title, isImportant);
        EventEnvelope<NoticeRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "community.notice.registered", "community-service",
                LocalDateTime.of(2026, 4, 10, 10, 0), event
        );
        return new ConsumerRecord<>("community.notice.registered", 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("중요 공지 등록 이벤트 수신 시 전체 사용자에게 배치 알림을 발송하고 acknowledge한다")
    void shouldSendBatchNotificationAndAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, "시스템 점검 안내", true);
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of(1L, 2L, 3L));

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendBatchNotificationCommand> captor = ArgumentCaptor.forClass(
                SendBatchNotificationCommand.class
        );
        verify(sendBatchNotificationUseCase).sendBatchNotification(captor.capture());

        SendBatchNotificationCommand command = captor.getValue();
        assertThat(command.userIds()).containsExactly(1L, 2L, 3L);
        assertThat(command.templateCode()).isEqualTo("NOTICE_REGISTERED");
        assertThat(command.deliveryChannel()).isEqualTo("PUSH_AND_IN_APP");
        assertThat(command.variables()).containsEntry("post_id", "100");
        assertThat(command.variables()).containsEntry("notice_title", "시스템 점검 안내");
        assertThat(command.scheduledAt()).isNull();

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("사용자 수가 10,000명을 초과하면 청크 분할하여 배치 발송한다")
    void shouldChunkBatchWhenUsersExceedMaxBatchSize() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, "대규모 공지");
        List<Long> largeUserIds = LongStream.rangeClosed(1, 15_000).boxed().toList();
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(largeUserIds);

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendBatchNotificationCommand> captor = ArgumentCaptor.forClass(
                SendBatchNotificationCommand.class
        );
        verify(sendBatchNotificationUseCase, times(2)).sendBatchNotification(captor.capture());

        List<SendBatchNotificationCommand> commands = captor.getAllValues();
        assertThat(commands.get(0).userIds()).hasSize(10_000);
        assertThat(commands.get(1).userIds()).hasSize(5_000);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEnvelopeIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "community.notice.registered", 0, 0L, "1", null
        );

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendBatchNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEventTypeMismatch() {
        // given
        NoticeRegisteredEvent event = new NoticeRegisteredEvent(100L, "공지사항", false);
        EventEnvelope<NoticeRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "community-service",
                LocalDateTime.of(2026, 4, 10, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "community.notice.registered", 0, 0L, "key-1", envelope
        );

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendBatchNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("postId가 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenPostIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, "공지사항");

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendBatchNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("title이 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenTitleIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, null);

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendBatchNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("배치 발송 중 예외가 발생해도 acknowledge한다")
    void shouldAcknowledgeEvenWhenBatchSendFails() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, "공지사항");
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of(1L, 2L));
        doThrow(new RuntimeException("발송 실패"))
                .when(sendBatchNotificationUseCase).sendBatchNotification(any());

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("사용자가 0명이면 배치 발송을 호출하지 않고 acknowledge한다")
    void shouldSkipBatchSendWhenNoUsers() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, "공지사항");
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of());

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendBatchNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중요 공지 등록 시 인앱 알림이 포함된 PUSH_AND_IN_APP 채널로 발송한다")
    void shouldSendWithPushAndInAppDeliveryChannel() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(200L, "서비스 업데이트 안내", true);
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of(10L, 20L));

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendBatchNotificationCommand> captor = ArgumentCaptor.forClass(
                SendBatchNotificationCommand.class
        );
        verify(sendBatchNotificationUseCase).sendBatchNotification(captor.capture());

        SendBatchNotificationCommand command = captor.getValue();
        assertThat(command.deliveryChannel()).isEqualTo("PUSH_AND_IN_APP");
        assertThat(command.variables()).containsEntry("post_id", "200");
        assertThat(command.variables()).containsEntry("notice_title", "서비스 업데이트 안내");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("공지사항 알림의 landingUrl 변수(post_id)가 올바르게 전달된다")
    void shouldPassCorrectPostIdForLandingUrl() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(999L, "긴급 공지");
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of(1L));

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendBatchNotificationCommand> captor = ArgumentCaptor.forClass(
                SendBatchNotificationCommand.class
        );
        verify(sendBatchNotificationUseCase).sendBatchNotification(captor.capture());

        SendBatchNotificationCommand command = captor.getValue();
        assertThat(command.variables()).containsEntry("post_id", "999");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("isImportant=true 이벤트 수신 시 deliveryChannel이 PUSH_AND_IN_APP으로 전달된다")
    void shouldSendPushAndInAppWhenIsImportantTrue() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(300L, "중요 공지", true);
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of(1L, 2L));

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendBatchNotificationCommand> captor = ArgumentCaptor.forClass(
                SendBatchNotificationCommand.class
        );
        verify(sendBatchNotificationUseCase, times(1)).sendBatchNotification(captor.capture());

        SendBatchNotificationCommand command = captor.getValue();
        assertThat(command.deliveryChannel()).isEqualTo("PUSH_AND_IN_APP");
        assertThat(command.templateCode()).isEqualTo("NOTICE_REGISTERED");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("isImportant=false 이벤트 수신 시 deliveryChannel이 IN_APP_ONLY로 전달된다")
    void shouldSendInAppOnlyWhenIsImportantFalse() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(301L, "일반 공지", false);
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of(1L, 2L));

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendBatchNotificationCommand> captor = ArgumentCaptor.forClass(
                SendBatchNotificationCommand.class
        );
        verify(sendBatchNotificationUseCase, times(1)).sendBatchNotification(captor.capture());

        SendBatchNotificationCommand command = captor.getValue();
        assertThat(command.deliveryChannel()).isEqualTo("IN_APP_ONLY");
        assertThat(command.templateCode()).isEqualTo("NOTICE_REGISTERED");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("isImportant=true 이벤트 수신 시 sendBatchNotification이 정확히 1회 호출된다")
    void shouldCallSendBatchNotificationExactlyOnceWhenImportant() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(302L, "중요 공지", true);
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of(1L));

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        verify(sendBatchNotificationUseCase, times(1)).sendBatchNotification(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("isImportant=false 이벤트 수신 시 sendBatchNotification이 정확히 1회 호출된다 (인앱 저장용)")
    void shouldCallSendBatchNotificationExactlyOnceWhenNotImportant() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(303L, "일반 공지", false);
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of(1L));

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        verify(sendBatchNotificationUseCase, times(1)).sendBatchNotification(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("레거시 이벤트(isImportant 필드 없음)는 기본값 false로 IN_APP_ONLY 발송된다")
    void shouldFallbackToInAppOnlyForLegacyEvent() {
        // given — buildRecord 기본 호출은 isImportant=false를 사용하여 레거시 이벤트 역직렬화 동작을 재현
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(304L, "레거시 공지");
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of(1L));

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendBatchNotificationCommand> captor = ArgumentCaptor.forClass(
                SendBatchNotificationCommand.class
        );
        verify(sendBatchNotificationUseCase).sendBatchNotification(captor.capture());
        assertThat(captor.getValue().deliveryChannel()).isEqualTo("IN_APP_ONLY");
    }

    @Test
    @DisplayName("발송 시 templateCode는 NOTICE_REGISTERED 고정값으로 전달된다")
    void shouldAlwaysPassTemplateCodeNoticeRegistered() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(305L, "공지", true);
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of(1L));

        // when
        consumer.handleNoticeRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendBatchNotificationCommand> captor = ArgumentCaptor.forClass(
                SendBatchNotificationCommand.class
        );
        verify(sendBatchNotificationUseCase).sendBatchNotification(captor.capture());
        assertThat(captor.getValue().templateCode()).isEqualTo("NOTICE_REGISTERED");
    }
}
