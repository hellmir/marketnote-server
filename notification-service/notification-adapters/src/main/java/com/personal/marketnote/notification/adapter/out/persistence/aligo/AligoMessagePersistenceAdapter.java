package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoMessageJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoMessageRecipientJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.repository.AligoMessageJpaRepository;
import com.personal.marketnote.notification.domain.aligo.AligoMessage;
import com.personal.marketnote.notification.domain.aligo.AligoMessageNotFoundException;
import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipient;
import com.personal.marketnote.notification.port.out.aligo.FindAligoMessagePort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoMessagePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class AligoMessagePersistenceAdapter
        implements SaveAligoMessagePort, UpdateAligoMessagePort, FindAligoMessagePort {

    private final AligoMessageJpaRepository aligoMessageJpaRepository;

    @Override
    public AligoMessage save(AligoMessage message) {
        List<AligoMessageRecipientJpaEntity> recipientEntities = message.getRecipients().stream()
                .map(r -> AligoMessageRecipientJpaEntity.create(r.getReceiver(), r.getRecvName()))
                .toList();

        AligoMessageJpaEntity entity = AligoMessageJpaEntity.create(
                message.getSenderKey(), message.getTemplateCode(), message.getSender(),
                message.getMessageType(), message.getContent(), message.getTitle(),
                message.getButtons(), message.getMessageStatus(),
                message.getRequestedAt(), recipientEntities
        );

        AligoMessageJpaEntity saved = aligoMessageJpaRepository.save(entity);
        return AligoMessageJpaEntityToDomainMapper.mapToDomain(saved);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void update(AligoMessage message) {
        AligoMessageJpaEntity entity = aligoMessageJpaRepository.findByIdWithRecipients(message.getId())
                .orElseThrow(() -> new AligoMessageNotFoundException(message.getId()));

        if (message.isSent()) {
            entity.updateSendResult(
                    message.getMessageStatus(), message.getAligoMid(),
                    message.getSuccessCount(), message.getFailCount(), message.getSentAt()
            );
            return;
        }
        if (message.isFailed() || message.isCancelled()) {
            entity.updateStatus(message.getMessageStatus());
        }
    }

    @Override
    public Optional<AligoMessage> findById(Long id) {
        return aligoMessageJpaRepository.findByIdWithRecipients(id)
                .map(AligoMessageJpaEntityToDomainMapper::mapToDomain);
    }
}
