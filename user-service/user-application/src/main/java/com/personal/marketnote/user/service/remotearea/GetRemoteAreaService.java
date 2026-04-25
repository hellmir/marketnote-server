package com.personal.marketnote.user.service.remotearea;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.port.in.result.remotearea.GetRemoteAreaResult;
import com.personal.marketnote.user.port.in.usecase.remotearea.GetRemoteAreaUseCase;
import com.personal.marketnote.user.port.out.remotearea.FindRemoteAreaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class GetRemoteAreaService implements GetRemoteAreaUseCase {

    private final FindRemoteAreaPort findRemoteAreaPort;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetRemoteAreaResult getRemoteAreas() {
        List<RemoteArea> remoteAreas = findRemoteAreaPort.findAllActive();
        return GetRemoteAreaResult.from(remoteAreas);
    }
}
