package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record CompleteFasstoDeliveryIcsItemCommand(
        List<String> ordNoList
) {
    public static CompleteFasstoDeliveryIcsItemCommand of(List<String> ordNoList) {
        return new CompleteFasstoDeliveryIcsItemCommand(ordNoList);
    }
}
