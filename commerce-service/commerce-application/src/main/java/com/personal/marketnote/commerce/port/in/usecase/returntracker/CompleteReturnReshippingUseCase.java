package com.personal.marketnote.commerce.port.in.usecase.returntracker;

import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnReshippingCommand;

public interface CompleteReturnReshippingUseCase {

    void completeReturnReshipping(CompleteReturnReshippingCommand command);
}
