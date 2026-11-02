package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateNotFoundException;
import com.personal.marketnote.notification.port.in.command.DeleteAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.usecase.aligo.DeleteAligoTemplateUseCase;
import com.personal.marketnote.notification.port.out.aligo.DeleteAligoTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.DeleteAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class DeleteAligoTemplateService implements DeleteAligoTemplateUseCase {

    private final FindAligoTemplatePort findAligoTemplatePort;
    private final DeleteAligoTemplateExternalPort deleteAligoTemplateExternalPort;
    private final DeleteAligoTemplatePort deleteAligoTemplatePort;

    @Override
    public void deleteTemplate(DeleteAligoTemplateCommand command) {
        AligoTemplate template = findAligoTemplatePort.findByTemplateCode(command.templateCode())
                .orElseThrow(() -> new AligoTemplateNotFoundException(command.templateCode()));
        template.validateDeletion();

        deleteAligoTemplateExternalPort.deleteTemplate(command.senderKey(), command.templateCode());

        deleteAligoTemplatePort.deleteByTemplateCode(command.templateCode());
    }
}
