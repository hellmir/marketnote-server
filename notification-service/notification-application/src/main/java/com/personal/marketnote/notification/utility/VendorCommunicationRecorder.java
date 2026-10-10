package com.personal.marketnote.notification.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.domain.notification.FcmSendFailedException;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationSenderType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorName;
import com.personal.marketnote.notification.port.in.command.vendorcommunication.NotificationVendorCommunicationHistoryCommand;
import com.personal.marketnote.notification.port.in.usecase.vendorcommunication.NotificationRecordVendorCommunicationHistoryUseCase;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VendorCommunicationRecorder {

    private final NotificationRecordVendorCommunicationHistoryUseCase notificationRecordVendorCommunicationHistoryUseCase;
    private final ObjectMapper objectMapper;

    public void recordFcmSendResult(
            Notification notification,
            DeviceToken deviceToken,
            String title,
            String body,
            String landingUrl,
            SendPushNotificationResult result
    ) {
        try {
            Map<String, Object> request = buildRequestMap(deviceToken, title, body, landingUrl);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", result.success());
            response.put("messageId", result.messageId());
            response.put("errorCode", result.errorCode());
            response.put("tokenInvalid", result.tokenInvalid());

            String exception = resolveExceptionFromResult(result);
            record(notification, request, response, exception);
        } catch (Exception e) {
            logFailure(notification, deviceToken, e);
        }
    }

    public void recordFcmSendException(
            Notification notification,
            DeviceToken deviceToken,
            String title,
            String body,
            String landingUrl,
            FcmSendFailedException exception
    ) {
        try {
            Map<String, Object> request = buildRequestMap(deviceToken, title, body, landingUrl);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("exceptionType", exception.getClass().getSimpleName());

            record(notification, request, response, exception.getMessage());
        } catch (Exception e) {
            logFailure(notification, deviceToken, e);
        }
    }

    private void record(
            Notification notification,
            Map<String, Object> request,
            Map<String, Object> response,
            String exception
    ) {
        Map<String, Object> payloadMap = new LinkedHashMap<>();
        payloadMap.put("request", request);
        payloadMap.put("response", response);

        JsonNode payloadJson = objectMapper.valueToTree(payloadMap);
        String payload = payloadJson.toString();

        notificationRecordVendorCommunicationHistoryUseCase.record(
                NotificationVendorCommunicationHistoryCommand.builder()
                        .targetType(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION)
                        .targetId(resolveTargetId(notification))
                        .vendorName(NotificationVendorName.FCM)
                        .communicationType(NotificationVendorCommunicationType.REQUEST)
                        .sender(NotificationVendorCommunicationSenderType.SERVER)
                        .exception(exception)
                        .payload(payload)
                        .payloadJson(payloadJson)
                        .build()
        );
    }

    private Map<String, Object> buildRequestMap(
            DeviceToken deviceToken, String title, String body, String landingUrl
    ) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("deviceTokenId", deviceToken.getId());
        request.put("deviceToken", maskDeviceToken(deviceToken.getToken()));
        request.put("platform", resolvePlatformName(deviceToken.getPlatform()));
        request.put("title", title);
        request.put("body", body);
        request.put("landingUrl", landingUrl);
        return request;
    }

    private String resolveExceptionFromResult(SendPushNotificationResult result) {
        if (result.success()) {
            return null;
        }
        return result.errorCode();
    }

    private String resolvePlatformName(Platform platform) {
        if (FormatValidator.hasNoValue(platform)) {
            return null;
        }
        return platform.name();
    }

    private String resolveTargetId(Notification notification) {
        if (FormatValidator.hasNoValue(notification) || FormatValidator.hasNoValue(notification.getId())) {
            return null;
        }
        return String.valueOf(notification.getId());
    }

    private String maskDeviceToken(String deviceToken) {
        if (FormatValidator.hasNoValue(deviceToken)) {
            return deviceToken;
        }
        int length = deviceToken.length();
        if (length <= 8) {
            return "*".repeat(length);
        }
        return deviceToken.substring(0, 4) + "***" + deviceToken.substring(length - 4);
    }

    private void logFailure(Notification notification, DeviceToken deviceToken, Exception e) {
        Long notificationId = FormatValidator.hasValue(notification) ? notification.getId() : null;
        Long deviceTokenId = FormatValidator.hasValue(deviceToken) ? deviceToken.getId() : null;
        log.error(
                "FCM 벤더 통신 이력 기록 실패: notificationId={}, deviceTokenId={}, message={}",
                notificationId, deviceTokenId, e.getMessage(), e
        );
    }
}
