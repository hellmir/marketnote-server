package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.command.RegisterAligoSenderProfileCommand;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterAligoSenderProfileResult;

public interface RegisterAligoSenderProfileUseCase {

    RegisterAligoSenderProfileResult registerSenderProfile(RegisterAligoSenderProfileCommand command);
}
