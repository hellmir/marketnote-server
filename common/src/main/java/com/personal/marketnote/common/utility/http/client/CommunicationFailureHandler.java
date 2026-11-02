package com.personal.marketnote.common.utility.http.client;

import com.personal.marketnote.common.utility.FormatValidator;
import org.springframework.http.ResponseEntity;

public class CommunicationFailureHandler {
    public static boolean isCertainFailure(ResponseEntity<?> response) {
        return FormatValidator.hasValue(response) && FormatValidator.equals(response.getStatusCode().value(), 200);
    }
}
