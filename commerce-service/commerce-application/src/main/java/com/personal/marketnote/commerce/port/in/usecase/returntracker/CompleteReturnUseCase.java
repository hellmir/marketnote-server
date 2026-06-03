package com.personal.marketnote.commerce.port.in.usecase.returntracker;

import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnCommand;

public interface CompleteReturnUseCase {

    void completeReturn(CompleteReturnCommand command);
}
