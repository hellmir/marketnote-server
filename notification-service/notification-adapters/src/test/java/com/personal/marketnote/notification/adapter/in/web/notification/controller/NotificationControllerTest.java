package com.personal.marketnote.notification.adapter.in.web.notification.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.in.ratelimit.RateLimited;
import com.personal.marketnote.notification.adapter.in.web.notification.response.GetNotificationHistoryResponse;
import com.personal.marketnote.notification.domain.template.NotificationDisplayCategory;
import com.personal.marketnote.notification.port.in.command.GetNotificationHistoryCommand;
import com.personal.marketnote.notification.port.in.result.notification.GetNotificationHistoryResult;
import com.personal.marketnote.notification.port.in.result.notification.GetUnreadNotificationCountResult;
import com.personal.marketnote.notification.port.in.usecase.notification.GetNotificationHistoryUseCase;
import com.personal.marketnote.notification.port.in.usecase.notification.GetUnreadNotificationCountUseCase;
import com.personal.marketnote.notification.port.in.usecase.notification.MarkAllNotificationAsReadUseCase;
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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
    private MarkAllNotificationAsReadUseCase markAllNotificationAsReadUseCase;

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
                    controller.getNotificationHistory(principal, null, 20, null);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("category 파라미터를 Command에 전파한다")
        void shouldPropagateCategoryToCommand() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);
            NotificationDisplayCategory category = NotificationDisplayCategory.ORDER_SHIPPING;
            GetNotificationHistoryResult result = new GetNotificationHistoryResult(
                    0L, false, -1L, List.of()
            );
            when(getNotificationHistoryUseCase.getNotificationHistory(any())).thenReturn(result);

            // when
            controller.getNotificationHistory(principal, null, 20, category);

            // then
            verify(getNotificationHistoryUseCase).getNotificationHistory(
                    argThat((GetNotificationHistoryCommand cmd) -> cmd.category() == category)
            );
        }

        @Test
        @DisplayName("category 미전달 시 Command의 category는 null이다")
        void shouldSetNullCategoryWhenNotProvided() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);
            GetNotificationHistoryResult result = new GetNotificationHistoryResult(
                    0L, false, -1L, List.of()
            );
            when(getNotificationHistoryUseCase.getNotificationHistory(any())).thenReturn(result);

            // when
            controller.getNotificationHistory(principal, null, 20, null);

            // then
            verify(getNotificationHistoryUseCase).getNotificationHistory(
                    argThat((GetNotificationHistoryCommand cmd) -> cmd.category() == null)
            );
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
    @DisplayName("markAllNotificationAsRead")
    class MarkAllNotificationAsRead {

        @Test
        @DisplayName("전체 읽음 처리 요청 시 OK 응답을 반환한다")
        void shouldMarkAllAsReadAndReturnOk() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    controller.markAllNotificationAsRead(principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(markAllNotificationAsReadUseCase).markAllAsRead(100L);
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

    @Nested
    @DisplayName("RateLimited 어노테이션 부착 검증")
    class RateLimitedAnnotation {

        @Test
        @DisplayName("getNotificationHistory 메서드에 @RateLimited(key=my-notifications, maxRequests=60, windowSeconds=60)이 부착되어 있다")
        void getNotificationHistoryHasRateLimited() throws NoSuchMethodException {
            Method method = NotificationController.class.getMethod(
                    "getNotificationHistory",
                    OAuth2AuthenticatedPrincipal.class, Long.class, int.class, NotificationDisplayCategory.class
            );

            RateLimited rateLimited = method.getAnnotation(RateLimited.class);

            assertThat(rateLimited).isNotNull();
            assertThat(rateLimited.key()).isEqualTo("my-notifications");
            assertThat(rateLimited.maxRequests()).isEqualTo(60);
            assertThat(rateLimited.windowSeconds()).isEqualTo(60);
        }

        @Test
        @DisplayName("markAllNotificationAsRead 메서드에 @RateLimited(key=notifications-read-all, maxRequests=10, windowSeconds=60)이 부착되어 있다")
        void markAllNotificationAsReadHasRateLimited() throws NoSuchMethodException {
            Method method = NotificationController.class.getMethod(
                    "markAllNotificationAsRead",
                    OAuth2AuthenticatedPrincipal.class
            );

            RateLimited rateLimited = method.getAnnotation(RateLimited.class);

            assertThat(rateLimited).isNotNull();
            assertThat(rateLimited.key()).isEqualTo("notifications-read-all");
            assertThat(rateLimited.maxRequests()).isEqualTo(10);
            assertThat(rateLimited.windowSeconds()).isEqualTo(60);
        }
    }
}
