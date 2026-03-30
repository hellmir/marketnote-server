package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentShopCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentShopsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentShopsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentShopsUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentShopsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFulfillmentShopsService implements GetFulfillmentShopsUseCase {
    private final GetFulfillmentShopsPort getFulfillmentShopsPort;

    @Override
    public GetFulfillmentShopsResult getShops(GetFulfillmentShopsCommand command) {
        return getFulfillmentShopsPort.getShops(
                FulfillmentShopCommandToRequestMapper.mapToShopsQuery(command)
        );
    }
}
