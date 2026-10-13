package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoMessageJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoMessageRecipientJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.repository.AligoMessageJpaRepository;
import com.personal.marketnote.notification.domain.aligo.AligoMessage;
import com.personal.marketnote.notification.domain.aligo.AligoMessageNotFoundException;
import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipient;
import com.personal.marketnote.notification.domain.aligo.AligoMessageStatus;
import com.personal.marketnote.notification.port.out.aligo.FindAligoMessageListPort;
import com.personal.marketnote.notification.port.out.aligo.FindAligoMessagePort;
import com.personal.marketnote.notification.port.out.aligo.FindPendingAligoMessagesPort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoMessagePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoMessagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class AligoMessagePersistenceAdapter
        implements SaveAligoMessagePort, UpdateAligoMessagePort, FindAligoMessagePort,
        FindPendingAligoMessagesPort, FindAligoMessageListPort {

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
                message.getFailoverSubject(), message.getFailoverMessageBody(),
                message.getRequestedAt(), message.getSendDate(), recipientEntities
        );

        AligoMessageJpaEntity saved = aligoMessageJpaRepository.save(entity);
        return AligoMessageJpaEntityToDomainMapper.mapToDomain(saved);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void update(AligoMessage message) {
        AligoMessageJpaEntity entity = aligoMessageJpaRepository.findByIdWithRecipients(message.getId())
                .orElseThrow(() -> new AligoMessageNotFoundException(message.getId()));

        syncRecipientResults(entity, message);

        if (message.isSent()) {
            entity.updateSendResult(
                    message.getMessageStatus(), message.getAligoMid(),
                    message.getSuccessCount(), message.getFailCount(), message.getSentAt()
            );
            return;
        }
        if (message.isFailovered()) {
            List<AligoMessageRecipient> recipients = message.getRecipients();
            String smid = recipients.isEmpty() ? null : recipients.get(0).getSmid();
            entity.updateFailoverResult(
                    message.getMessageStatus(), message.getAligoMid(), smid,
                    message.getSuccessCount(), message.getFailCount(), message.getSentAt()
            );
            return;
        }
        if (message.isDelivered()) {
            entity.updateStatus(message.getMessageStatus());
            return;
        }
        entity.updateStatus(message.getMessageStatus());
    }

    @Override
    public Optional<AligoMessage> findById(Long id) {
        return aligoMessageJpaRepository.findByIdWithRecipients(id)
                .map(AligoMessageJpaEntityToDomainMapper::mapToDomain);
    }

    @Override
    public List<AligoMessage> findPendingMessages(LocalDateTime after) {
        return aligoMessageJpaRepository
                .findByMessageStatusAndCreatedAtAfter(AligoMessageStatus.SENT, after)
                .stream()
                .map(AligoMessageJpaEntityToDomainMapper::mapToDomain)
                .toList();
    }

    @Override
    public List<AligoMessage> findMessages(LocalDateTime startDate, LocalDateTime endDate, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        return aligoMessageJpaRepository.findByCreatedAtBetween(startDate, endDate, pageable)
                .stream()
                .map(AligoMessageJpaEntityToDomainMapper::mapToDomainSummary)
                .toList();
    }

    @Override
    public long countMessages(LocalDateTime startDate, LocalDateTime endDate) {
        return aligoMessageJpaRepository.countByCreatedAtBetween(startDate, endDate);
    }

    private void syncRecipientResults(AligoMessageJpaEntity entity, AligoMessage message) {
        if (FormatValidator.hasNoValue(message.getRecipients())) {
            return;
        }
        for (AligoMessageRecipient domainRecipient : message.getRecipients()) {
            if (!domainRecipient.isResolved()) {
                continue;
            }
            Optional<AligoMessageRecipientJpaEntity> matched = entity.getRecipients().stream()
                    .filter(e -> e.getId().equals(domainRecipient.getId()))
                    .findFirst();
            if (matched.isEmpty()) {
                log.warn("수신자 엔티티 ID 불일치: messageId={}, recipientId={}",
                        message.getId(), domainRecipient.getId());
                continue;
            }
            matched.get().updatePollingResult(
                    domainRecipient.getRecipientStatus(),
                    domainRecipient.getMsgId(),
                    domainRecipient.getRslt(),
                    domainRecipient.getRsltMessage(),
                    domainRecipient.getSentAt(),
                    domainRecipient.getResultAt(),
                    domainRecipient.getReportAt()
            );
        }
    }
}
