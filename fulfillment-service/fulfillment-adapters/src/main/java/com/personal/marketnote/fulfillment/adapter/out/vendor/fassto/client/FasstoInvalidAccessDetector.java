package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentApiResponse;
import com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Slf4j
public final class FasstoInvalidAccessDetector {
    private FasstoInvalidAccessDetector() {
    }

    public static boolean isInvalidAccess(FulfillmentApiResponse response, ResponseEntity<String> rawResponse) {
        if (FormatValidator.hasValue(response)) {
            return response.isInvalidAccess();
        }
        return isInvalidAccessRawBody(rawResponse);
    }

    public static boolean isInvalidAccessRawBody(ResponseEntity<String> rawResponse) {
        if (FormatValidator.hasNoValue(rawResponse)) {
            return false;
        }
        return FulfillmentApiResponse.containsInvalidAccessMarker(rawResponse.getBody());
    }

    public static void throwIfInvalidAccess(FulfillmentApiResponse parsedResponse, ResponseEntity<String> rawResponse) {
        if (!isInvalidAccess(parsedResponse, rawResponse)) {
            return;
        }
        if (FormatValidator.hasValue(parsedResponse)) {
            log.debug("Fulfillment INVALID_ACCESS detected. vendorMessage={}", parsedResponse.resolveErrorMessage());
        }
        throw new FulfillmentInvalidAccessException();
    }
}
