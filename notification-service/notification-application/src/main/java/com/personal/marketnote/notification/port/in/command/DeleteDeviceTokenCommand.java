package com.personal.marketnote.notification.port.in.command;

import com.personal.marketnote.common.utility.FormatValidator;

public record DeleteDeviceTokenCommand(
        Long userId,
        String deviceId
) {

    @Override
    public String toString() {
        return "DeleteDeviceTokenCommand{"
                + "userId=" + userId
                + ", deviceId=" + maskDeviceId(deviceId)
                + "}";
    }

    private static String maskDeviceId(String deviceId) {
        if (FormatValidator.hasNoValue(deviceId) || deviceId.length() < 8) {
            return "***";
        }
        return deviceId.substring(0, 4) + "***" + deviceId.substring(deviceId.length() - 4);
    }
}
