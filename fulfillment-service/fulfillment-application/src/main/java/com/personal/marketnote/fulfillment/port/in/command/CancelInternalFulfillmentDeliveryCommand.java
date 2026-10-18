package com.personal.marketnote.fulfillment.port.in.command;

import java.util.List;

public record CancelInternalFulfillmentDeliveryCommand(Long orderId, List<String> productKeys) {
}
