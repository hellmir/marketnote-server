package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoCategoryTreeResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoCategoryUseCase;
import com.personal.marketnote.notification.port.out.aligo.GetAligoCategoryExternalPort;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetAligoCategoryService implements GetAligoCategoryUseCase {

    private final GetAligoCategoryExternalPort getAligoCategoryExternalPort;

    @Override
    public GetAligoCategoryTreeResult getCategoryTree() {
        return getAligoCategoryExternalPort.getCategoryTree();
    }
}
