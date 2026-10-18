package com.personal.marketnote.product.adapter.out.persistence.tagsync.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "product_tag_change_sync_log",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_tag_change_sync_log_event_id",
                columnNames = "event_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductTagChangeSyncLogJpaEntity extends BaseGeneralEntity {

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    public static ProductTagChangeSyncLogJpaEntity of(UUID eventId) {
        return ProductTagChangeSyncLogJpaEntity.builder()
                .eventId(eventId)
                .build();
    }
}
