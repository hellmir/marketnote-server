package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.command.DeleteAligoTemplateCommand;

public interface DeleteAligoTemplateUseCase {

    void deleteTemplate(DeleteAligoTemplateCommand command);
}
