package com.personal.marketnote.fulfillment.adapter.out.persistence.goods.entity;

import com.personal.marketnote.fulfillment.domain.goods.FasstoGoodsRegistration;
import com.personal.marketnote.fulfillment.domain.goods.FasstoGoodsRegistrationSnapshotState;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "fassto_goods_registration")
@EntityListeners(value = AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FasstoGoodsRegistrationJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static FasstoGoodsRegistrationJpaEntity from(FasstoGoodsRegistration registration) {
        return FasstoGoodsRegistrationJpaEntity.builder()
                .id(registration.getId())
                .productId(registration.getProductId())
                .createdAt(registration.getCreatedAt())
                .build();
    }

    public FasstoGoodsRegistration toDomain() {
        return FasstoGoodsRegistration.from(
                FasstoGoodsRegistrationSnapshotState.builder()
                        .id(id)
                        .productId(productId)
                        .createdAt(createdAt)
                        .build()
        );
    }
}
