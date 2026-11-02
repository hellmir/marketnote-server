package com.personal.marketnote.commerce.domain.fulfillment;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum CancellableFulfillmentWorkStatus {
    NOT_REGISTERED("출고 미접수"),
    REGISTERED("출고 접수"),
    PICKING("피킹 진행 중"),
    PICKED("피킹 완료"),
    PACKING("포장 중"),
    RELEASED("출고 완료");

    private final String description;

    private static final Set<CancellableFulfillmentWorkStatus> CANCELLABLE_STATUSES = EnumSet.of(
            NOT_REGISTERED, REGISTERED, PICKING
    );

    public boolean isCancellable() {
        return CANCELLABLE_STATUSES.contains(this);
    }

    public static CancellableFulfillmentWorkStatus from(String workStatusCode) {
        if (FormatValidator.hasNoValue(workStatusCode)) {
            throw new InvalidFulfillmentWorkStatusException("null");
        }
        try {
            return valueOf(workStatusCode);
        } catch (IllegalArgumentException e) {
            throw new InvalidFulfillmentWorkStatusException(workStatusCode);
        }
    }
}
