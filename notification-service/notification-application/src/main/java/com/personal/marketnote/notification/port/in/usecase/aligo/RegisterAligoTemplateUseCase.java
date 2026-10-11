package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.command.RegisterAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterAligoTemplateResult;

public interface RegisterAligoTemplateUseCase {

    RegisterAligoTemplateResult registerTemplate(RegisterAligoTemplateCommand command);
}
