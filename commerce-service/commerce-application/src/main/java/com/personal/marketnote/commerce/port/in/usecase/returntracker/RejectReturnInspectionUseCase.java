package com.personal.marketnote.commerce.port.in.usecase.returntracker;

import com.personal.marketnote.commerce.port.in.command.returntracker.RejectReturnInspectionCommand;

public interface RejectReturnInspectionUseCase {

    void rejectReturnInspection(RejectReturnInspectionCommand command);
}
