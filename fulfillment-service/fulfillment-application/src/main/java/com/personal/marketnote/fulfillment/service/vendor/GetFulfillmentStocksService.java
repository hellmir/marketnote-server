package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStocksCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentStocksResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentStocksUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentStocksPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentStocksService implements GetFulfillmentStocksUseCase {
    private final GetFulfillmentStocksPort getFulfillmentStocksPort;

    @Override
    public GetFulfillmentStocksResult getStocks(GetFulfillmentStocksCommand command) {
        return getFulfillmentStocksPort.getStocks(command);
    }
}
