package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.result.aligo.GetBrandTalkTemplateListResult;

public interface GetBrandTalkTemplateListUseCase {

    GetBrandTalkTemplateListResult getTemplateList(String senderKey, int page, int pageSize);
}
