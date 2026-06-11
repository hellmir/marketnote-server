package com.personal.marketnote.notification.service.device;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.DeviceTokenCreateState;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.port.in.command.RegisterDeviceTokenCommand;
import com.personal.marketnote.notification.port.in.result.device.RegisterDeviceTokenResult;
import com.personal.marketnote.notification.port.in.usecase.device.RegisterDeviceTokenUseCase;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.SaveDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.UpdateDeviceTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class RegisterDeviceTokenService implements RegisterDeviceTokenUseCase {

    private final FindDeviceTokenPort findDeviceTokenPort;
    private final SaveDeviceTokenPort saveDeviceTokenPort;
    private final UpdateDeviceTokenPort updateDeviceTokenPort;
    private final DeleteDeviceTokenPort deleteDeviceTokenPort;
    private final Clock clock;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public RegisterDeviceTokenResult registerDeviceToken(RegisterDeviceTokenCommand command) {
        Platform platform = Platform.from(command.platform());
        LocalDateTime now = LocalDateTime.now(clock);

        Optional<DeviceToken> existing = findDeviceTokenPort.findActiveByDeviceId(command.deviceId());
        if (existing.isEmpty()) {
            return createNewDeviceToken(command, platform, now);
        }

        DeviceToken existingToken = existing.get();
        if (!existingToken.isOwnedBy(command.userId())) {
            deleteDeviceTokenPort.deleteById(existingToken.getId());
            return createNewDeviceToken(command, platform, now);
        }

        existingToken.updateToken(command.token(), platform, now);
        Long updatedId = updateDeviceTokenPort.update(existingToken);
        return RegisterDeviceTokenResult.ofUpdated(updatedId);
    }

    private RegisterDeviceTokenResult createNewDeviceToken(
            RegisterDeviceTokenCommand command, Platform platform, LocalDateTime now) {
        DeviceToken deviceToken = DeviceToken.from(DeviceTokenCreateState.builder()
                .userId(command.userId())
                .token(command.token())
                .platform(platform)
                .deviceId(command.deviceId())
                .lastUsedAt(now)
                .build());
        Long id = saveDeviceTokenPort.save(deviceToken);
        return RegisterDeviceTokenResult.ofCreated(id);
    }
}
