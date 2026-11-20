package com.personal.marketnote.commerce.adapter.out.event.exception;

public class InventoryEventSerializationException extends RuntimeException {
    public InventoryEventSerializationException(String topic, Throwable cause) {
        super("ERR_INVENTORY_EVENT_01::재고 이벤트 직렬화에 실패했습니다. topic=" + topic, cause);
    }
}
