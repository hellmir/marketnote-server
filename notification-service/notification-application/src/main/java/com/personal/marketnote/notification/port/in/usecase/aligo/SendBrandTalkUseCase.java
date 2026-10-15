package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.command.SendBrandTalkCommand;
import com.personal.marketnote.notification.port.in.result.aligo.SendBrandTalkResult;

public interface SendBrandTalkUseCase {

    SendBrandTalkResult sendBrandTalk(SendBrandTalkCommand command);
}
