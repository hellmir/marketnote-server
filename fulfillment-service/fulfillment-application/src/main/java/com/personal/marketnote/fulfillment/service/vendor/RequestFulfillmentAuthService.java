package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.exception.FulfillmentAuthRequestFailedException;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.RequestFulfillmentAuthPort;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@UseCase
public class RequestFulfillmentAuthService implements RequestFulfillmentAuthUseCase {
    private static final long CACHE_BUFFER_SECONDS = 60L;

    private final RequestFulfillmentAuthPort requestFulfillmentAuthPort;
    private final Clock clock;
    private final AtomicReference<FulfillmentAccessToken> cachedToken = new AtomicReference<>();
    private final ReentrantLock issuanceLock = new ReentrantLock();

    public RequestFulfillmentAuthService(
            RequestFulfillmentAuthPort requestFulfillmentAuthPort,
            Clock clock
    ) {
        this.requestFulfillmentAuthPort = requestFulfillmentAuthPort;
        this.clock = clock;
    }

    @Override
    public FulfillmentAccessToken requestAccessToken() {
        FulfillmentAccessToken existing = cachedToken.get();
        if (isReusable(existing)) {
            return existing;
        }

        issuanceLock.lock();
        try {
            FulfillmentAccessToken rechecked = cachedToken.get();
            if (isReusable(rechecked)) {
                return rechecked;
            }

            FulfillmentAccessToken issued = requestFulfillmentAuthPort.requestAccessToken();
            if (FormatValidator.hasNoValue(issued)) {
                throw new FulfillmentAuthRequestFailedException(new IOException("Port returned null token"));
            }
            cachedToken.set(issued);
            return issued;
        } finally {
            issuanceLock.unlock();
        }
    }

    @Override
    public void invalidateCache() {
        cachedToken.set(null);
    }

    private boolean isReusable(FulfillmentAccessToken token) {
        if (FormatValidator.hasNoValue(token)) {
            return false;
        }

        return !token.isExpiringSoon(LocalDateTime.now(clock), CACHE_BUFFER_SECONDS);
    }
}
