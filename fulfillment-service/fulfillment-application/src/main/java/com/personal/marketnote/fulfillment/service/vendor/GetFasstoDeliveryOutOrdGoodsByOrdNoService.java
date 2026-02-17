package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FasstoDeliveryCommandToRequestMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoDeliveryOutOrdGoodsByOrdNoCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoDeliveryOutOrdGoodsByOrdNoResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFasstoDeliveryOutOrdGoodsByOrdNoUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFasstoDeliveryOutOrdGoodsByOrdNoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetFasstoDeliveryOutOrdGoodsByOrdNoService implements GetFasstoDeliveryOutOrdGoodsByOrdNoUseCase {
    private final GetFasstoDeliveryOutOrdGoodsByOrdNoPort getFasstoDeliveryOutOrdGoodsByOrdNoPort;

    @Override
    public GetFasstoDeliveryOutOrdGoodsByOrdNoResult getOutOrdGoodsByOrdNo(
            GetFasstoDeliveryOutOrdGoodsByOrdNoCommand command
    ) {
        return getFasstoDeliveryOutOrdGoodsByOrdNoPort.getOutOrdGoodsByOrdNo(
                FasstoDeliveryCommandToRequestMapper.mapToOutOrdGoodsByOrdNoQuery(command)
        );
    }
}
