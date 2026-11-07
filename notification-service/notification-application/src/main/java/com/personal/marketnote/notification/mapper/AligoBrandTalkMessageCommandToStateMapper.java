package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageRecipientCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkFailoverType;
import com.personal.marketnote.notification.domain.aligo.BrandTalkKakaoTarget;
import com.personal.marketnote.notification.port.in.command.BrandTalkRecipientCommand;
import com.personal.marketnote.notification.port.in.command.SendBrandTalkCommand;

import java.time.LocalDateTime;
import java.util.List;

public class AligoBrandTalkMessageCommandToStateMapper {

    private AligoBrandTalkMessageCommandToStateMapper() {
    }

    public static AligoBrandTalkMessageCreateState mapToState(
            SendBrandTalkCommand command,
            BrandTalkKakaoTarget kakaoTarget, BrandTalkFailoverType failoverType,
            boolean failoverEnabled, String failoverSubject,
            LocalDateTime requestedAt,
            List<AligoBrandTalkMessageRecipientCreateState> recipientStates
    ) {
        return AligoBrandTalkMessageCreateState.builder()
                .senderKey(command.senderKey())
                .sender(command.sender())
                .kakaoTarget(kakaoTarget)
                .advertYn(command.advertYn())
                .templateCode(command.templateCode())
                .failoverEnabled(failoverEnabled)
                .failoverSubject(failoverSubject)
                .failoverType(failoverType)
                .requestedAt(requestedAt)
                .recipients(recipientStates)
                .build();
    }

    public static AligoBrandTalkMessageRecipientCreateState mapToRecipientState(
            BrandTalkRecipientCommand recipient, String receiverMessage
    ) {
        return AligoBrandTalkMessageRecipientCreateState.builder()
                .receiver(recipient.receiver())
                .receiverMessage(receiverMessage)
                .build();
    }
}
