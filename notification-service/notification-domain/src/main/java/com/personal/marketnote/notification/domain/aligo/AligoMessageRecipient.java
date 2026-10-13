package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class AligoMessageRecipient {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");

    private Long id;
    private String receiver;
    private String recvName;
    private String msgId;
    private String smid;
    private AligoMessageRecipientStatus recipientStatus;
    private String rslt;
    private String rsltMessage;
    private LocalDateTime sentAt;
    private LocalDateTime resultAt;
    private LocalDateTime reportAt;

    public static AligoMessageRecipient from(AligoMessageRecipientCreateState state) {
        if (FormatValidator.hasNoValue(state.getReceiver())) {
            throw new InvalidAligoTemplateException("수신자 전화번호는 필수입니다.");
        }
        if (!PHONE_PATTERN.matcher(state.getReceiver()).matches()) {
            throw new InvalidAligoTemplateException("수신자 전화번호 형식이 올바르지 않습니다.");
        }
        return AligoMessageRecipient.builder()
                .receiver(state.getReceiver())
                .recvName(state.getRecvName())
                .recipientStatus(AligoMessageRecipientStatus.PENDING)
                .build();
    }

    public static AligoMessageRecipient from(AligoMessageRecipientSnapshotState state) {
        return AligoMessageRecipient.builder()
                .id(state.getId())
                .receiver(state.getReceiver())
                .recvName(state.getRecvName())
                .msgId(state.getMsgId())
                .smid(state.getSmid())
                .recipientStatus(state.getRecipientStatus())
                .rslt(state.getRslt())
                .rsltMessage(state.getRsltMessage())
                .sentAt(state.getSentAt())
                .resultAt(state.getResultAt())
                .reportAt(state.getReportAt())
                .build();
    }

    public boolean isPending() {
        return recipientStatus.isPending();
    }

    public boolean isSuccess() {
        return recipientStatus.isSuccess();
    }

    public boolean isFailed() {
        return recipientStatus.isFailed();
    }

    public void markAsSuccess(String msgId) {
        this.msgId = msgId;
        this.recipientStatus = AligoMessageRecipientStatus.SUCCESS;
    }

    public void markAsFailed(String rslt, String rsltMessage) {
        this.rslt = rslt;
        this.rsltMessage = rsltMessage;
        this.recipientStatus = AligoMessageRecipientStatus.FAILED;
    }

    public void updateSmid(String smid) {
        this.smid = smid;
    }

    public boolean isResolved() {
        return !isPending();
    }

    public void updateResult(
            AligoMessageRecipientStatus targetStatus, String msgId,
            String rslt, String rsltMessage,
            LocalDateTime sentAt, LocalDateTime resultAt, LocalDateTime reportAt
    ) {
        if (!isPending()) {
            return;
        }
        this.recipientStatus = targetStatus;
        this.msgId = msgId;
        this.rslt = rslt;
        this.rsltMessage = rsltMessage;
        this.sentAt = sentAt;
        this.resultAt = resultAt;
        this.reportAt = reportAt;
    }

    public void forceResolveFailed() {
        if (!isPending()) {
            return;
        }
        this.recipientStatus = AligoMessageRecipientStatus.FAILED;
        this.rslt = "TIMEOUT";
        this.rsltMessage = "24시간 내 결과 미수신으로 강제 실패 처리";
    }
}
