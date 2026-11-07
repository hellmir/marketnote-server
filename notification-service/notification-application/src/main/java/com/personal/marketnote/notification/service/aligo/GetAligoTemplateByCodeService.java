package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButton;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateNotFoundException;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoTemplateDetailResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoTemplateByCodeUseCase;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class GetAligoTemplateByCodeService implements GetAligoTemplateByCodeUseCase {

    private final FindAligoTemplatePort findAligoTemplatePort;

    @Override
    @Cacheable(value = "aligo:template:active", key = "#templateCode")
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public GetAligoTemplateDetailResult getTemplateByCode(String templateCode) {
        AligoTemplate template = findAligoTemplatePort.findByTemplateCode(templateCode)
                .orElseThrow(() -> {
                    log.warn("알림톡 템플릿을 찾을 수 없습니다. templateCode={}", templateCode);
                    return new AligoTemplateNotFoundException();
                });

        return toDetailResult(template);
    }

    private GetAligoTemplateDetailResult toDetailResult(AligoTemplate template) {
        List<GetAligoTemplateDetailResult.ButtonResult> buttons = template.getButtons().stream()
                .map(this::toButtonResult)
                .toList();

        return new GetAligoTemplateDetailResult(
                template.getId(), template.getSenderKey(),
                template.getTemplateCode(), template.getTemplateName(),
                template.getTemplateContent(), template.getTemplateTitle(),
                template.getTemplateSubtitle(), template.getTemplateType().name(),
                template.getTemplateEmType().name(), template.isTemplateSecure(),
                template.isTemplateAdvert(), template.getImageUrl(),
                template.getImageName(), template.getInspStatus(),
                template.getTemplateStatus().name(), template.getComments(),
                buttons
        );
    }

    private GetAligoTemplateDetailResult.ButtonResult toButtonResult(AligoTemplateButton button) {
        return new GetAligoTemplateDetailResult.ButtonResult(
                button.getOrdering(), button.getName(), button.getLinkType().name(),
                button.getLinkMo(), button.getLinkPc(), button.getLinkIos(), button.getLinkAnd()
        );
    }
}
