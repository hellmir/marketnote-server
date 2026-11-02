package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.DisconnectFulfillmentAuthUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.DisconnectFulfillmentAuthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class DisconnectFulfillmentAuthService implements DisconnectFulfillmentAuthUseCase {
    private final DisconnectFulfillmentAuthPort disconnectFulfillmentAuthPort;

    @Override
    public void disconnectAccessToken(String accessToken) {
        disconnectFulfillmentAuthPort.disconnectAccessToken(accessToken);
    }
}
