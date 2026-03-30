package com.personal.marketnote.fulfillment.adapter.out.persistence.delivery.entity;

import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistration;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistrationSnapshotState;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "fassto_delivery_registration")
@EntityListeners(value = AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FulfillmentDeliveryRegistrationJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static FulfillmentDeliveryRegistrationJpaEntity from(FulfillmentDeliveryRegistration registration) {
        return FulfillmentDeliveryRegistrationJpaEntity.builder()
                .id(registration.getId())
                .orderId(registration.getOrderId())
                .createdAt(registration.getCreatedAt())
                .build();
    }

    public FulfillmentDeliveryRegistration toDomain() {
        return FulfillmentDeliveryRegistration.from(
                FulfillmentDeliveryRegistrationSnapshotState.builder()
                        .id(id)
                        .orderId(orderId)
                        .createdAt(createdAt)
                        .build()
        );
    }
}
