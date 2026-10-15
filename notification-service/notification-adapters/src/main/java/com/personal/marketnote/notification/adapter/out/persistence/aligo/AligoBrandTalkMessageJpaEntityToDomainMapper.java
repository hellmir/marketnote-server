package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoBrandTalkMessageJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoBrandTalkMessageRecipientJpaEntity;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessage;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageRecipientSnapshotState;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageSnapshotState;

import java.util.List;

public class AligoBrandTalkMessageJpaEntityToDomainMapper {

    private AligoBrandTalkMessageJpaEntityToDomainMapper() {
    }

    public static AligoBrandTalkMessage mapToDomain(AligoBrandTalkMessageJpaEntity entity) {
        List<AligoBrandTalkMessageRecipientSnapshotState> recipientStates = entity.getRecipients().stream()
                .map(AligoBrandTalkMessageJpaEntityToDomainMapper::mapRecipientToState)
                .toList();

        return AligoBrandTalkMessage.from(
                AligoBrandTalkMessageSnapshotState.builder()
                        .id(entity.getId())
                        .senderKey(entity.getSenderKey())
                        .sender(entity.getSender())
                        .kakaoTarget(entity.getKakaoTarget())
                        .advertYn(entity.isAdvertYn())
                        .templateCode(entity.getTemplateCode())
                        .messageStatus(entity.getMessageStatus())
                        .costType(entity.getCostType())
                        .unitCost(entity.getUnitCost())
                        .totalCost(entity.getTotalCost())
                        .aligoMid(entity.getAligoMid())
                        .successCount(entity.getSuccessCount())
                        .failCount(entity.getFailCount())
                        .failoverEnabled(entity.isFailoverEnabled())
                        .failoverSubject(entity.getFailoverSubject())
                        .failoverType(entity.getFailoverType())
                        .requestedAt(entity.getRequestedAt())
                        .sentAt(entity.getSentAt())
                        .recipients(recipientStates)
                        .status(entity.getStatus())
                        .createdAt(entity.getCreatedAt())
                        .modifiedAt(entity.getModifiedAt())
                        .build()
        );
    }

    private static AligoBrandTalkMessageRecipientSnapshotState mapRecipientToState(
            AligoBrandTalkMessageRecipientJpaEntity entity
    ) {
        return AligoBrandTalkMessageRecipientSnapshotState.builder()
                .id(entity.getId())
                .receiver(entity.getReceiver())
                .receiverMessage(entity.getReceiverMessage())
                .msgId(entity.getMsgId())
                .recipientStatus(entity.getRecipientStatus())
                .rslt(entity.getRslt())
                .rsltMessage(entity.getRsltMessage())
                .build();
    }
}
