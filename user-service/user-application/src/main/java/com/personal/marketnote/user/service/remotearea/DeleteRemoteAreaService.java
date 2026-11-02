package com.personal.marketnote.user.service.remotearea;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.exception.RemoteAreaNotFoundException;
import com.personal.marketnote.user.port.in.usecase.remotearea.DeleteRemoteAreaUseCase;
import com.personal.marketnote.user.port.out.remotearea.DeleteRemoteAreaPort;
import com.personal.marketnote.user.port.out.remotearea.FindRemoteAreaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class DeleteRemoteAreaService implements DeleteRemoteAreaUseCase {

    private final FindRemoteAreaPort findRemoteAreaPort;
    private final DeleteRemoteAreaPort deleteRemoteAreaPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void deleteRemoteArea(Long id) {
        RemoteArea remoteArea = findRemoteAreaPort.findActiveById(id)
                .orElseThrow(() -> new RemoteAreaNotFoundException(id));

        remoteArea.deactivate();
        deleteRemoteAreaPort.deactivate(remoteArea);
    }
}
