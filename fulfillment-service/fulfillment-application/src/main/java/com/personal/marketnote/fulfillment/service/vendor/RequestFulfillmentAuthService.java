package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.RequestFulfillmentAuthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class RequestFulfillmentAuthService implements RequestFulfillmentAuthUseCase {
    private final RequestFulfillmentAuthPort requestFulfillmentAuthPort;

    @Override
    public FulfillmentAccessToken requestAccessToken() {
        return requestFulfillmentAuthPort.requestAccessToken();
    }
}
