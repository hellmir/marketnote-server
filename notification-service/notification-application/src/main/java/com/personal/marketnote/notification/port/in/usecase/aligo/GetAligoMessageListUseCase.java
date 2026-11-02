package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.command.GetAligoMessageListCommand;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageListResult;

public interface GetAligoMessageListUseCase {

    GetAligoMessageListResult getMessageList(GetAligoMessageListCommand command);
}
