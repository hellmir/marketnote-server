package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.shippingaddress.DeliveryRequestType;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetDeliveryRequestTypesResult;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.GetDeliveryRequestTypesUseCase;

import java.util.Arrays;
import java.util.List;

@UseCase
public class GetDeliveryRequestTypesService implements GetDeliveryRequestTypesUseCase {

    @Override
    public List<GetDeliveryRequestTypesResult> getDeliveryRequestTypes() {
        return Arrays.stream(DeliveryRequestType.values())
                .map(GetDeliveryRequestTypesResult::from)
                .toList();
    }
}
