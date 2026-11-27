package com.personal.marketnote.notification.adapter.out.fcm;

import com.google.firebase.messaging.*;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.domain.notification.FcmSendFailedException;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.result.SendBatchPushNotificationResult;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

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
            FirebaseMessagingException exception = createFirebaseMessagingException(
                MessagingErrorCode.INVALID_ARGUMENT
            );
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

    @Nested
    @DisplayName("FCM 대량 발송")
    class SendBatch {

        @Test
        @DisplayName("sendBatch 호출 시 모든 메시지가 성공하면 successCount 가 입력 사이즈와 같고 failedTokens 는 비어있다")
        void shouldReturnAllSuccess() throws Exception {
            // given
            List<SendPushNotificationCommand> commands = List.of(
                createCommand("token-1"),
                createCommand("token-2")
            );
            BatchResponse batchResponse = mock(BatchResponse.class);
            SendResponse ok1 = mock(SendResponse.class);
            SendResponse ok2 = mock(SendResponse.class);
            when(ok1.isSuccessful()).thenReturn(true);
            when(ok2.isSuccessful()).thenReturn(true);
            when(batchResponse.getSuccessCount()).thenReturn(2);
            when(batchResponse.getFailureCount()).thenReturn(0);
            when(batchResponse.getResponses()).thenReturn(List.of(ok1, ok2));
            when(firebaseMessaging.sendEach(anyList())).thenReturn(batchResponse);

            // when
            SendBatchPushNotificationResult result = fcmPushNotificationClient.sendBatch(commands);

            // then
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(result.failureCount()).isZero();
            assertThat(result.failedTokens()).isEmpty();
        }

        @Test
        @DisplayName("sendBatch 호출 시 일부 응답이 실패하면 failedTokens 에 디바이스 토큰과 errorCode 가 포함된다")
        void shouldCollectFailedTokens() throws Exception {
            // given
            List<SendPushNotificationCommand> commands = List.of(
                createCommand("token-A"),
                createCommand("token-B")
            );
            BatchResponse batchResponse = mock(BatchResponse.class);
            SendResponse ok = mock(SendResponse.class);
            SendResponse fail = mock(SendResponse.class);
            FirebaseMessagingException fme = createFirebaseMessagingException(MessagingErrorCode.UNREGISTERED);
            when(ok.isSuccessful()).thenReturn(true);
            when(fail.isSuccessful()).thenReturn(false);
            when(fail.getException()).thenReturn(fme);
            when(batchResponse.getSuccessCount()).thenReturn(1);
            when(batchResponse.getFailureCount()).thenReturn(1);
            when(batchResponse.getResponses()).thenReturn(List.of(ok, fail));
            when(firebaseMessaging.sendEach(anyList())).thenReturn(batchResponse);

            // when
            SendBatchPushNotificationResult result = fcmPushNotificationClient.sendBatch(commands);

            // then
            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.failureCount()).isEqualTo(1);
            assertThat(result.failedTokens()).hasSize(1);
            assertThat(result.failedTokens().get(0).deviceToken()).isEqualTo("token-B");
            assertThat(result.failedTokens().get(0).errorCode()).isEqualTo("UNREGISTERED");
            assertThat(result.failedTokens().get(0).tokenInvalid()).isTrue();
        }

        @Test
        @DisplayName("sendBatch 호출 시 실패 응답에 예외가 없으면 errorCode 는 UNKNOWN 으로 기록된다")
        void shouldUseUnknownErrorCodeWhenExceptionMissing() throws Exception {
            // given
            List<SendPushNotificationCommand> commands = List.of(createCommand("token-X"));
            BatchResponse batchResponse = mock(BatchResponse.class);
            SendResponse fail = mock(SendResponse.class);
            when(fail.isSuccessful()).thenReturn(false);
            when(fail.getException()).thenReturn(null);
            when(batchResponse.getSuccessCount()).thenReturn(0);
            when(batchResponse.getFailureCount()).thenReturn(1);
            when(batchResponse.getResponses()).thenReturn(List.of(fail));
            when(firebaseMessaging.sendEach(anyList())).thenReturn(batchResponse);

            // when
            SendBatchPushNotificationResult result = fcmPushNotificationClient.sendBatch(commands);

            // then
            assertThat(result.failedTokens()).hasSize(1);
            assertThat(result.failedTokens().get(0).errorCode()).isEqualTo("UNKNOWN");
            assertThat(result.failedTokens().get(0).tokenInvalid()).isFalse();
        }

        @Test
        @DisplayName("sendBatch 호출 시 재시도 불가 예외(QUOTA_EXCEEDED 외)가 발생하면 청크 전체가 BATCH_SEND_FAILED 로 기록된다")
        void shouldFailEntireChunkWhenNonRetriableException() throws Exception {
            // given
            List<SendPushNotificationCommand> commands = List.of(
                createCommand("token-1"),
                createCommand("token-2")
            );
            FirebaseMessagingException fme = createFirebaseMessagingException(MessagingErrorCode.INTERNAL);
            when(firebaseMessaging.sendEach(anyList())).thenThrow(fme);

            // when
            SendBatchPushNotificationResult result = fcmPushNotificationClient.sendBatch(commands);

            // then
            assertThat(result.successCount()).isZero();
            assertThat(result.failureCount()).isEqualTo(2);
            assertThat(result.failedTokens()).hasSize(2);
            assertThat(result.failedTokens())
                .extracting(SendBatchPushNotificationResult.FailedToken::errorCode)
                .containsOnly("BATCH_SEND_FAILED");
            verify(firebaseMessaging, times(1)).sendEach(anyList());
        }

        @Test
        @DisplayName("sendBatch 호출 시 QUOTA_EXCEEDED 예외 후 재시도하여 성공하면 success 로 집계된다")
        void shouldRetryOnQuotaExceededAndSucceed() throws Exception {
            // given
            List<SendPushNotificationCommand> commands = List.of(createCommand("token-Q"));
            FirebaseMessagingException quota = createFirebaseMessagingException(MessagingErrorCode.QUOTA_EXCEEDED);
            BatchResponse success = mock(BatchResponse.class);
            SendResponse ok = mock(SendResponse.class);
            when(ok.isSuccessful()).thenReturn(true);
            when(success.getSuccessCount()).thenReturn(1);
            when(success.getFailureCount()).thenReturn(0);
            when(success.getResponses()).thenReturn(List.of(ok));
            when(firebaseMessaging.sendEach(anyList()))
                .thenThrow(quota)
                .thenReturn(success);

            // when
            SendBatchPushNotificationResult result = fcmPushNotificationClient.sendBatch(commands);

            // then
            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.failureCount()).isZero();
            verify(firebaseMessaging, times(2)).sendEach(anyList());
        }

        @Test
        @DisplayName("sendBatch 호출 시 QUOTA_EXCEEDED 가 최대 재시도 횟수를 넘기면 청크 전체가 BATCH_SEND_FAILED 로 기록된다")
        void shouldFailAfterMaxRetriesOnQuotaExceeded() throws Exception {
            // given
            List<SendPushNotificationCommand> commands = List.of(createCommand("token-MAX"));
            FirebaseMessagingException quota = createFirebaseMessagingException(MessagingErrorCode.QUOTA_EXCEEDED);
            when(firebaseMessaging.sendEach(anyList())).thenThrow(quota);

            // when
            SendBatchPushNotificationResult result = fcmPushNotificationClient.sendBatch(commands);

            // then
            assertThat(result.failedTokens()).hasSize(1);
            assertThat(result.failedTokens().get(0).errorCode()).isEqualTo("BATCH_SEND_FAILED");
            // 첫 시도 + 3회 재시도 = 4회
            verify(firebaseMessaging, times(4)).sendEach(anyList());
        }

        @Test
        @DisplayName("sendBatch 호출 시 빈 commands 리스트는 successCount=0, failureCount=0, failedTokens 비어있는 결과를 반환한다")
        void shouldHandleEmptyCommands() {
            // when
            SendBatchPushNotificationResult result = fcmPushNotificationClient.sendBatch(List.of());

            // then
            assertThat(result.successCount()).isZero();
            assertThat(result.failureCount()).isZero();
            assertThat(result.failedTokens()).isEmpty();
        }
    }

    private SendPushNotificationCommand createCommand(String deviceToken) {
        return new SendPushNotificationCommand(
            deviceToken, "[알림]", "테스트", "/test", Platform.ANDROID
        );
    }
}
