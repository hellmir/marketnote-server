package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoTemplateButtonJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoTemplateJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.repository.AligoTemplateJpaRepository;
import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButton;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateNotFoundException;
import com.personal.marketnote.notification.port.out.aligo.DeleteAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplateListPort;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class AligoTemplatePersistenceAdapter
        implements SaveAligoTemplatePort, UpdateAligoTemplatePort, DeleteAligoTemplatePort,
        FindAligoTemplatePort, FindAligoTemplateListPort {

    private final AligoTemplateJpaRepository aligoTemplateJpaRepository;

    @Override
    public AligoTemplate save(AligoTemplate template) {
        List<AligoTemplateButtonJpaEntity> buttonEntities = mapButtonsToEntities(template.getButtons());

        AligoTemplateJpaEntity entity = AligoTemplateJpaEntity.create(
                template.getSenderKey(), template.getTemplateCode(), template.getTemplateName(),
                template.getTemplateContent(), template.getTemplateTitle(), template.getTemplateSubtitle(),
                template.getTemplateType(), template.getTemplateEmType(),
                template.isTemplateSecure(), template.isTemplateAdvert(),
                template.getImageUrl(), template.getImageName(),
                template.getInspStatus(), template.getTemplateStatus(),
                buttonEntities
        );

        AligoTemplateJpaEntity saved = aligoTemplateJpaRepository.save(entity);
        return AligoTemplateJpaEntityToDomainMapper.mapToDomain(saved);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void update(AligoTemplate template) {
        AligoTemplateJpaEntity entity = aligoTemplateJpaRepository.findByTemplateCodeWithButtons(
                template.getTemplateCode()
        ).orElseThrow(() -> new AligoTemplateNotFoundException(template.getTemplateCode()));

        List<AligoTemplateButtonJpaEntity> buttonEntities = mapButtonsToEntities(template.getButtons());

        entity.updateFrom(
                template.getTemplateName(), template.getTemplateContent(),
                template.getTemplateTitle(), template.getTemplateSubtitle(),
                template.getInspStatus(), template.getTemplateStatus(),
                template.getComments(), buttonEntities
        );
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteByTemplateCode(String templateCode) {
        aligoTemplateJpaRepository.deleteByTemplateCode(templateCode);
    }

    @Override
    public Optional<AligoTemplate> findByTemplateCode(String templateCode) {
        return aligoTemplateJpaRepository.findByTemplateCodeWithButtons(templateCode)
                .map(AligoTemplateJpaEntityToDomainMapper::mapToDomain);
    }

    @Override
    public List<AligoTemplate> findAllBySenderKey(String senderKey) {
        return aligoTemplateJpaRepository.findAllBySenderKeyAndStatus(senderKey).stream()
                .map(AligoTemplateJpaEntityToDomainMapper::mapToDomain)
                .toList();
    }

    private List<AligoTemplateButtonJpaEntity> mapButtonsToEntities(List<AligoTemplateButton> buttons) {
        if (FormatValidator.hasNoValue(buttons)) {
            return List.of();
        }
        return buttons.stream()
                .map(btn -> AligoTemplateButtonJpaEntity.create(
                        btn.getOrdering(), btn.getName(), btn.getLinkType(),
                        btn.getLinkMo(), btn.getLinkPc(), btn.getLinkIos(), btn.getLinkAnd()
                ))
                .toList();
    }
}
