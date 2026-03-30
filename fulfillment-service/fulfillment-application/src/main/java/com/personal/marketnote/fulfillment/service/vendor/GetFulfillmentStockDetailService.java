package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentStockCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentStocksResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentStockDetailUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentStockDetailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentStockDetailService implements GetFulfillmentStockDetailUseCase {
    private final GetFulfillmentStockDetailPort getFulfillmentStockDetailPort;

    @Override
    public GetFulfillmentStocksResult getStockDetail(GetFulfillmentStockDetailCommand command) {
        return getFulfillmentStockDetailPort.getStockDetail(
                FulfillmentStockCommandToRequestMapper.mapToDetailQuery(command)
        );
    }
}
