package com.personal.marketnote.fulfillment.port.in.command;

import com.personal.marketnote.common.utility.FormatValidator;

public record GetInternalReturnGodDetailCommand(
        String returnSlipNumbers
) {
    private static final int MAX_SLIP_NUMBER_COUNT = 100;

    public static GetInternalReturnGodDetailCommand of(String returnSlipNumbers) {
        if (FormatValidator.hasNoValue(returnSlipNumbers)) {
            throw new IllegalArgumentException("returnSlipNumbers는 필수입니다.");
        }
        long count = returnSlipNumbers.chars().filter(c -> c == ',').count() + 1;
        if (count > MAX_SLIP_NUMBER_COUNT) {
            throw new IllegalArgumentException(
                    "returnSlipNumbers 개수가 최대 제한(" + MAX_SLIP_NUMBER_COUNT + ")을 초과합니다: " + count);
        }
        return new GetInternalReturnGodDetailCommand(returnSlipNumbers);
    }
}
