package com.personal.marketnote.fulfillment.port.in.usecase;

import com.personal.marketnote.fulfillment.port.in.command.GetInternalReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.GetInternalReturnGodDetailResult;

public interface GetInternalReturnGodDetailUseCase {
    GetInternalReturnGodDetailResult getReturnGodDetail(GetInternalReturnGodDetailCommand command);
}
