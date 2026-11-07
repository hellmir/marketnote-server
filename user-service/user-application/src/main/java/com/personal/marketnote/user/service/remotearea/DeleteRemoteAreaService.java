package com.personal.marketnote.user.service.remotearea;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.exception.RemoteAreaNotFoundException;
import com.personal.marketnote.user.port.in.usecase.remotearea.DeleteRemoteAreaUseCase;
import com.personal.marketnote.user.port.out.remotearea.DeleteRemoteAreaPort;
import com.personal.marketnote.user.port.out.remotearea.FindRemoteAreaPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class DeleteRemoteAreaService implements DeleteRemoteAreaUseCase {

    private final FindRemoteAreaPort findRemoteAreaPort;
    private final DeleteRemoteAreaPort deleteRemoteAreaPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void deleteRemoteArea(Long id) {
        RemoteArea remoteArea = findRemoteAreaPort.findActiveById(id)
                .orElseThrow(() -> {
                    log.warn("도서산간 지역을 찾을 수 없습니다. remoteAreaId={}", id);
                    return new RemoteAreaNotFoundException();
                });

        remoteArea.deactivate();
        deleteRemoteAreaPort.deactivate(remoteArea);
    }
}
