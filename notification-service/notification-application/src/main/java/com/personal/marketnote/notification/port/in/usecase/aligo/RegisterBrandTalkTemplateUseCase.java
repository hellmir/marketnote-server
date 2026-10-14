package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.command.RegisterBrandTalkTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterBrandTalkTemplateResult;

public interface RegisterBrandTalkTemplateUseCase {

    RegisterBrandTalkTemplateResult registerTemplate(RegisterBrandTalkTemplateCommand command);
}
