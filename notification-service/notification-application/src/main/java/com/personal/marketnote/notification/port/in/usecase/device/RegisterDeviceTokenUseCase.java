package com.personal.marketnote.notification.port.in.usecase.device;

import com.personal.marketnote.notification.port.in.command.RegisterDeviceTokenCommand;
import com.personal.marketnote.notification.port.in.result.device.RegisterDeviceTokenResult;

public interface RegisterDeviceTokenUseCase {
    RegisterDeviceTokenResult registerDeviceToken(RegisterDeviceTokenCommand command);
}
