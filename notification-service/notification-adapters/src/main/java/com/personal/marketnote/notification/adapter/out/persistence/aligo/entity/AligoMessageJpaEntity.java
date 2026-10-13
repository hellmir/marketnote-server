package com.personal.marketnote.notification.adapter.out.persistence.aligo.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.notification.domain.aligo.AligoMessageStatus;
import com.personal.marketnote.notification.domain.aligo.AligoMessageType;
import com.personal.marketnote.common.utility.FormatValidator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aligo_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AligoMessageJpaEntity extends BaseGeneralEntity {

    @Column(nullable = false, length = 100)
    private String senderKey;

    @Column(nullable = false)
    private String templateCode;

    @Column(nullable = false, length = 20)
    private String sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AligoMessageType messageType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String buttons;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AligoMessageStatus messageStatus;

    @Column(unique = true, length = 100)
    private String aligoMid;

    @Column(nullable = false)
    private int successCount;

    @Column(nullable = false)
    private int failCount;

    @Column(nullable = false)
    private boolean failoverUsed;

    @Column(length = 100)
    private String failoverSubject;

    @Column(columnDefinition = "TEXT")
    private String failoverMessageBody;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime sendDate;

    private LocalDateTime sentAt;

    @OneToMany(mappedBy = "aligoMessage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AligoMessageRecipientJpaEntity> recipients = new ArrayList<>();

    public void updateSendResult(
            AligoMessageStatus messageStatus, String aligoMid,
            int successCount, int failCount, LocalDateTime sentAt
    ) {
        this.messageStatus = messageStatus;
        this.aligoMid = aligoMid;
        this.successCount = successCount;
        this.failCount = failCount;
        this.sentAt = sentAt;
    }

    public void updateFailoverResult(
            AligoMessageStatus messageStatus, String aligoMid, String smid,
            int successCount, int failCount, LocalDateTime sentAt
    ) {
        this.messageStatus = messageStatus;
        this.aligoMid = aligoMid;
        this.successCount = successCount;
        this.failCount = failCount;
        this.sentAt = sentAt;
        this.failoverUsed = true;
        for (AligoMessageRecipientJpaEntity recipient : recipients) {
            recipient.updateSmid(smid);
        }
    }

    public void updateStatus(AligoMessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public static AligoMessageJpaEntity create(
            String senderKey, String templateCode, String sender,
            AligoMessageType messageType, String content, String title,
            String buttons, AligoMessageStatus messageStatus,
            String failoverSubject, String failoverMessageBody,
            LocalDateTime requestedAt, LocalDateTime sendDate,
            List<AligoMessageRecipientJpaEntity> recipientEntities
    ) {
        AligoMessageJpaEntity entity = new AligoMessageJpaEntity();
        entity.senderKey = senderKey;
        entity.templateCode = templateCode;
        entity.sender = sender;
        entity.messageType = messageType;
        entity.content = content;
        entity.title = title;
        entity.buttons = buttons;
        entity.messageStatus = messageStatus;
        entity.successCount = 0;
        entity.failCount = 0;
        entity.failoverUsed = false;
        entity.failoverSubject = failoverSubject;
        entity.failoverMessageBody = failoverMessageBody;
        entity.requestedAt = requestedAt;
        entity.sendDate = sendDate;
        if (FormatValidator.hasValue(recipientEntities)) {
            for (AligoMessageRecipientJpaEntity recipient : recipientEntities) {
                recipient.assignMessage(entity);
                entity.recipients.add(recipient);
            }
        }
        return entity;
    }
}
