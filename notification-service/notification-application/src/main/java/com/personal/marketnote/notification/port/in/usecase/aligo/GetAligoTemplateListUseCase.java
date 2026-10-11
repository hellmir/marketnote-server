package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.result.aligo.GetAligoTemplateListResult;

public interface GetAligoTemplateListUseCase {

    GetAligoTemplateListResult getTemplateList(String senderKey);
}
