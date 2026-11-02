package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.command.AuthenticateAligoSenderProfileCommand;
import com.personal.marketnote.notification.port.in.result.aligo.AuthenticateAligoSenderProfileResult;

public interface AuthenticateAligoSenderProfileUseCase {

    AuthenticateAligoSenderProfileResult authenticateSenderProfile(AuthenticateAligoSenderProfileCommand command);
}
