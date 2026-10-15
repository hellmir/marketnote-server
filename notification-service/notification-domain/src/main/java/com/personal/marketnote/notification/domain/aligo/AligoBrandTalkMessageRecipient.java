package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class AligoBrandTalkMessageRecipient {

    private Long id;
    private String receiver;
    private String receiverMessage;
    private String msgId;
    private AligoMessageRecipientStatus recipientStatus;
    private String rslt;
    private String rsltMessage;

    public static AligoBrandTalkMessageRecipient from(AligoBrandTalkMessageRecipientCreateState state) {
        if (FormatValidator.hasNoValue(state.getReceiver())) {
            throw new InvalidBrandTalkMessageException("수신자 전화번호는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getReceiverMessage())) {
            throw new InvalidBrandTalkMessageException("수신자 메시지 JSON은 필수입니다.");
        }
        return AligoBrandTalkMessageRecipient.builder()
                .receiver(state.getReceiver())
                .receiverMessage(state.getReceiverMessage())
                .recipientStatus(AligoMessageRecipientStatus.PENDING)
                .build();
    }

    public static AligoBrandTalkMessageRecipient from(AligoBrandTalkMessageRecipientSnapshotState state) {
        return AligoBrandTalkMessageRecipient.builder()
                .id(state.getId())
                .receiver(state.getReceiver())
                .receiverMessage(state.getReceiverMessage())
                .msgId(state.getMsgId())
                .recipientStatus(state.getRecipientStatus())
                .rslt(state.getRslt())
                .rsltMessage(state.getRsltMessage())
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
}
