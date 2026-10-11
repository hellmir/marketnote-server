package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.command.UpdateAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.UpdateAligoTemplateResult;

public interface UpdateAligoTemplateUseCase {

    UpdateAligoTemplateResult updateTemplate(UpdateAligoTemplateCommand command);
}
