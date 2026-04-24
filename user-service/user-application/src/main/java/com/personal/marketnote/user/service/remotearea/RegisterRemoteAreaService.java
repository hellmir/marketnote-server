package com.personal.marketnote.user.service.remotearea;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.domain.remotearea.RemoteAreaCreateState;
import com.personal.marketnote.user.exception.RemoteAreaAlreadyExistsException;
import com.personal.marketnote.user.port.in.command.remotearea.RegisterRemoteAreaCommand;
import com.personal.marketnote.user.port.in.usecase.remotearea.RegisterRemoteAreaUseCase;
import com.personal.marketnote.user.port.out.remotearea.FindRemoteAreaPort;
import com.personal.marketnote.user.port.out.remotearea.SaveRemoteAreaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class RegisterRemoteAreaService implements RegisterRemoteAreaUseCase {

    private final FindRemoteAreaPort findRemoteAreaPort;
    private final SaveRemoteAreaPort saveRemoteAreaPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void registerRemoteArea(RegisterRemoteAreaCommand command) {
        RemoteArea remoteArea = RemoteArea.from(
                RemoteAreaCreateState.builder()
                        .province(command.province())
                        .district(command.district())
                        .village(command.village())
                        .subarea(command.subarea())
                        .build()
        );

        validateNotDuplicate(remoteArea);

        saveRemoteAreaPort.save(remoteArea);
    }

    private void validateNotDuplicate(RemoteArea remoteArea) {
        if (findRemoteAreaPort.existsByAddress(remoteArea.getProvince(), remoteArea.getDistrict(), remoteArea.getVillage(), remoteArea.getSubarea())) {
            throw new RemoteAreaAlreadyExistsException(remoteArea.getProvince(), remoteArea.getDistrict(), remoteArea.getVillage(), remoteArea.getSubarea());
        }
    }
}
