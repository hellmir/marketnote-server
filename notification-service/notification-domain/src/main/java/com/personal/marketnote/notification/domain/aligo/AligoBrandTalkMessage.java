package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class AligoBrandTalkMessage extends BaseDomain {

    private static final int MIN_RECIPIENTS = 1;
    private static final int MAX_RECIPIENTS = 500;
    private static final int MAX_MMS_IMAGES = 3;

    private Long id;
    private String senderKey;
    private String sender;
    private BrandTalkKakaoTarget kakaoTarget;
    private boolean advertYn;
    private String templateCode;
    private BrandTalkMessageStatus messageStatus;
    private BrandTalkMessageCostType costType;
    private Integer unitCost;
    private Integer totalCost;
    private String aligoMid;
    private int successCount;
    private int failCount;
    private boolean failoverEnabled;
    private String failoverSubject;
    private BrandTalkFailoverType failoverType;
    private LocalDateTime requestedAt;
    private LocalDateTime sentAt;
    private List<AligoBrandTalkMessageRecipient> recipients;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static AligoBrandTalkMessage from(AligoBrandTalkMessageCreateState state) {
        validate(state);
        List<AligoBrandTalkMessageRecipient> recipients = state.getRecipients().stream()
                .map(AligoBrandTalkMessageRecipient::from)
                .toList();

        AligoBrandTalkMessage message = AligoBrandTalkMessage.builder()
                .senderKey(state.getSenderKey())
                .sender(state.getSender())
                .kakaoTarget(state.getKakaoTarget())
                .advertYn(state.isAdvertYn())
                .templateCode(state.getTemplateCode())
                .messageStatus(BrandTalkMessageStatus.REQUESTED)
                .successCount(0)
                .failCount(0)
                .failoverEnabled(state.isFailoverEnabled())
                .failoverSubject(state.getFailoverSubject())
                .failoverType(state.getFailoverType())
                .requestedAt(state.getRequestedAt())
                .recipients(recipients)
                .build();
        message.activate();
        return message;
    }

    public static AligoBrandTalkMessage from(AligoBrandTalkMessageSnapshotState state) {
        List<AligoBrandTalkMessageRecipient> recipients = List.of();
        if (FormatValidator.hasValue(state.getRecipients())) {
            recipients = state.getRecipients().stream()
                    .map(AligoBrandTalkMessageRecipient::from)
                    .toList();
        }

        AligoBrandTalkMessage message = AligoBrandTalkMessage.builder()
                .id(state.getId())
                .senderKey(state.getSenderKey())
                .sender(state.getSender())
                .kakaoTarget(state.getKakaoTarget())
                .advertYn(state.isAdvertYn())
                .templateCode(state.getTemplateCode())
                .messageStatus(state.getMessageStatus())
                .costType(state.getCostType())
                .unitCost(state.getUnitCost())
                .totalCost(state.getTotalCost())
                .aligoMid(state.getAligoMid())
                .successCount(state.getSuccessCount())
                .failCount(state.getFailCount())
                .failoverEnabled(state.isFailoverEnabled())
                .failoverSubject(state.getFailoverSubject())
                .failoverType(state.getFailoverType())
                .requestedAt(state.getRequestedAt())
                .sentAt(state.getSentAt())
                .recipients(recipients)
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        message.status = state.getStatus();
        return message;
    }

    public boolean isRequested() {
        return messageStatus.isRequested();
    }

    public boolean isSent() {
        return messageStatus.isSent();
    }

    public boolean isFailed() {
        return messageStatus.isFailed();
    }

    public boolean hasFailover() {
        return failoverEnabled;
    }

    public void markAsSent(
            String aligoMid, BrandTalkMessageCostType costType,
            int unitCost, int totalCost, int successCount, int failCount,
            LocalDateTime sentAt
    ) {
        if (!messageStatus.canTransitionTo(BrandTalkMessageStatus.SENT)) {
            throw new InvalidBrandTalkMessageStatusTransitionException(messageStatus, BrandTalkMessageStatus.SENT);
        }
        this.aligoMid = aligoMid;
        this.costType = costType;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.successCount = successCount;
        this.failCount = failCount;
        this.messageStatus = BrandTalkMessageStatus.SENT;
        this.sentAt = sentAt;
    }

    public void markAsFailed() {
        if (!messageStatus.canTransitionTo(BrandTalkMessageStatus.FAILED)) {
            throw new InvalidBrandTalkMessageStatusTransitionException(messageStatus, BrandTalkMessageStatus.FAILED);
        }
        this.messageStatus = BrandTalkMessageStatus.FAILED;
    }

    private static void validate(AligoBrandTalkMessageCreateState state) {
        if (FormatValidator.hasNoValue(state.getSenderKey())) {
            throw new InvalidBrandTalkMessageException("senderKey는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getSender())) {
            throw new InvalidBrandTalkMessageException("발신번호는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getKakaoTarget())) {
            throw new InvalidBrandTalkMessageException("kakaoTarget은 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getTemplateCode())) {
            throw new InvalidBrandTalkMessageException("templateCode는 필수입니다.");
        }
        validateRecipients(state);
        validateFailover(state);
    }

    private static void validateRecipients(AligoBrandTalkMessageCreateState state) {
        if (FormatValidator.hasNoValue(state.getRecipients())) {
            throw new InvalidBrandTalkMessageRecipientCountException(0);
        }
        int recipientCount = state.getRecipients().size();
        if (recipientCount < MIN_RECIPIENTS || recipientCount > MAX_RECIPIENTS) {
            throw new InvalidBrandTalkMessageRecipientCountException(recipientCount);
        }
    }

    private static void validateFailover(AligoBrandTalkMessageCreateState state) {
        if (!state.isFailoverEnabled()) {
            return;
        }
        if (FormatValidator.hasNoValue(state.getFailoverType())) {
            throw new InvalidBrandTalkFailoverException("대체 발송 유형은 필수입니다.");
        }
    }
}
