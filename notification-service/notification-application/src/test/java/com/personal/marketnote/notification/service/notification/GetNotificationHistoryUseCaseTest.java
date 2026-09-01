package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.notification.*;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.command.GetNotificationHistoryCommand;
import com.personal.marketnote.notification.port.in.result.notification.GetNotificationHistoryResult;
import com.personal.marketnote.notification.port.in.result.notification.NotificationItemResult;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetNotificationHistoryUseCaseTest {

    @InjectMocks
    private GetNotificationHistoryService getNotificationHistoryService;

    @Mock
    private FindNotificationPort findNotificationPort;

    @Test
    @DisplayName("첫 페이지 조회 시 totalElements를 포함하여 반환한다")
    void shouldReturnTotalElementsOnFirstPage() {
        // given
        Long userId = 1L;
        int pageSize = 2;
        GetNotificationHistoryCommand command = new GetNotificationHistoryCommand(userId, null, pageSize);

        List<Notification> notifications = createNotifications(3, userId);
        when(findNotificationPort.findByUserId(userId, null, 3)).thenReturn(notifications);
        when(findNotificationPort.countByUserId(userId)).thenReturn(10L);

        // when
        GetNotificationHistoryResult result = getNotificationHistoryService.getNotificationHistory(command);

        // then
        assertThat(result.totalElements()).isEqualTo(10L);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.notifications()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(2L);

        verify(findNotificationPort).findByUserId(userId, null, 3);
        verify(findNotificationPort).countByUserId(userId);
    }

    @Test
    @DisplayName("두 번째 페이지 조회 시 totalElements가 null이다")
    void shouldReturnNullTotalElementsOnSubsequentPage() {
        // given
        Long userId = 1L;
        Long cursor = 5L;
        int pageSize = 2;
        GetNotificationHistoryCommand command = new GetNotificationHistoryCommand(userId, cursor, pageSize);

        List<Notification> notifications = createNotifications(3, userId);
        when(findNotificationPort.findByUserId(userId, cursor, 3)).thenReturn(notifications);

        // when
        GetNotificationHistoryResult result = getNotificationHistoryService.getNotificationHistory(command);

        // then
        assertThat(result.totalElements()).isNull();
        assertThat(result.hasNext()).isTrue();
        assertThat(result.notifications()).hasSize(2);

        verify(findNotificationPort).findByUserId(userId, cursor, 3);
        verifyNoMoreInteractions(findNotificationPort);
    }

    @Test
    @DisplayName("결과가 pageSize 이하이면 hasNext가 false이다")
    void shouldReturnHasNextFalseWhenNoMoreResults() {
        // given
        Long userId = 1L;
        int pageSize = 20;
        GetNotificationHistoryCommand command = new GetNotificationHistoryCommand(userId, null, pageSize);

        List<Notification> notifications = createNotifications(5, userId);
        when(findNotificationPort.findByUserId(userId, null, 21)).thenReturn(notifications);

        // when
        GetNotificationHistoryResult result = getNotificationHistoryService.getNotificationHistory(command);

        // then
        assertThat(result.hasNext()).isFalse();
        assertThat(result.totalElements()).isEqualTo(5L);
        assertThat(result.notifications()).hasSize(5);

        verify(findNotificationPort).findByUserId(userId, null, 21);
        verifyNoMoreInteractions(findNotificationPort);
    }

    @Test
    @DisplayName("결과가 비어있으면 빈 목록과 nextCursor null을 반환한다")
    void shouldReturnEmptyResultWhenNoNotifications() {
        // given
        Long userId = 1L;
        int pageSize = 20;
        GetNotificationHistoryCommand command = new GetNotificationHistoryCommand(userId, null, pageSize);

        when(findNotificationPort.findByUserId(userId, null, 21)).thenReturn(List.of());

        // when
        GetNotificationHistoryResult result = getNotificationHistoryService.getNotificationHistory(command);

        // then
        assertThat(result.notifications()).isEmpty();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.totalElements()).isEqualTo(0L);

        verify(findNotificationPort).findByUserId(userId, null, 21);
        verifyNoMoreInteractions(findNotificationPort);
    }

    @Test
    @DisplayName("알림 항목의 상세 정보가 올바르게 매핑된다")
    void shouldMapNotificationItemResultCorrectly() {
        // given
        Long userId = 1L;
        int pageSize = 20;
        GetNotificationHistoryCommand command = new GetNotificationHistoryCommand(userId, null, pageSize);

        LocalDateTime now = LocalDateTime.of(2026, 4, 9, 12, 0);
        Notification notification = Notification.from(
                NotificationSnapshotState.builder()
                        .id(100L)
                        .userId(userId)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .title("주문이 완료되었습니다")
                        .body("테스트 상품 외 1건이 결제되었습니다.")
                        .deliveryChannel(DeliveryChannel.PUSH_AND_IN_APP)
                        .isRead(false)
                        .landingUrl("/order/123")
                        .sendStatus(SendStatus.SENT)
                        .status(EntityStatus.ACTIVE)
                        .createdAt(now)
                        .modifiedAt(now)
                        .build()
        );

        when(findNotificationPort.findByUserId(userId, null, 21)).thenReturn(List.of(notification));

        // when
        GetNotificationHistoryResult result = getNotificationHistoryService.getNotificationHistory(command);

        // then
        assertThat(result.notifications()).hasSize(1);
        NotificationItemResult item = result.notifications().getFirst();
        assertThat(item.id()).isEqualTo(100L);
        assertThat(item.notificationType()).isEqualTo("ORDER_PAYMENT_COMPLETED");
        assertThat(item.notificationTypeDescription()).isEqualTo("주문 결제 완료");
        assertThat(item.title()).isEqualTo("주문이 완료되었습니다");
        assertThat(item.body()).isEqualTo("테스트 상품 외 1건이 결제되었습니다.");
        assertThat(item.deliveryChannel()).isEqualTo(DeliveryChannel.PUSH_AND_IN_APP);
        assertThat(item.isRead()).isFalse();
        assertThat(item.landingUrl()).isEqualTo("/order/123");
        assertThat(item.sendStatus()).isEqualTo(SendStatus.SENT);
        assertThat(item.createdAt()).isEqualTo(now);
    }

    private List<Notification> createNotifications(int count, Long userId) {
        LocalDateTime now = LocalDateTime.of(2026, 4, 9, 12, 0);
        List<Notification> notifications = new ArrayList<>();
        for (long i = count; i >= 1; i--) {
            notifications.add(Notification.from(
                    NotificationSnapshotState.builder()
                            .id(i)
                            .userId(userId)
                            .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                            .title("알림 " + i)
                            .body("본문 " + i)
                            .deliveryChannel(DeliveryChannel.PUSH_AND_IN_APP)
                            .isRead(false)
                            .sendStatus(SendStatus.SENT)
                            .status(EntityStatus.ACTIVE)
                            .createdAt(now.minusHours(i))
                            .modifiedAt(now.minusHours(i))
                            .build()
            ));
        }
        return notifications;
    }
}
