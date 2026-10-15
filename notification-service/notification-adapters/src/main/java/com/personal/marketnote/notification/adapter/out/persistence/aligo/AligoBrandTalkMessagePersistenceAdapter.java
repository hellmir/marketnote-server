package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoBrandTalkMessageJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoBrandTalkMessageRecipientJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.repository.AligoBrandTalkMessageJpaRepository;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessage;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageNotFoundException;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageRecipient;
import com.personal.marketnote.notification.port.out.aligo.FindBrandTalkMessagePort;
import com.personal.marketnote.notification.port.out.aligo.SaveBrandTalkMessagePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateBrandTalkMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@PersistenceAdapter
@RequiredArgsConstructor
public class AligoBrandTalkMessagePersistenceAdapter
        implements SaveBrandTalkMessagePort, UpdateBrandTalkMessagePort, FindBrandTalkMessagePort {

    private final AligoBrandTalkMessageJpaRepository repository;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public AligoBrandTalkMessage save(AligoBrandTalkMessage message) {
        List<AligoBrandTalkMessageRecipientJpaEntity> recipientEntities = message.getRecipients().stream()
                .map(this::toRecipientEntity)
                .toList();

        AligoBrandTalkMessageJpaEntity entity = AligoBrandTalkMessageJpaEntity.create(
                message.getSenderKey(), message.getSender(), message.getKakaoTarget(),
                message.isAdvertYn(), message.getTemplateCode(),
                message.isFailoverEnabled(), message.getFailoverSubject(), message.getFailoverType(),
                message.getRequestedAt(), recipientEntities
        );

        AligoBrandTalkMessageJpaEntity saved = repository.save(entity);
        return AligoBrandTalkMessageJpaEntityToDomainMapper.mapToDomain(saved);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void update(AligoBrandTalkMessage message) {
        AligoBrandTalkMessageJpaEntity entity = repository.findByIdWithRecipients(message.getId())
                .orElseThrow(() -> new AligoBrandTalkMessageNotFoundException(message.getId()));

        if (message.isSent()) {
            entity.updateSendResult(
                    message.getAligoMid(), message.getCostType(),
                    message.getUnitCost(), message.getTotalCost(),
                    message.getSuccessCount(), message.getFailCount(),
                    message.getMessageStatus(), message.getSentAt()
            );
            return;
        }
        entity.updateMessageStatus(message.getMessageStatus());
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public Optional<AligoBrandTalkMessage> findById(Long id) {
        return repository.findByIdWithRecipients(id)
                .map(AligoBrandTalkMessageJpaEntityToDomainMapper::mapToDomain);
    }

    private AligoBrandTalkMessageRecipientJpaEntity toRecipientEntity(AligoBrandTalkMessageRecipient recipient) {
        return AligoBrandTalkMessageRecipientJpaEntity.create(recipient.getReceiver(), recipient.getReceiverMessage());
    }
}
