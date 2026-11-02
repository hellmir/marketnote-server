package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSettlementDailyCostsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSettlementDailyCostsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentSettlementDailyCostsUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentSettlementDailyCostsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentSettlementDailyCostsService implements GetFulfillmentSettlementDailyCostsUseCase {
    private final GetFulfillmentSettlementDailyCostsPort getFulfillmentSettlementDailyCostsPort;

    @Override
    public GetFulfillmentSettlementDailyCostsResult getDailyCosts(GetFulfillmentSettlementDailyCostsCommand command) {
        return getFulfillmentSettlementDailyCostsPort.getDailyCosts(command);
    }
}
