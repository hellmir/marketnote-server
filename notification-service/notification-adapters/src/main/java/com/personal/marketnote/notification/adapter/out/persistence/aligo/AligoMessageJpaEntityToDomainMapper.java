package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoMessageJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoMessageRecipientJpaEntity;
import com.personal.marketnote.notification.domain.aligo.AligoMessage;
import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipientSnapshotState;
import com.personal.marketnote.notification.domain.aligo.AligoMessageSnapshotState;

import java.util.List;

public final class AligoMessageJpaEntityToDomainMapper {

    private AligoMessageJpaEntityToDomainMapper() {
    }

    public static AligoMessage mapToDomain(AligoMessageJpaEntity entity) {
        List<AligoMessageRecipientSnapshotState> recipientStates = entity.getRecipients().stream()
                .map(AligoMessageJpaEntityToDomainMapper::mapRecipientToSnapshotState)
                .toList();

        return AligoMessage.from(
                AligoMessageSnapshotState.builder()
                        .id(entity.getId())
                        .senderKey(entity.getSenderKey())
                        .templateCode(entity.getTemplateCode())
                        .sender(entity.getSender())
                        .messageType(entity.getMessageType())
                        .content(entity.getContent())
                        .title(entity.getTitle())
                        .buttons(entity.getButtons())
                        .messageStatus(entity.getMessageStatus())
                        .aligoMid(entity.getAligoMid())
                        .successCount(entity.getSuccessCount())
                        .failCount(entity.getFailCount())
                        .failoverUsed(entity.isFailoverUsed())
                        .failoverSubject(entity.getFailoverSubject())
                        .failoverMessageBody(entity.getFailoverMessageBody())
                        .requestedAt(entity.getRequestedAt())
                        .sentAt(entity.getSentAt())
                        .recipients(recipientStates)
                        .status(entity.getStatus())
                        .createdAt(entity.getCreatedAt())
                        .modifiedAt(entity.getModifiedAt())
                        .build()
        );
    }

    private static AligoMessageRecipientSnapshotState mapRecipientToSnapshotState(AligoMessageRecipientJpaEntity entity) {
        return AligoMessageRecipientSnapshotState.builder()
                .id(entity.getId())
                .receiver(entity.getReceiver())
                .recvName(entity.getRecvName())
                .msgId(entity.getMsgId())
                .smid(entity.getSmid())
                .recipientStatus(entity.getRecipientStatus())
                .rslt(entity.getRslt())
                .rsltMessage(entity.getRsltMessage())
                .sentAt(entity.getSentAt())
                .resultAt(entity.getResultAt())
                .reportAt(entity.getReportAt())
                .build();
    }
}
