package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.mapper.FulfillmentWarehousingCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentWarehousingUseCase;
import com.personal.marketnote.fulfillment.port.out.scheduler.ScheduleFulfillmentWarehousingPollingCommand;
import com.personal.marketnote.fulfillment.port.out.scheduler.ScheduleFulfillmentWarehousingPollingPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentWarehousingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class RegisterFulfillmentWarehousingService implements RegisterFulfillmentWarehousingUseCase {
    private final RegisterFulfillmentWarehousingPort registerFulfillmentWarehousingPort;
    private final ScheduleFulfillmentWarehousingPollingPort scheduleFulfillmentWarehousingPollingPort;

    @Override
    public RegisterFulfillmentWarehousingResult registerWarehousing(RegisterFulfillmentWarehousingCommand command) {
        RegisterFulfillmentWarehousingResult result = registerFulfillmentWarehousingPort.registerWarehousing(
                FulfillmentWarehousingCommandToRequestMapper.mapToRegisterRequest(command)
        );
        schedulePolling(command, result);
        return result;
    }

    private void schedulePolling(RegisterFulfillmentWarehousingCommand command, RegisterFulfillmentWarehousingResult result) {
        if (FormatValidator.hasNoValue(command)
                || FormatValidator.hasNoValue(command.warehousingRequests())
                || FormatValidator.hasNoValue(result)) {
            return;
        }

        for (RegisterFulfillmentWarehousingItemCommand item : command.warehousingRequests()) {
            if (FormatValidator.hasNoValue(item) || FormatValidator.hasNoValue(item.ordNo())) {
                continue;
            }

            scheduleFulfillmentWarehousingPollingPort.schedule(
                    ScheduleFulfillmentWarehousingPollingCommand.of(
                            command.customerCode(),
                            item.ordNo(),
                            item.ordDt()
                    )
            );
        }
    }
}
