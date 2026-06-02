package com.personal.marketnote.commerce.port.in.usecase.returntracker;

import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnRefundCommand;

public interface CompleteReturnRefundUseCase {

    void completeReturnRefund(CompleteReturnRefundCommand command);
}
