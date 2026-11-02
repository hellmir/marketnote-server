package com.personal.marketnote.notification.domain.aligo;

public class AligoParameterMissingException extends AligoApiException {

    public AligoParameterMissingException(String message) {
        super("ERR_ALIGO_02::" + message);
    }
}
