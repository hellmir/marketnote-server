package com.personal.marketnote.notification.service.template;

import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationTemplateCreateState;
import com.personal.marketnote.notification.domain.template.NotificationTemplateNotFoundException;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.result.template.GetNotificationTemplateResult;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNotificationTemplateUseCaseTest {

    @InjectMocks
    private GetNotificationTemplateService getNotificationTemplateService;

    @Mock
    private FindNotificationTemplatePort findNotificationTemplatePort;

    @Test
    @DisplayName("ID로 알림 템플릿을 조회한다")
    void shouldGetNotificationTemplateById() {
        // given
        NotificationTemplate template = createTemplate("ORDER_PAYMENT_COMPLETED",
                NotificationType.ORDER_PAYMENT_COMPLETED, "주문이 완료되었습니다");

        when(findNotificationTemplatePort.findActiveById(1L))
                .thenReturn(Optional.of(template));

        // when
        GetNotificationTemplateResult result = getNotificationTemplateService.getNotificationTemplate(1L);

        // then
        assertThat(result.templateCode()).isEqualTo("ORDER_PAYMENT_COMPLETED");
        assertThat(result.notificationType()).isEqualTo(NotificationType.ORDER_PAYMENT_COMPLETED);
        assertThat(result.title()).isEqualTo("주문이 완료되었습니다");
        verify(findNotificationTemplatePort).findActiveById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
    void shouldThrowExceptionWhenTemplateNotFound() {
        // given
        when(findNotificationTemplatePort.findActiveById(999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getNotificationTemplateService.getNotificationTemplate(999L))
                .isInstanceOf(NotificationTemplateNotFoundException.class);
    }

    @Test
    @DisplayName("전체 알림 템플릿을 조회한다")
    void shouldGetAllNotificationTemplates() {
        // given
        NotificationTemplate template1 = createTemplate("ORDER_PAYMENT_COMPLETED",
                NotificationType.ORDER_PAYMENT_COMPLETED, "주문이 완료되었습니다");
        NotificationTemplate template2 = createTemplate("SHIPPING_STARTED",
                NotificationType.SHIPPING_STARTED, "배송이 시작되었습니다");

        when(findNotificationTemplatePort.findAllActive())
                .thenReturn(List.of(template1, template2));

        // when
        List<GetNotificationTemplateResult> results = getNotificationTemplateService.getNotificationTemplates();

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).templateCode()).isEqualTo("ORDER_PAYMENT_COMPLETED");
        assertThat(results.get(1).templateCode()).isEqualTo("SHIPPING_STARTED");
        verify(findNotificationTemplatePort).findAllActive();
    }

    @Test
    @DisplayName("알림 템플릿이 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoTemplates() {
        // given
        when(findNotificationTemplatePort.findAllActive())
                .thenReturn(List.of());

        // when
        List<GetNotificationTemplateResult> results = getNotificationTemplateService.getNotificationTemplates();

        // then
        assertThat(results).isEmpty();
    }

    private NotificationTemplate createTemplate(String templateCode, NotificationType type, String title) {
        return NotificationTemplate.from(NotificationTemplateCreateState.builder()
                .templateCode(templateCode)
                .notificationType(type)
                .title(title)
                .bodyTemplate("본문")
                .urlTemplate("/order/{orderId}")
                .build());
    }
}
