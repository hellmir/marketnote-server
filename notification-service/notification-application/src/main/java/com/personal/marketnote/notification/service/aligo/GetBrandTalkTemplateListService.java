package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplateSnapshotState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateStatus;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateType;
import com.personal.marketnote.notification.port.in.result.aligo.GetBrandTalkTemplateListResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetBrandTalkTemplateListUseCase;
import com.personal.marketnote.notification.port.out.aligo.FindBrandTalkTemplateListPort;
import com.personal.marketnote.notification.port.out.aligo.GetBrandTalkTemplateListExternalPort;
import com.personal.marketnote.notification.port.out.aligo.SaveBrandTalkTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateBrandTalkTemplatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class GetBrandTalkTemplateListService implements GetBrandTalkTemplateListUseCase {

    private final GetBrandTalkTemplateListExternalPort getTemplateListExternalPort;
    private final FindBrandTalkTemplateListPort findBrandTalkTemplateListPort;
    private final SaveBrandTalkTemplatePort saveBrandTalkTemplatePort;
    private final UpdateBrandTalkTemplatePort updateBrandTalkTemplatePort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public GetBrandTalkTemplateListResult getTemplateList(String senderKey, int page, int pageSize) {
        GetBrandTalkTemplateListResult.ExternalListResponse externalResponse =
                getTemplateListExternalPort.getTemplateList(senderKey, page, pageSize);

        syncTemplates(senderKey, externalResponse.templates());

        List<GetBrandTalkTemplateListResult.BrandTalkTemplateItem> items = externalResponse.templates().stream()
                .map(this::toItem)
                .toList();

        return new GetBrandTalkTemplateListResult(externalResponse.pageInfo(), items);
    }

    private void syncTemplates(
            String senderKey,
            List<GetBrandTalkTemplateListResult.ExternalTemplateItem> externalTemplates
    ) {
        List<AligoBrandTalkTemplate> localTemplates = findBrandTalkTemplateListPort.findAllBySenderKey(senderKey);
        Map<String, AligoBrandTalkTemplate> localMap = localTemplates.stream()
                .collect(Collectors.toMap(AligoBrandTalkTemplate::getTemplateCode, t -> t));

        for (GetBrandTalkTemplateListResult.ExternalTemplateItem external : externalTemplates) {
            AligoBrandTalkTemplate local = localMap.get(external.templateCode());

            if (FormatValidator.hasNoValue(local)) {
                saveNewTemplate(senderKey, external);
                continue;
            }

            updateExistingTemplate(local, external);
        }
    }

    private void saveNewTemplate(
            String senderKey, GetBrandTalkTemplateListResult.ExternalTemplateItem external
    ) {
        AligoBrandTalkTemplate newTemplate = AligoBrandTalkTemplate.from(
                AligoBrandTalkTemplateSnapshotState.builder()
                        .senderKey(senderKey)
                        .templateCode(external.templateCode())
                        .templateName(external.templateName())
                        .templateType(BrandTalkTemplateType.from(external.templateType()))
                        .templateContent("")
                        .templateStatus(BrandTalkTemplateStatus.from(external.templateStatus()))
                        .build()
        );
        saveBrandTalkTemplatePort.save(newTemplate);
    }

    private void updateExistingTemplate(
            AligoBrandTalkTemplate local, GetBrandTalkTemplateListResult.ExternalTemplateItem external
    ) {
        BrandTalkTemplateStatus externalStatus = BrandTalkTemplateStatus.from(external.templateStatus());
        if (local.hasTemplateStatus(externalStatus)) {
            return;
        }
        local.updateTemplateStatus(externalStatus);
        updateBrandTalkTemplatePort.update(local);
    }

    private GetBrandTalkTemplateListResult.BrandTalkTemplateItem toItem(
            GetBrandTalkTemplateListResult.ExternalTemplateItem external
    ) {
        return new GetBrandTalkTemplateListResult.BrandTalkTemplateItem(
                external.templateCode(),
                external.templateName(),
                external.templateType(),
                external.templateStatus(),
                external.sendVariable(),
                external.createdAt()
        );
    }
}
