package com.personal.marketnote.fulfillment.adapter.out.persistence.shipping.entity;

import com.personal.marketnote.fulfillment.domain.shipping.ShippingStatus;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTrackerSnapshotState;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_tracker")
@EntityListeners(value = AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingTrackerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "carrier_code")
    private String carrierCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_status", nullable = false)
    private ShippingStatus shippingStatus;

    @Column(name = "polling_active", nullable = false)
    private boolean pollingActive;

    @Column(name = "last_polled_at")
    private LocalDateTime lastPolledAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    public static ShippingTrackerJpaEntity from(ShippingTracker shippingTracker) {
        return ShippingTrackerJpaEntity.builder()
                .id(shippingTracker.getId())
                .orderId(shippingTracker.getOrderId())
                .trackingNumber(shippingTracker.getTrackingNumber())
                .carrierCode(shippingTracker.getCarrierCode())
                .shippingStatus(shippingTracker.getShippingStatus())
                .pollingActive(shippingTracker.isPollingActive())
                .lastPolledAt(shippingTracker.getLastPolledAt())
                .createdAt(shippingTracker.getCreatedAt())
                .modifiedAt(shippingTracker.getModifiedAt())
                .build();
    }

    public ShippingTracker toDomain() {
        return ShippingTracker.from(
                ShippingTrackerSnapshotState.builder()
                        .id(id)
                        .orderId(orderId)
                        .trackingNumber(trackingNumber)
                        .carrierCode(carrierCode)
                        .shippingStatus(shippingStatus)
                        .pollingActive(pollingActive)
                        .lastPolledAt(lastPolledAt)
                        .createdAt(createdAt)
                        .modifiedAt(modifiedAt)
                        .build()
        );
    }
}
