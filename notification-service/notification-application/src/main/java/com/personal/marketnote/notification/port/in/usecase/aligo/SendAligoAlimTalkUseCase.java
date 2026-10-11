package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.command.SendAligoAlimTalkCommand;
import com.personal.marketnote.notification.port.in.result.aligo.SendAligoAlimTalkResult;

public interface SendAligoAlimTalkUseCase {

    SendAligoAlimTalkResult sendAlimTalk(SendAligoAlimTalkCommand command);
}
