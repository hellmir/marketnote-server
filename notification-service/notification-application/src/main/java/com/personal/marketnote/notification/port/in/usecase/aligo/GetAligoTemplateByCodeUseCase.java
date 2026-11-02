package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.result.aligo.GetAligoTemplateDetailResult;

public interface GetAligoTemplateByCodeUseCase {

    GetAligoTemplateDetailResult getTemplateByCode(String templateCode);
}
