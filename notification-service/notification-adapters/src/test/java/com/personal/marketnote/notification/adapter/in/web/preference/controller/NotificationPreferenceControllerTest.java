package com.personal.marketnote.notification.adapter.in.web.preference.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.preference.request.UpdateNotificationPreferenceRequest;
import com.personal.marketnote.notification.adapter.in.web.preference.response.GetNotificationPreferenceResponse;
import com.personal.marketnote.notification.port.in.result.preference.GetNotificationPreferenceResult;
import com.personal.marketnote.notification.port.in.usecase.preference.GetNotificationPreferenceUseCase;
import com.personal.marketnote.notification.port.in.usecase.preference.UpdateNotificationPreferenceUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceControllerTest {

    @InjectMocks
    private NotificationPreferenceController controller;

    @Mock
    private GetNotificationPreferenceUseCase getNotificationPreferenceUseCase;

    @Mock
    private UpdateNotificationPreferenceUseCase updateNotificationPreferenceUseCase;

    private OAuth2AuthenticatedPrincipal buildPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }

    @Nested
    @DisplayName("getNotificationPreferences")
    class GetNotificationPreferences {

        @Test
        @DisplayName("알림 수신 설정 목록을 조회하면 OK 응답을 반환한다")
        void shouldReturnPreferences() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);
            GetNotificationPreferenceResult result = new GetNotificationPreferenceResult(
                    "ORDER_PAYMENT_COMPLETED", "주문 결제 완료", true, null
            );
            when(getNotificationPreferenceUseCase.getNotificationPreferences(100L))
                    .thenReturn(List.of(result));

            // when
            ResponseEntity<BaseResponse<List<GetNotificationPreferenceResponse>>> response =
                    controller.getNotificationPreferences(principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateNotificationPreference")
    class UpdateNotificationPreference {

        @Test
        @DisplayName("알림 수신 설정을 변경하면 OK 응답을 반환한다")
        void shouldUpdateAndReturnOk() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);
            UpdateNotificationPreferenceRequest request = new UpdateNotificationPreferenceRequest(
                    "ORDER_PAYMENT_COMPLETED", true
            );

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    controller.updateNotificationPreference(request, principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(updateNotificationPreferenceUseCase).updateNotificationPreference(any());
        }
    }
}
