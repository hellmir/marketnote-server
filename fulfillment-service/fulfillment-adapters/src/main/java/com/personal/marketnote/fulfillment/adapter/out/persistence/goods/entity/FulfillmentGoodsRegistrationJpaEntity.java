package com.personal.marketnote.fulfillment.adapter.out.persistence.goods.entity;

import com.personal.marketnote.fulfillment.domain.goods.FulfillmentGoodsRegistration;
import com.personal.marketnote.fulfillment.domain.goods.FulfillmentGoodsRegistrationSnapshotState;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fassto_goods_registration")
@EntityListeners(value = AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FulfillmentGoodsRegistrationJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_key", nullable = false, unique = true)
    private UUID productKey;

    @Column(name = "fassto_product_code", nullable = false, unique = true, updatable = false, length = 25)
    private String fasstoProductCode;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static FulfillmentGoodsRegistrationJpaEntity from(FulfillmentGoodsRegistration registration) {
        return FulfillmentGoodsRegistrationJpaEntity.builder()
                .id(registration.getId())
                .productKey(registration.getProductKey())
                .fasstoProductCode(registration.getFasstoProductCode())
                .createdAt(registration.getCreatedAt())
                .build();
    }

    public FulfillmentGoodsRegistration toDomain() {
        return FulfillmentGoodsRegistration.from(
                FulfillmentGoodsRegistrationSnapshotState.builder()
                        .id(id)
                        .productKey(productKey)
                        .fasstoProductCode(fasstoProductCode)
                        .createdAt(createdAt)
                        .build()
        );
    }
}
