package com.personal.marketnote.commerce.port.in.usecase.order;

import com.personal.marketnote.commerce.port.in.command.order.RejectReturnCommand;

public interface RejectReturnUseCase {

    void rejectReturn(RejectReturnCommand command);
}
