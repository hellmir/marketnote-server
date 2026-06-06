package com.personal.marketnote.commerce.port.in.usecase.returntracker;

import com.personal.marketnote.commerce.port.in.command.returntracker.StartReturnReshippingCommand;

public interface StartReturnReshippingUseCase {

    void startReturnReshipping(StartReturnReshippingCommand command);
}
