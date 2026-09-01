package com.personal.marketnote.notification.adapter.out.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.domain.notification.FcmSendFailedException;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FcmPushNotificationClientTest {

    @InjectMocks
    private FcmPushNotificationClient fcmPushNotificationClient;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Nested
    @DisplayName("FCM 메시지 발송 성공")
    class SendSuccess {

        @Test
        @DisplayName("Android 디바이스에 FCM 메시지를 발송하면 messageId를 반환한다")
        void shouldReturnMessageIdForAndroidDevice() throws Exception {
            // given
            SendPushNotificationCommand command = new SendPushNotificationCommand(
                    "fcm-token-android", "[배송 시작]", "요청하신 상품이 배송 시작되었습니다.",
                    "/order/12345", Platform.ANDROID
            );
            when(firebaseMessaging.send(any(Message.class))).thenReturn("projects/test/messages/12345");

            // when
            SendPushNotificationResult result = fcmPushNotificationClient.send(command);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isEqualTo("projects/test/messages/12345");
            assertThat(result.errorCode()).isNull();
            assertThat(result.tokenInvalid()).isFalse();
        }

        @Test
        @DisplayName("iOS 디바이스에 FCM 메시지를 발송하면 messageId를 반환한다")
        void shouldReturnMessageIdForIosDevice() throws Exception {
            // given
            SendPushNotificationCommand command = new SendPushNotificationCommand(
                    "fcm-token-ios", "[주문 완료]", "주문이 완료되었습니다.",
                    "/order/67890", Platform.IOS
            );
            when(firebaseMessaging.send(any(Message.class))).thenReturn("projects/test/messages/67890");

            // when
            SendPushNotificationResult result = fcmPushNotificationClient.send(command);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.messageId()).isEqualTo("projects/test/messages/67890");
        }
    }

    @Nested
    @DisplayName("FCM 토큰 무효 처리")
    class TokenInvalid {

        @Test
        @DisplayName("UNREGISTERED 에러 시 tokenInvalid가 true를 반환한다")
        void shouldReturnTokenInvalidForUnregistered() throws Exception {
            // given
            SendPushNotificationCommand command = createDefaultCommand();
            FirebaseMessagingException exception = createFirebaseMessagingException(MessagingErrorCode.UNREGISTERED);
            when(firebaseMessaging.send(any(Message.class))).thenThrow(exception);

            // when
            SendPushNotificationResult result = fcmPushNotificationClient.send(command);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.tokenInvalid()).isTrue();
            assertThat(result.errorCode()).isEqualTo("UNREGISTERED");
        }

        @Test
        @DisplayName("INVALID_ARGUMENT 에러 시 tokenInvalid가 true를 반환한다")
        void shouldReturnTokenInvalidForInvalidArgument() throws Exception {
            // given
            SendPushNotificationCommand command = createDefaultCommand();
            FirebaseMessagingException exception = createFirebaseMessagingException(MessagingErrorCode.INVALID_ARGUMENT);
            when(firebaseMessaging.send(any(Message.class))).thenThrow(exception);

            // when
            SendPushNotificationResult result = fcmPushNotificationClient.send(command);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.tokenInvalid()).isTrue();
            assertThat(result.errorCode()).isEqualTo("INVALID_ARGUMENT");
        }
    }

    @Nested
    @DisplayName("FCM 발송 실패 처리")
    class SendFailure {

        @Test
        @DisplayName("INTERNAL 에러 시 success가 false를 반환한다")
        void shouldReturnFailureForInternalError() throws Exception {
            // given
            SendPushNotificationCommand command = createDefaultCommand();
            FirebaseMessagingException exception = createFirebaseMessagingException(MessagingErrorCode.INTERNAL);
            when(firebaseMessaging.send(any(Message.class))).thenThrow(exception);

            // when
            SendPushNotificationResult result = fcmPushNotificationClient.send(command);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.tokenInvalid()).isFalse();
            assertThat(result.errorCode()).isEqualTo("INTERNAL");
        }

        @Test
        @DisplayName("예상치 못한 예외 발생 시 FcmSendFailedException이 발생한다")
        void shouldThrowFcmSendFailedExceptionForUnexpectedError() throws Exception {
            // given
            SendPushNotificationCommand command = createDefaultCommand();
            when(firebaseMessaging.send(any(Message.class))).thenThrow(new RuntimeException("네트워크 오류"));

            // when & then
            assertThatThrownBy(() -> fcmPushNotificationClient.send(command))
                    .isInstanceOf(FcmSendFailedException.class);
        }
    }

    private SendPushNotificationCommand createDefaultCommand() {
        return new SendPushNotificationCommand(
                "fcm-token-test", "[알림]", "테스트 알림입니다.",
                "/test/123", Platform.ANDROID
        );
    }

    private FirebaseMessagingException createFirebaseMessagingException(MessagingErrorCode errorCode) {
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(exception.getMessagingErrorCode()).thenReturn(errorCode);
        return exception;
    }
}
