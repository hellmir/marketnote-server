package com.personal.marketnote.notification.adapter.in.web.notification.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.notification.response.GetNotificationHistoryResponse;
import com.personal.marketnote.notification.port.in.result.notification.GetNotificationHistoryResult;
import com.personal.marketnote.notification.port.in.result.notification.GetUnreadNotificationCountResult;
import com.personal.marketnote.notification.port.in.usecase.notification.GetNotificationHistoryUseCase;
import com.personal.marketnote.notification.port.in.usecase.notification.GetUnreadNotificationCountUseCase;
import com.personal.marketnote.notification.port.in.usecase.notification.MarkNotificationAsReadUseCase;
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
class NotificationControllerTest {

    @InjectMocks
    private NotificationController controller;

    @Mock
    private GetNotificationHistoryUseCase getNotificationHistoryUseCase;

    @Mock
    private MarkNotificationAsReadUseCase markNotificationAsReadUseCase;

    @Mock
    private GetUnreadNotificationCountUseCase getUnreadNotificationCountUseCase;

    private OAuth2AuthenticatedPrincipal buildPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }

    @Nested
    @DisplayName("getNotificationHistory")
    class GetNotificationHistory {

        @Test
        @DisplayName("알림 이력을 조회하면 OK 응답을 반환한다")
        void shouldReturnNotificationHistory() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);
            GetNotificationHistoryResult result = new GetNotificationHistoryResult(
                    0L, false, -1L, List.of()
            );
            when(getNotificationHistoryUseCase.getNotificationHistory(any())).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetNotificationHistoryResponse>> response =
                    controller.getNotificationHistory(principal, null, 20);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }
    }

    @Nested
    @DisplayName("markNotificationAsRead")
    class MarkNotificationAsRead {

        @Test
        @DisplayName("알림을 읽음 처리하면 OK 응답을 반환한다")
        void shouldMarkAsReadAndReturnOk() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    controller.markNotificationAsRead(principal, 1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(markNotificationAsReadUseCase).markAsRead(any());
        }
    }

    @Nested
    @DisplayName("getUnreadNotificationCount")
    class GetUnreadNotificationCount {

        @Test
        @DisplayName("미읽음 알림 수를 조회하면 OK 응답을 반환한다")
        void shouldReturnUnreadCount() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);
            when(getUnreadNotificationCountUseCase.getUnreadCount(100L))
                    .thenReturn(new GetUnreadNotificationCountResult(5));

            // when
            ResponseEntity<BaseResponse<GetUnreadNotificationCountResult>> response =
                    controller.getUnreadNotificationCount(principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent().unreadCount()).isEqualTo(5);
        }
    }
}
