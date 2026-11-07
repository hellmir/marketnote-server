package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.AligoMessageCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipientCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoMessageType;
import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.port.in.command.AligoAlimTalkRecipientCommand;
import com.personal.marketnote.notification.port.in.command.AligoFailoverCommand;
import com.personal.marketnote.notification.port.in.command.SendAligoAlimTalkCommand;

import java.time.LocalDateTime;
import java.util.List;

public class AligoMessageCommandToStateMapper {

    private AligoMessageCommandToStateMapper() {
    }

    public static AligoMessageCreateState mapToState(
            SendAligoAlimTalkCommand command, AligoTemplate template,
            String renderedContent, String buttonsJson, LocalDateTime requestedAt
    ) {
        List<AligoMessageRecipientCreateState> recipientStates = command.recipients().stream()
                .map(AligoMessageCommandToStateMapper::mapToRecipientState)
                .toList();

        AligoMessageCreateState.AligoMessageCreateStateBuilder builder = AligoMessageCreateState.builder()
                .senderKey(template.getSenderKey())
                .templateCode(template.getTemplateCode())
                .sender(command.sender())
                .messageType(AligoMessageType.AT)
                .content(renderedContent)
                .title(template.getTemplateName())
                .buttons(buttonsJson)
                .requestedAt(requestedAt)
                .recipients(recipientStates);

        AligoFailoverCommand failover = command.failover();
        if (FormatValidator.hasValue(failover) && failover.enabled()) {
            builder.failoverSubject(failover.subject())
                    .failoverMessageBody(failover.body());
        }

        return builder.build();
    }

    public static AligoMessageRecipientCreateState mapToRecipientState(AligoAlimTalkRecipientCommand command) {
        return AligoMessageRecipientCreateState.builder()
                .receiver(command.receiver())
                .recvName(command.recvName())
                .build();
    }
}
