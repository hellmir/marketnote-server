package com.personal.marketnote.commerce.port.in.usecase.order;

import com.personal.marketnote.commerce.port.in.command.order.GetReturnRefundInfoCommand;
import com.personal.marketnote.commerce.port.in.result.order.GetReturnRefundInfoResult;

public interface GetReturnRefundInfoUseCase {
    GetReturnRefundInfoResult getReturnRefundInfo(GetReturnRefundInfoCommand command);
}
