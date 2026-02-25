package com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception;

public class KcpCommunicationException extends RuntimeException {
    public KcpCommunicationException(String message) {
        super(message);
    }

    public KcpCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
