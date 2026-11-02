package com.personal.marketnote.notification.port.in.command;

import com.personal.marketnote.common.utility.FormatValidator;

public record RegisterDeviceTokenCommand(
        Long userId,
        String token,
        String platform,
        String deviceId
) {

    @Override
    public String toString() {
        return "RegisterDeviceTokenCommand{"
                + "userId=" + userId
                + ", token=***MASKED***"
                + ", platform=" + platform
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
