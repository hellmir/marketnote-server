package com.personal.marketnote.notification.service.device;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.DeviceTokenNotFoundException;
import com.personal.marketnote.notification.port.in.command.DeleteDeviceTokenCommand;
import com.personal.marketnote.notification.port.in.usecase.device.DeleteDeviceTokenUseCase;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class DeleteDeviceTokenService implements DeleteDeviceTokenUseCase {

    private final FindDeviceTokenPort findDeviceTokenPort;
    private final DeleteDeviceTokenPort deleteDeviceTokenPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void deleteDeviceToken(DeleteDeviceTokenCommand command) {
        DeviceToken deviceToken = findDeviceTokenPort.findActiveByDeviceId(command.deviceId())
                .orElseThrow(DeviceTokenNotFoundException::new);

        if (!deviceToken.isOwnedBy(command.userId())) {
            throw new DeviceTokenNotFoundException();
        }

        deleteDeviceTokenPort.deleteById(deviceToken.getId());
    }
}
