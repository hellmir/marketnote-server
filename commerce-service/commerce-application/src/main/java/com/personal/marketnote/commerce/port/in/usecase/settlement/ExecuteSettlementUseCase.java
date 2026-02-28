package com.personal.marketnote.commerce.port.in.usecase.settlement;

import com.personal.marketnote.commerce.port.in.command.settlement.ExecuteSettlementCommand;

public interface ExecuteSettlementUseCase {
    void executeSettlement(ExecuteSettlementCommand command);
}
