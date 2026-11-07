package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileCreateState;
import com.personal.marketnote.notification.port.in.command.AuthenticateAligoSenderProfileCommand;

public class AligoSenderProfileCommandToStateMapper {

    private AligoSenderProfileCommandToStateMapper() {
    }

    public static AligoSenderProfileCreateState mapToState(
            String senderKey, AuthenticateAligoSenderProfileCommand command
    ) {
        return AligoSenderProfileCreateState.builder()
                .senderKey(senderKey)
                .plusId(command.plusId())
                .phoneNumber(command.phoneNumber())
                .build();
    }
}
