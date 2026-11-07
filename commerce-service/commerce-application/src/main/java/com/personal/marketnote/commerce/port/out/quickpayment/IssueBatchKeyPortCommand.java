package com.personal.marketnote.commerce.port.out.quickpayment;

import com.personal.marketnote.commerce.port.in.command.quickpayment.IssueBatchKeyCommand;
import lombok.Builder;

@Builder
public record IssueBatchKeyPortCommand(
        String encData,
        String encInfo
) {
    public static IssueBatchKeyPortCommand from(IssueBatchKeyCommand command) {
        return new IssueBatchKeyPortCommand(command.encData(), command.encInfo());
    }
}
