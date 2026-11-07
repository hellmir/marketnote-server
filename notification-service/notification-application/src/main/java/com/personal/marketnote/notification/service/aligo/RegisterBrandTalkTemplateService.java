package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplate;
import com.personal.marketnote.notification.mapper.AligoBrandTalkTemplateCommandToStateMapper;
import com.personal.marketnote.notification.port.in.command.RegisterBrandTalkTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterBrandTalkTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.RegisterBrandTalkTemplateUseCase;
import com.personal.marketnote.notification.port.out.aligo.RegisterBrandTalkTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.SaveBrandTalkTemplatePort;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class RegisterBrandTalkTemplateService implements RegisterBrandTalkTemplateUseCase {

    private final RegisterBrandTalkTemplateExternalPort registerBrandTalkTemplateExternalPort;
    private final SaveBrandTalkTemplatePort saveBrandTalkTemplatePort;

    @Override
    public RegisterBrandTalkTemplateResult registerTemplate(RegisterBrandTalkTemplateCommand command) {
        AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(
                AligoBrandTalkTemplateCommandToStateMapper.mapToCreateState(command)
        );

        registerBrandTalkTemplateExternalPort.registerTemplate(
                template, command.imageFile(), command.imageFileName(),
                command.carouselItemImageFiles(), command.carouselItemImageFileNames()
        );

        AligoBrandTalkTemplate saved = saveBrandTalkTemplatePort.save(template);

        return new RegisterBrandTalkTemplateResult(
                saved.getId(), saved.getTemplateCode(),
                saved.getTemplateName(), saved.getTemplateType().name()
        );
    }
}
