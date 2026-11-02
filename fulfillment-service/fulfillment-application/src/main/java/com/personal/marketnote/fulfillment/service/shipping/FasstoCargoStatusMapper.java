package com.personal.marketnote.fulfillment.service.shipping;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingStatus;

import java.util.Set;

class FasstoCargoStatusMapper {

    private static final Set<String> PREPARING_STATUSES = Set.of("출고요청", "출고지시", "피킹중");
    private static final String DELIVERED_STATUS = "배송완료";
    private static final String DELIVERY_FAILED_STATUS = "배송불가";

    private FasstoCargoStatusMapper() {
    }

    static ShippingStatus toShippingStatus(String cargoStatusName) {
        if (FormatValidator.hasNoValue(cargoStatusName)) {
            return ShippingStatus.PREPARING;
        }
        if (PREPARING_STATUSES.contains(cargoStatusName)) {
            return ShippingStatus.PREPARING;
        }
        if (DELIVERED_STATUS.equals(cargoStatusName)) {
            return ShippingStatus.DELIVERED;
        }
        if (DELIVERY_FAILED_STATUS.equals(cargoStatusName)) {
            return ShippingStatus.DELIVERY_FAILED;
        }
        return ShippingStatus.SHIPPING;
    }
}
