package com.personal.marketnote.fulfillment.domain.shipping;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import com.personal.marketnote.fulfillment.domain.exception.InvalidShippingStatusTransitionException;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingTracker {
    private Long id;
    private Long orderId;
    private String trackingNumber;
    private String carrierCode;
    private ShippingStatus shippingStatus;
    private boolean pollingActive;
    private LocalDateTime lastPolledAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ShippingTracker from(ShippingTrackerCreateState state) {
        if (FormatValidator.hasNoValue(state.getOrderId())) {
            throw new FulfillmentQueryParameterNoValueException("orderId");
        }
        return ShippingTracker.builder()
                .orderId(state.getOrderId())
                .shippingStatus(ShippingStatus.PREPARING)
                .pollingActive(true)
                .build();
    }

    public static ShippingTracker from(ShippingTrackerSnapshotState state) {
        return ShippingTracker.builder()
                .id(state.getId())
                .orderId(state.getOrderId())
                .trackingNumber(state.getTrackingNumber())
                .carrierCode(state.getCarrierCode())
                .shippingStatus(state.getShippingStatus())
                .pollingActive(state.isPollingActive())
                .lastPolledAt(state.getLastPolledAt())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public void startShipping(String trackingNumber, String carrierCode) {
        if (FormatValidator.hasNoValue(trackingNumber)) {
            throw new FulfillmentQueryParameterNoValueException("trackingNumber", "startShipping");
        }
        if (FormatValidator.hasNoValue(carrierCode)) {
            throw new FulfillmentQueryParameterNoValueException("carrierCode", "startShipping");
        }
        validateTransition(ShippingStatus.SHIPPING);
        this.shippingStatus = ShippingStatus.SHIPPING;
        this.trackingNumber = trackingNumber;
        this.carrierCode = carrierCode;
    }

    public void completeDelivery() {
        validateTransition(ShippingStatus.DELIVERED);
        this.shippingStatus = ShippingStatus.DELIVERED;
        this.pollingActive = false;
    }

    public void cancel() {
        validateTransition(ShippingStatus.CANCELLED);
        this.shippingStatus = ShippingStatus.CANCELLED;
        this.pollingActive = false;
    }

    public void startReturnShipping() {
        validateTransition(ShippingStatus.RETURN_SHIPPING);
        this.shippingStatus = ShippingStatus.RETURN_SHIPPING;
        this.pollingActive = true;
    }

    public void completeReturnDelivery() {
        validateTransition(ShippingStatus.RETURN_DELIVERED);
        this.shippingStatus = ShippingStatus.RETURN_DELIVERED;
        this.pollingActive = false;
    }

    public void markDeliveryFailed() {
        validateTransition(ShippingStatus.DELIVERY_FAILED);
        this.shippingStatus = ShippingStatus.DELIVERY_FAILED;
        this.pollingActive = false;
    }

    public void advanceToShipping() {
        validateTransition(ShippingStatus.SHIPPING);
        this.shippingStatus = ShippingStatus.SHIPPING;
    }

    public void updateTrackingInfo(String trackingNumber, String carrierCode) {
        if (!isShipping()) {
            throw new InvalidShippingStatusTransitionException(this.shippingStatus, ShippingStatus.SHIPPING);
        }
        if (FormatValidator.hasNoValue(trackingNumber)) {
            throw new FulfillmentQueryParameterNoValueException("trackingNumber", "updateTrackingInfo");
        }
        if (FormatValidator.hasNoValue(carrierCode)) {
            throw new FulfillmentQueryParameterNoValueException("carrierCode", "updateTrackingInfo");
        }
        this.trackingNumber = trackingNumber;
        this.carrierCode = carrierCode;
    }

    public void updateLastPolledAt(LocalDateTime polledAt) {
        if (FormatValidator.hasNoValue(polledAt)) {
            throw new FulfillmentQueryParameterNoValueException("polledAt", "updateLastPolledAt");
        }
        this.lastPolledAt = polledAt;
    }

    public boolean isPreparing() {
        return shippingStatus.isPreparing();
    }

    public boolean isShipping() {
        return shippingStatus.isShipping();
    }

    public boolean isDelivered() {
        return shippingStatus.isDelivered();
    }

    public boolean isCancelled() {
        return shippingStatus.isCancelled();
    }

    public boolean isReturnShipping() {
        return shippingStatus.isReturnShipping();
    }

    public boolean isReturnDelivered() {
        return shippingStatus.isReturnDelivered();
    }

    public boolean isDeliveryFailed() {
        return shippingStatus.isDeliveryFailed();
    }

    public boolean hasStatus(ShippingStatus status) {
        return this.shippingStatus == status;
    }

    public boolean hasNoTrackingNumber() {
        return FormatValidator.hasNoValue(trackingNumber);
    }

    private void validateTransition(ShippingStatus target) {
        if (!this.shippingStatus.canTransitionTo(target)) {
            throw new InvalidShippingStatusTransitionException(this.shippingStatus, target);
        }
    }
}
