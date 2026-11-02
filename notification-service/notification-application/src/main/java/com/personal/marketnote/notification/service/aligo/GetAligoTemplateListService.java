package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoTemplateListResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoTemplateListUseCase;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplateListPort;
import com.personal.marketnote.notification.port.out.aligo.GetAligoTemplateListExternalPort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplatePort;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UseCase
@RequiredArgsConstructor
public class GetAligoTemplateListService implements GetAligoTemplateListUseCase {

    private final GetAligoTemplateListExternalPort getAligoTemplateListExternalPort;
    private final FindAligoTemplateListPort findAligoTemplateListPort;
    private final SaveAligoTemplatePort saveAligoTemplatePort;
    private final UpdateAligoTemplatePort updateAligoTemplatePort;

    @Override
    public GetAligoTemplateListResult getTemplateList(String senderKey) {
        List<AligoTemplate> externalTemplates = getAligoTemplateListExternalPort.getTemplateList(senderKey);
        List<AligoTemplate> localTemplates = findAligoTemplateListPort.findAllBySenderKey(senderKey);

        List<AligoTemplate> synced = syncTemplates(externalTemplates, localTemplates);

        List<GetAligoTemplateListResult.AligoTemplateResult> results = synced.stream()
                .map(this::toResult)
                .toList();

        return new GetAligoTemplateListResult(results);
    }

    private List<AligoTemplate> syncTemplates(
            List<AligoTemplate> externalTemplates, List<AligoTemplate> localTemplates
    ) {
        Map<String, AligoTemplate> localMap = localTemplates.stream()
                .collect(Collectors.toMap(AligoTemplate::getTemplateCode, t -> t));

        List<AligoTemplate> synced = new ArrayList<>();

        for (AligoTemplate external : externalTemplates) {
            AligoTemplate local = localMap.get(external.getTemplateCode());

            if (FormatValidator.hasNoValue(local)) {
                AligoTemplate saved = saveAligoTemplatePort.save(external);
                synced.add(saved);
                continue;
            }

            local.updateInspStatus(external.getInspStatus());
            local.updateTemplateStatus(external.getTemplateStatus());
            updateAligoTemplatePort.update(local);
            synced.add(local);
        }

        return synced;
    }

    private GetAligoTemplateListResult.AligoTemplateResult toResult(AligoTemplate template) {
        return new GetAligoTemplateListResult.AligoTemplateResult(
                template.getId(),
                template.getTemplateCode(),
                template.getTemplateName(),
                template.getTemplateType().name(),
                template.getInspStatus(),
                template.getTemplateStatus().name(),
                template.getComments()
        );
    }
}
