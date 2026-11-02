package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.port.in.result.aligo.GetBrandTalkTemplateListResult;

public interface GetBrandTalkTemplateListExternalPort {

    GetBrandTalkTemplateListResult.ExternalListResponse getTemplateList(String senderKey, int page, int pageSize);
}
