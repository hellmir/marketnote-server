package com.personal.marketnote.notification.port.in.usecase.device;

import com.personal.marketnote.notification.port.in.command.DeleteDeviceTokenCommand;

public interface DeleteDeviceTokenUseCase {
    void deleteDeviceToken(DeleteDeviceTokenCommand command);
}
