package com.personal.marketnote.notification.service.template;

import com.personal.marketnote.notification.domain.template.DuplicateNotificationTemplateException;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationTemplateCreateState;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.command.RegisterNotificationTemplateCommand;
import com.personal.marketnote.notification.port.in.result.template.RegisterNotificationTemplateResult;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import com.personal.marketnote.notification.port.out.template.SaveNotificationTemplatePort;
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
class RegisterNotificationTemplateUseCaseTest {

    @InjectMocks
    private RegisterNotificationTemplateService registerNotificationTemplateService;

    @Mock
    private FindNotificationTemplatePort findNotificationTemplatePort;

    @Mock
    private SaveNotificationTemplatePort saveNotificationTemplatePort;

    @Test
    @DisplayName("알림 템플릿을 등록한다")
    void shouldRegisterNotificationTemplate() {
        // given
        RegisterNotificationTemplateCommand command = new RegisterNotificationTemplateCommand(
                "ORDER_PAYMENT_COMPLETED",
                "ORDER_PAYMENT_COMPLETED",
                "주문이 완료되었습니다",
                "{productName} 외 {count}건이 결제되었습니다.",
                "/order/{orderId}"
        );

        when(findNotificationTemplatePort.findActiveByTemplateCode("ORDER_PAYMENT_COMPLETED"))
                .thenReturn(Optional.empty());
        when(saveNotificationTemplatePort.save(any(NotificationTemplate.class)))
                .thenReturn(1L);

        // when
        RegisterNotificationTemplateResult result = registerNotificationTemplateService.registerNotificationTemplate(command);

        // then
        assertThat(result.id()).isEqualTo(1L);
        verify(findNotificationTemplatePort).findActiveByTemplateCode("ORDER_PAYMENT_COMPLETED");
        verify(saveNotificationTemplatePort).save(any(NotificationTemplate.class));
    }

    @Test
    @DisplayName("이미 존재하는 템플릿 코드로 등록하면 예외가 발생한다")
    void shouldThrowExceptionWhenDuplicateTemplateCode() {
        // given
        RegisterNotificationTemplateCommand command = new RegisterNotificationTemplateCommand(
                "ORDER_PAYMENT_COMPLETED",
                "ORDER_PAYMENT_COMPLETED",
                "주문이 완료되었습니다",
                "본문",
                "/order/{orderId}"
        );

        NotificationTemplate existing = NotificationTemplate.from(NotificationTemplateCreateState.builder()
                .templateCode("ORDER_PAYMENT_COMPLETED")
                .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                .title("기존 제목")
                .bodyTemplate("기존 본문")
                .urlTemplate("/order/{orderId}")
                .build());

        when(findNotificationTemplatePort.findActiveByTemplateCode("ORDER_PAYMENT_COMPLETED"))
                .thenReturn(Optional.of(existing));

        // when & then
        assertThatThrownBy(() -> registerNotificationTemplateService.registerNotificationTemplate(command))
                .isInstanceOf(DuplicateNotificationTemplateException.class);
        verify(saveNotificationTemplatePort, never()).save(any());
    }
}
