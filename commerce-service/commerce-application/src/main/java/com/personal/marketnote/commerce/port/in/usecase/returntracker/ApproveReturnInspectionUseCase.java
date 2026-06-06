package com.personal.marketnote.commerce.port.in.usecase.returntracker;

import com.personal.marketnote.commerce.port.in.command.returntracker.ApproveReturnInspectionCommand;

public interface ApproveReturnInspectionUseCase {

    void approveReturnInspection(ApproveReturnInspectionCommand command);
}
