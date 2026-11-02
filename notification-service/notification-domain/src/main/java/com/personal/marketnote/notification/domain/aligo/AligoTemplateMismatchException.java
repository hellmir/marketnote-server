package com.personal.marketnote.notification.domain.aligo;

public class AligoTemplateMismatchException extends RuntimeException {

    public AligoTemplateMismatchException(String message) {
        super("ERR_ALIGO_MESSAGE_02::" + message);
    }
}
