package com.personal.marketnote.notification.service.template;

import com.personal.marketnote.notification.domain.template.*;
import com.personal.marketnote.notification.port.in.command.UpdateNotificationTemplateCommand;
import com.personal.marketnote.notification.port.in.result.template.UpdateNotificationTemplateResult;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import com.personal.marketnote.notification.port.out.template.UpdateNotificationTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateNotificationTemplateUseCaseTest {

    @InjectMocks
    private UpdateNotificationTemplateService updateNotificationTemplateService;

    @Mock
    private FindNotificationTemplatePort findNotificationTemplatePort;

    @Mock
    private UpdateNotificationTemplatePort updateNotificationTemplatePort;

    @Test
    @DisplayName("알림 템플릿을 수정한다")
    void shouldUpdateNotificationTemplate() {
        // given
        NotificationTemplate template = NotificationTemplate.from(NotificationTemplateCreateState.builder()
                .templateCode("ORDER_PAYMENT_COMPLETED")
                .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                .notificationCategory(NotificationCategory.INFORMATIONAL)
                .title("주문이 완료되었습니다")
                .bodyTemplate("기존 본문")
                .urlTemplate("/order/{orderId}")
                .build());

        UpdateNotificationTemplateCommand command = new UpdateNotificationTemplateCommand(
                "수정된 제목",
                "수정된 본문 {productName}",
                "/updated/{orderId}"
        );

        when(findNotificationTemplatePort.findActiveById(1L))
                .thenReturn(Optional.of(template));

        // when
        UpdateNotificationTemplateResult result = updateNotificationTemplateService.updateNotificationTemplate(1L, command);

        // then
        assertThat(result.title()).isEqualTo("수정된 제목");
        assertThat(result.bodyTemplate()).isEqualTo("수정된 본문 {productName}");
        assertThat(result.urlTemplate()).isEqualTo("/updated/{orderId}");
        verify(updateNotificationTemplatePort).update(any(NotificationTemplate.class));
    }

    @Test
    @DisplayName("존재하지 않는 템플릿을 수정하면 예외가 발생한다")
    void shouldThrowExceptionWhenTemplateNotFound() {
        // given
        UpdateNotificationTemplateCommand command = new UpdateNotificationTemplateCommand(
                "제목", "본문", "/url"
        );

        when(findNotificationTemplatePort.findActiveById(999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> updateNotificationTemplateService.updateNotificationTemplate(999L, command))
                .isInstanceOf(NotificationTemplateNotFoundException.class);
        verify(updateNotificationTemplatePort, never()).update(any());
    }
}
