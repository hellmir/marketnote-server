package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateNotFoundException;
import com.personal.marketnote.notification.domain.aligo.InvalidAligoTemplateTransitionException;
import com.personal.marketnote.notification.port.in.result.aligo.RequestAligoTemplateInspectionResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.RequestAligoTemplateInspectionUseCase;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.RequestAligoTemplateInspectionExternalPort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplatePort;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class RequestAligoTemplateInspectionService implements RequestAligoTemplateInspectionUseCase {

    private final FindAligoTemplatePort findAligoTemplatePort;
    private final RequestAligoTemplateInspectionExternalPort requestAligoTemplateInspectionExternalPort;
    private final UpdateAligoTemplatePort updateAligoTemplatePort;

    @Override
    public RequestAligoTemplateInspectionResult requestInspection(String templateCode) {
        AligoTemplate template = findAligoTemplatePort.findByTemplateCode(templateCode)
                .orElseThrow(() -> new AligoTemplateNotFoundException(templateCode));

        if (!template.canRequestInspection()) {
            throw new InvalidAligoTemplateTransitionException(template.getInspStatus(), "REQ");
        }

        requestAligoTemplateInspectionExternalPort.requestInspection(template.getSenderKey(), templateCode);
        template.markAsRequested();
        updateAligoTemplatePort.update(template);

        return new RequestAligoTemplateInspectionResult(templateCode, template.getInspStatus());
    }
}
