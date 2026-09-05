package com.personal.marketnote.notification.adapter.out.fcm;

import com.google.firebase.messaging.*;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.domain.notification.FcmSendFailedException;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.notification.SendPushNotificationPort;
import com.personal.marketnote.notification.port.out.result.SendBatchPushNotificationResult;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class FcmPushNotificationClient implements SendPushNotificationPort {

    private static final int BATCH_CHUNK_SIZE = 500;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 1000L;

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public SendPushNotificationResult send(SendPushNotificationCommand command) {
        Message message = buildMessage(command);

        try {
            String messageId = firebaseMessaging.send(message);
            log.info("FCM 발송 성공: messageId={}", messageId);
            return SendPushNotificationResult.success(messageId);
        } catch (FirebaseMessagingException fme) {
            return handleFirebaseMessagingException(fme);
        } catch (Exception e) {
            log.error("FCM 발송 중 예상치 못한 오류: {}", e.getMessage());
            throw new FcmSendFailedException("FCM 발송 중 예상치 못한 오류 발생");
        }
    }

    private Message buildMessage(SendPushNotificationCommand command) {
        Message.Builder builder = Message.builder()
                .setToken(command.deviceToken())
                .setNotification(Notification.builder()
                        .setTitle(command.title())
                        .setBody(command.body())
                        .build())
                .putData("url", command.url());

        applyPlatformConfig(builder, command.platform());

        return builder.build();
    }

    private void applyPlatformConfig(Message.Builder builder, Platform platform) {
        if (platform.isAndroid()) {
            builder.setAndroidConfig(AndroidConfig.builder()
                    .setNotification(AndroidNotification.builder()
                            .setChannelId("default")
                            .setSound("default")
                            .build())
                    .build());
            return;
        }

        builder.setApnsConfig(ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setSound("default")
                        .setBadge(1)
                        .setContentAvailable(true)
                        .build())
                .build());
    }

    private SendPushNotificationResult handleFirebaseMessagingException(FirebaseMessagingException fme) {
        MessagingErrorCode errorCode = fme.getMessagingErrorCode();
        String errorCodeName = errorCode != null ? errorCode.name() : "UNKNOWN";

        if (isTokenInvalid(errorCode)) {
            log.warn("FCM 토큰 무효: errorCode={}", errorCodeName);
            return SendPushNotificationResult.tokenInvalid(errorCodeName);
        }

        log.error("FCM 발송 실패: errorCode={}", errorCodeName);
        return SendPushNotificationResult.failure(errorCodeName);
    }

    private boolean isTokenInvalid(MessagingErrorCode errorCode) {
        return errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode == MessagingErrorCode.INVALID_ARGUMENT;
    }

    @Override
    public SendBatchPushNotificationResult sendBatch(List<SendPushNotificationCommand> commands) {
        int totalSuccess = 0;
        int totalFailure = 0;
        List<SendBatchPushNotificationResult.FailedToken> allFailedTokens = new ArrayList<>();

        for (int i = 0; i < commands.size(); i += BATCH_CHUNK_SIZE) {
            int end = Math.min(i + BATCH_CHUNK_SIZE, commands.size());
            List<SendPushNotificationCommand> chunk = commands.subList(i, end);

            List<Message> messages = chunk.stream()
                    .map(this::buildMessage)
                    .toList();

            BatchResponse response = sendWithRetry(messages);
            if (response == null) {
                totalFailure += chunk.size();
                for (SendPushNotificationCommand cmd : chunk) {
                    allFailedTokens.add(new SendBatchPushNotificationResult.FailedToken(
                            cmd.deviceToken(), "BATCH_SEND_FAILED", false));
                }
                continue;
            }

            totalSuccess += response.getSuccessCount();
            totalFailure += response.getFailureCount();

            List<SendResponse> responses = response.getResponses();
            for (int j = 0; j < responses.size(); j++) {
                SendResponse sendResponse = responses.get(j);
                if (sendResponse.isSuccessful()) {
                    continue;
                }
                FirebaseMessagingException exception = sendResponse.getException();
                MessagingErrorCode errorCode = exception != null ? exception.getMessagingErrorCode() : null;
                String errorCodeName = errorCode != null ? errorCode.name() : "UNKNOWN";
                allFailedTokens.add(new SendBatchPushNotificationResult.FailedToken(
                        chunk.get(j).deviceToken(), errorCodeName, isTokenInvalid(errorCode)));
            }
        }

        log.info("FCM 대량 발송 완료: 성공={}, 실패={}", totalSuccess, totalFailure);
        return new SendBatchPushNotificationResult(totalSuccess, totalFailure, allFailedTokens);
    }

    private BatchResponse sendWithRetry(List<Message> messages) {
        long backoffMs = INITIAL_BACKOFF_MS;
        for (int attempt = 0; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            if (Thread.currentThread().isInterrupted()) {
                log.warn("FCM 대량 발송 재시도 중 인터럽트 감지, 중단");
                return null;
            }
            try {
                return firebaseMessaging.sendEach(messages);
            } catch (FirebaseMessagingException fme) {
                if (fme.getMessagingErrorCode() != MessagingErrorCode.QUOTA_EXCEEDED) {
                    log.error("FCM 대량 발송 실패 (재시도 불가): errorCode={}", fme.getMessagingErrorCode());
                    return null;
                }
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    log.error("FCM 대량 발송 재시도 횟수 초과: attempts={}", MAX_RETRY_ATTEMPTS);
                    return null;
                }
                log.warn("FCM 429 QUOTA_EXCEEDED, {}ms 후 재시도 (attempt={})", backoffMs, attempt + 1);
                sleep(backoffMs);
                backoffMs *= 2;
            }
        }
        return null;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new FcmSendFailedException("FCM 재시도 중 인터럽트 발생");
        }
    }
}
