package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.personal.marketnote.notification.domain.aligo.*;

public final class AligoApiErrorMapper {

    private static final int CODE_SUCCESS = 0;
    private static final int CODE_AUTHENTICATION_FAILURE = -101;
    private static final int CODE_PARAMETER_OR_POINT_OR_CANCEL = -99;
    private static final int CODE_TEMPLATE_NOT_MODIFIABLE = 509;

    private AligoApiErrorMapper() {
    }

    public static void throwIfError(int resultCode, String message) {
        if (resultCode == CODE_SUCCESS) {
            return;
        }
        if (resultCode == CODE_AUTHENTICATION_FAILURE) {
            throw new AligoAuthenticationException();
        }
        if (resultCode == CODE_PARAMETER_OR_POINT_OR_CANCEL) {
            throw resolveMinus99Exception(message);
        }
        if (resultCode == CODE_TEMPLATE_NOT_MODIFIABLE) {
            throw new AligoTemplateNotModifiableException();
        }
        throw new AligoApiRequestFailedException(message);
    }

    private static AligoApiException resolveMinus99Exception(String message) {
        if (message.contains("포인트")) {
            return new AligoInsufficientPointException();
        }
        if (message.contains("취소") || message.contains("5분전")) {
            return new AligoCancelDeadlineExceededException();
        }
        return new AligoParameterMissingException(message);
    }
}
