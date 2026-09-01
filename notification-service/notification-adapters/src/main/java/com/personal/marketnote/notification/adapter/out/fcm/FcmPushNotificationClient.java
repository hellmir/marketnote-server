package com.personal.marketnote.notification.adapter.out.fcm;

import com.google.firebase.messaging.*;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.domain.notification.FcmSendFailedException;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.notification.SendPushNotificationPort;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FcmPushNotificationClient implements SendPushNotificationPort {

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
}
