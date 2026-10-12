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
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class AligoMessage extends BaseDomain {

    private static final int MIN_RECIPIENTS = 1;
    private static final int MAX_RECIPIENTS = 500;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");

    private Long id;
    private String senderKey;
    private String templateCode;
    private String sender;
    private AligoMessageType messageType;
    private String content;
    private String title;
    private String buttons;
    private AligoMessageStatus messageStatus;
    private String aligoMid;
    private int successCount;
    private int failCount;
    private boolean failoverUsed;
    private String failoverSubject;
    private String failoverMessageBody;
    private LocalDateTime requestedAt;
    private LocalDateTime sentAt;
    private List<AligoMessageRecipient> recipients;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static AligoMessage from(AligoMessageCreateState state) {
        validate(state);
        List<AligoMessageRecipient> recipients = state.getRecipients().stream()
                .map(AligoMessageRecipient::from)
                .toList();

        AligoMessage message = AligoMessage.builder()
                .senderKey(state.getSenderKey())
                .templateCode(state.getTemplateCode())
                .sender(state.getSender())
                .messageType(state.getMessageType())
                .content(state.getContent())
                .title(state.getTitle())
                .buttons(state.getButtons())
                .messageStatus(AligoMessageStatus.REQUESTED)
                .successCount(0)
                .failCount(0)
                .failoverUsed(false)
                .failoverSubject(state.getFailoverSubject())
                .failoverMessageBody(state.getFailoverMessageBody())
                .requestedAt(state.getRequestedAt())
                .recipients(recipients)
                .build();
        message.activate();
        return message;
    }

    public static AligoMessage from(AligoMessageSnapshotState state) {
        List<AligoMessageRecipient> recipients = List.of();
        if (FormatValidator.hasValue(state.getRecipients())) {
            recipients = state.getRecipients().stream()
                    .map(AligoMessageRecipient::from)
                    .toList();
        }

        AligoMessage message = AligoMessage.builder()
                .id(state.getId())
                .senderKey(state.getSenderKey())
                .templateCode(state.getTemplateCode())
                .sender(state.getSender())
                .messageType(state.getMessageType())
                .content(state.getContent())
                .title(state.getTitle())
                .buttons(state.getButtons())
                .messageStatus(state.getMessageStatus())
                .aligoMid(state.getAligoMid())
                .successCount(state.getSuccessCount())
                .failCount(state.getFailCount())
                .failoverUsed(state.isFailoverUsed())
                .failoverSubject(state.getFailoverSubject())
                .failoverMessageBody(state.getFailoverMessageBody())
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

    public boolean isCancelled() {
        return messageStatus.isCancelled();
    }

    public boolean isFailovered() {
        return messageStatus.isFailovered();
    }

    public boolean isCancellable() {
        return messageStatus.isCancellable();
    }

    public boolean hasFailoverContent() {
        return FormatValidator.hasValue(failoverSubject) || FormatValidator.hasValue(failoverMessageBody);
    }

    public void markAsSent(String aligoMid, int successCount, int failCount, LocalDateTime sentAt) {
        if (!messageStatus.canTransitionTo(AligoMessageStatus.SENT)) {
            throw new InvalidAligoMessageStatusTransitionException(messageStatus, AligoMessageStatus.SENT);
        }
        this.aligoMid = aligoMid;
        this.successCount = successCount;
        this.failCount = failCount;
        this.messageStatus = AligoMessageStatus.SENT;
        this.sentAt = sentAt;
    }

    public void markAsFailed() {
        if (!messageStatus.canTransitionTo(AligoMessageStatus.FAILED)) {
            throw new InvalidAligoMessageStatusTransitionException(messageStatus, AligoMessageStatus.FAILED);
        }
        this.messageStatus = AligoMessageStatus.FAILED;
    }

    public void markAsFailovered(String aligoMid, String smid, int successCount, int failCount, LocalDateTime sentAt) {
        if (!messageStatus.canTransitionTo(AligoMessageStatus.FAILOVERED)) {
            throw new InvalidAligoMessageStatusTransitionException(messageStatus, AligoMessageStatus.FAILOVERED);
        }
        this.aligoMid = aligoMid;
        this.successCount = successCount;
        this.failCount = failCount;
        this.messageStatus = AligoMessageStatus.FAILOVERED;
        this.sentAt = sentAt;
        this.failoverUsed = true;
        if (FormatValidator.hasValue(recipients)) {
            for (AligoMessageRecipient recipient : recipients) {
                recipient.updateSmid(smid);
            }
        }
    }

    public void cancel() {
        if (!messageStatus.canTransitionTo(AligoMessageStatus.CANCELLED)) {
            throw new InvalidAligoMessageStatusTransitionException(messageStatus, AligoMessageStatus.CANCELLED);
        }
        this.messageStatus = AligoMessageStatus.CANCELLED;
    }

    private static void validate(AligoMessageCreateState state) {
        if (FormatValidator.hasNoValue(state.getSenderKey())) {
            throw new InvalidAligoTemplateException("senderKey는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getTemplateCode())) {
            throw new InvalidAligoTemplateException("templateCode는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getContent())) {
            throw new InvalidAligoTemplateException("발송 본문은 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getSender())) {
            throw new InvalidAligoTemplateException("발신번호는 필수입니다.");
        }
        if (!PHONE_PATTERN.matcher(state.getSender()).matches()) {
            throw new InvalidAligoTemplateException("발신번호 형식이 올바르지 않습니다.");
        }
        if (FormatValidator.hasNoValue(state.getRecipients())) {
            throw new InvalidAligoMessageRecipientCountException(0);
        }
        int recipientCount = state.getRecipients().size();
        if (recipientCount < MIN_RECIPIENTS || recipientCount > MAX_RECIPIENTS) {
            throw new InvalidAligoMessageRecipientCountException(recipientCount);
        }
    }
}
