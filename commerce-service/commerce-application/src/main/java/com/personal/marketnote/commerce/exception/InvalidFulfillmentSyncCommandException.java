package com.personal.marketnote.commerce.exception;

public class InvalidFulfillmentSyncCommandException extends IllegalArgumentException {
    private static final String MESSAGE = "Sync fulfillment vendor inventory command is required.";

    public InvalidFulfillmentSyncCommandException() {
        super(MESSAGE);
    }
}
