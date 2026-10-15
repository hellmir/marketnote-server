package com.personal.marketnote.notification.adapter.out.persistence.aligo.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.notification.domain.aligo.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aligo_brand_talk_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AligoBrandTalkMessageJpaEntity extends BaseGeneralEntity {

    @Column(nullable = false, length = 100)
    private String senderKey;

    @Column(nullable = false, length = 20)
    private String sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BrandTalkKakaoTarget kakaoTarget;

    @Column(nullable = false)
    private boolean advertYn;

    @Column(nullable = false)
    private String templateCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BrandTalkMessageStatus messageStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private BrandTalkMessageCostType costType;

    private Integer unitCost;

    private Integer totalCost;

    @Column(unique = true, length = 100)
    private String aligoMid;

    @Column(nullable = false)
    private int successCount;

    @Column(nullable = false)
    private int failCount;

    @Column(nullable = false)
    private boolean failoverEnabled;

    @Column(length = 100)
    private String failoverSubject;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private BrandTalkFailoverType failoverType;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime sentAt;

    @OneToMany(mappedBy = "brandTalkMessage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AligoBrandTalkMessageRecipientJpaEntity> recipients = new ArrayList<>();

    public static AligoBrandTalkMessageJpaEntity create(
            String senderKey, String sender, BrandTalkKakaoTarget kakaoTarget,
            boolean advertYn, String templateCode,
            boolean failoverEnabled, String failoverSubject, BrandTalkFailoverType failoverType,
            LocalDateTime requestedAt, List<AligoBrandTalkMessageRecipientJpaEntity> recipientEntities
    ) {
        AligoBrandTalkMessageJpaEntity entity = new AligoBrandTalkMessageJpaEntity();
        entity.senderKey = senderKey;
        entity.sender = sender;
        entity.kakaoTarget = kakaoTarget;
        entity.advertYn = advertYn;
        entity.templateCode = templateCode;
        entity.messageStatus = BrandTalkMessageStatus.REQUESTED;
        entity.successCount = 0;
        entity.failCount = 0;
        entity.failoverEnabled = failoverEnabled;
        entity.failoverSubject = failoverSubject;
        entity.failoverType = failoverType;
        entity.requestedAt = requestedAt;

        for (AligoBrandTalkMessageRecipientJpaEntity recipient : recipientEntities) {
            recipient.assignMessage(entity);
            entity.recipients.add(recipient);
        }
        return entity;
    }

    public void updateSendResult(
            String aligoMid, BrandTalkMessageCostType costType,
            int unitCost, int totalCost, int successCount, int failCount,
            BrandTalkMessageStatus messageStatus, LocalDateTime sentAt
    ) {
        this.aligoMid = aligoMid;
        this.costType = costType;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.successCount = successCount;
        this.failCount = failCount;
        this.messageStatus = messageStatus;
        this.sentAt = sentAt;
    }

    public void updateMessageStatus(BrandTalkMessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }
}
