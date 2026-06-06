package com.personal.marketnote.commerce.port.in.usecase.returntracker;

import com.personal.marketnote.commerce.port.in.command.returntracker.RequestReturnReshippingCommand;

public interface RequestReturnReshippingUseCase {

    void requestReturnReshipping(RequestReturnReshippingCommand command);
}
