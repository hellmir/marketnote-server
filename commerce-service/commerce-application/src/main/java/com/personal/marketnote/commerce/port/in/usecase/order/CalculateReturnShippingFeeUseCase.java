package com.personal.marketnote.commerce.port.in.usecase.order;

import com.personal.marketnote.commerce.port.in.command.order.CalculateReturnShippingFeeCommand;
import com.personal.marketnote.commerce.port.in.result.order.CalculateReturnShippingFeeResult;

public interface CalculateReturnShippingFeeUseCase {

    CalculateReturnShippingFeeResult calculateReturnShippingFee(CalculateReturnShippingFeeCommand command);
}
