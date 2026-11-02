package com.personal.marketnote.notification.adapter.in.web.template.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.template.request.RegisterNotificationTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.template.request.UpdateNotificationTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.template.response.GetNotificationTemplateResponse;
import com.personal.marketnote.notification.adapter.in.web.template.response.RegisterNotificationTemplateResponse;
import com.personal.marketnote.notification.adapter.in.web.template.response.UpdateNotificationTemplateResponse;
import com.personal.marketnote.notification.domain.template.NotificationCategory;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.result.template.GetNotificationTemplateResult;
import com.personal.marketnote.notification.port.in.result.template.RegisterNotificationTemplateResult;
import com.personal.marketnote.notification.port.in.result.template.UpdateNotificationTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.template.DeleteNotificationTemplateUseCase;
import com.personal.marketnote.notification.port.in.usecase.template.GetNotificationTemplateUseCase;
import com.personal.marketnote.notification.port.in.usecase.template.RegisterNotificationTemplateUseCase;
import com.personal.marketnote.notification.port.in.usecase.template.UpdateNotificationTemplateUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateControllerTest {

    @InjectMocks
    private NotificationTemplateController controller;

    @Mock
    private RegisterNotificationTemplateUseCase registerNotificationTemplateUseCase;

    @Mock
    private GetNotificationTemplateUseCase getNotificationTemplateUseCase;

    @Mock
    private UpdateNotificationTemplateUseCase updateNotificationTemplateUseCase;

    @Mock
    private DeleteNotificationTemplateUseCase deleteNotificationTemplateUseCase;

    @Nested
    @DisplayName("registerNotificationTemplate")
    class RegisterNotificationTemplate {

        @Test
        @DisplayName("템플릿을 등록하면 CREATED 응답을 반환한다")
        void shouldRegisterAndReturnCreated() {
            // given
            RegisterNotificationTemplateRequest request = new RegisterNotificationTemplateRequest(
                    "ORDER_COMPLETE", NotificationType.ORDER_PAYMENT_COMPLETED,
                    NotificationCategory.INFORMATIONAL, "주문 완료", "{name}님의 주문이 완료되었습니다.", "/orders/{orderId}"
            );
            when(registerNotificationTemplateUseCase.registerNotificationTemplate(any()))
                    .thenReturn(RegisterNotificationTemplateResult.of(1L));

            // when
            ResponseEntity<BaseResponse<RegisterNotificationTemplateResponse>> response =
                    controller.registerNotificationTemplate(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().getContent().id()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("getNotificationTemplates")
    class GetNotificationTemplates {

        @Test
        @DisplayName("템플릿 목록을 조회하면 OK 응답을 반환한다")
        void shouldReturnTemplates() {
            // given
            GetNotificationTemplateResult result = new GetNotificationTemplateResult(
                    1L, "ORDER_COMPLETE", NotificationType.ORDER_PAYMENT_COMPLETED,
                    NotificationCategory.INFORMATIONAL, "주문 완료", "본문", "/orders", null, null
            );
            when(getNotificationTemplateUseCase.getNotificationTemplates()).thenReturn(List.of(result));

            // when
            ResponseEntity<BaseResponse<List<GetNotificationTemplateResponse>>> response =
                    controller.getNotificationTemplates();

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getNotificationTemplate")
    class GetNotificationTemplate {

        @Test
        @DisplayName("템플릿을 단건 조회하면 OK 응답을 반환한다")
        void shouldReturnSingleTemplate() {
            // given
            GetNotificationTemplateResult result = new GetNotificationTemplateResult(
                    1L, "ORDER_COMPLETE", NotificationType.ORDER_PAYMENT_COMPLETED,
                    NotificationCategory.INFORMATIONAL, "주문 완료", "본문", "/orders", null, null
            );
            when(getNotificationTemplateUseCase.getNotificationTemplate(1L)).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetNotificationTemplateResponse>> response =
                    controller.getNotificationTemplate(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("updateNotificationTemplate")
    class UpdateNotificationTemplate {

        @Test
        @DisplayName("템플릿을 수정하면 OK 응답을 반환한다")
        void shouldUpdateAndReturnOk() {
            // given
            UpdateNotificationTemplateRequest request = new UpdateNotificationTemplateRequest(
                    "수정된 제목", "수정된 본문", "/updated"
            );
            UpdateNotificationTemplateResult result = new UpdateNotificationTemplateResult(
                    1L, "ORDER_COMPLETE", NotificationType.ORDER_PAYMENT_COMPLETED,
                    NotificationCategory.INFORMATIONAL, "수정된 제목", "수정된 본문", "/updated"
            );
            when(updateNotificationTemplateUseCase.updateNotificationTemplate(eq(1L), any()))
                    .thenReturn(result);

            // when
            ResponseEntity<BaseResponse<UpdateNotificationTemplateResponse>> response =
                    controller.updateNotificationTemplate(1L, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("deleteNotificationTemplate")
    class DeleteNotificationTemplate {

        @Test
        @DisplayName("템플릿을 삭제하면 OK 응답을 반환한다")
        void shouldDeleteAndReturnOk() {
            // when
            ResponseEntity<BaseResponse<Void>> response = controller.deleteNotificationTemplate(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteNotificationTemplateUseCase).deleteNotificationTemplate(1L);
        }
    }
}
