package com.personal.marketnote.fulfillment.port.in.command;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.exception.InvalidReturnSlipNumbersCountException;
import com.personal.marketnote.fulfillment.exception.ReturnSlipNumbersNoValueException;

public record GetInternalReturnGodDetailCommand(
        String returnSlipNumbers
) {
    private static final int MAX_SLIP_NUMBER_COUNT = 100;

    public static GetInternalReturnGodDetailCommand of(String returnSlipNumbers) {
        if (FormatValidator.hasNoValue(returnSlipNumbers)) {
            throw new ReturnSlipNumbersNoValueException();
        }
        long count = returnSlipNumbers.chars().filter(c -> c == ',').count() + 1;
        if (count > MAX_SLIP_NUMBER_COUNT) {
            throw new InvalidReturnSlipNumbersCountException(count, MAX_SLIP_NUMBER_COUNT);
        }
        return new GetInternalReturnGodDetailCommand(returnSlipNumbers);
    }
}
