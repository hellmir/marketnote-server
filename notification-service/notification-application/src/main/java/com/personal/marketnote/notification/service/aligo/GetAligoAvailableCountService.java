package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoAvailableCountResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoAvailableCountUseCase;
import com.personal.marketnote.notification.port.out.aligo.GetAligoHeartInfoExternalPort;
import com.personal.marketnote.notification.port.out.aligo.GetAligoHeartInfoExternalPort.AligoHeartInfoResult;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetAligoAvailableCountService implements GetAligoAvailableCountUseCase {

    private final GetAligoHeartInfoExternalPort getAligoHeartInfoExternalPort;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public GetAligoAvailableCountResult getAvailableCount() {
        AligoHeartInfoResult heartInfo = getAligoHeartInfoExternalPort.getHeartInfo();
        return new GetAligoAvailableCountResult(
                heartInfo.smsCnt(), heartInfo.lmsCnt(), heartInfo.mmsCnt(), heartInfo.altCnt()
        );
    }
}
