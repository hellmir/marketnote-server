package com.personal.marketnote.notification.domain.aligo;

public class InvalidBrandTalkFailoverException extends IllegalArgumentException {

    public InvalidBrandTalkFailoverException(String message) {
        super("ERR_BRAND_TALK_FAILOVER::" + message);
    }
}
