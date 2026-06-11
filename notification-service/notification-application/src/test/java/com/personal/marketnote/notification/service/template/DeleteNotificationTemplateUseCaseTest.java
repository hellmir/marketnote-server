package com.personal.marketnote.notification.service.template;

import com.personal.marketnote.notification.domain.template.*;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import com.personal.marketnote.notification.port.out.template.UpdateNotificationTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteNotificationTemplateUseCaseTest {

    @InjectMocks
    private DeleteNotificationTemplateService deleteNotificationTemplateService;

    @Mock
    private FindNotificationTemplatePort findNotificationTemplatePort;

    @Mock
    private UpdateNotificationTemplatePort updateNotificationTemplatePort;

    @Test
    @DisplayName("알림 템플릿을 비활성화(삭제)한다")
    void shouldDeactivateNotificationTemplate() {
        // given
        NotificationTemplate template = NotificationTemplate.from(NotificationTemplateCreateState.builder()
                .templateCode("ORDER_PAYMENT_COMPLETED")
                .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                .notificationCategory(NotificationCategory.INFORMATIONAL)
                .title("주문이 완료되었습니다")
                .bodyTemplate("본문")
                .urlTemplate("/order/{orderId}")
                .build());

        when(findNotificationTemplatePort.findActiveById(1L))
                .thenReturn(Optional.of(template));

        // when
        deleteNotificationTemplateService.deleteNotificationTemplate(1L);

        // then
        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        verify(updateNotificationTemplatePort).update(captor.capture());
        assertThat(captor.getValue().isInactive()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 템플릿을 삭제하면 예외가 발생한다")
    void shouldThrowExceptionWhenTemplateNotFound() {
        // given
        when(findNotificationTemplatePort.findActiveById(999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deleteNotificationTemplateService.deleteNotificationTemplate(999L))
                .isInstanceOf(NotificationTemplateNotFoundException.class);
        verify(updateNotificationTemplatePort, never()).update(any());
    }
}
