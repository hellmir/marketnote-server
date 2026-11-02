package com.personal.marketnote.fulfillment.utility;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class FulfillmentAuthenticatedCaller {
    private final RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;

    public <T> T call(Function<String, T> operation) {
        FulfillmentAccessToken token = requestFulfillmentAuthUseCase.requestAccessToken();
        try {
            return operation.apply(token.getValue());
        } catch (FulfillmentInvalidAccessException firstFailure) {
            log.warn("Fulfillment vendor returned INVALID_ACCESS. Invalidating cached token and retrying once.");
            requestFulfillmentAuthUseCase.invalidateCache();
            FulfillmentAccessToken retriedToken = requestFulfillmentAuthUseCase.requestAccessToken();
            try {
                return operation.apply(retriedToken.getValue());
            } catch (FulfillmentInvalidAccessException secondFailure) {
                log.error("Fulfillment vendor returned INVALID_ACCESS even after token reissue. Giving up.");
                throw secondFailure;
            }
        }
    }
}
