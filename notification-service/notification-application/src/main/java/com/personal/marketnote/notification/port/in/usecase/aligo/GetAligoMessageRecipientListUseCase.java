package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageRecipientListResult;

public interface GetAligoMessageRecipientListUseCase {

    GetAligoMessageRecipientListResult getRecipientList(Long messageId);
}
