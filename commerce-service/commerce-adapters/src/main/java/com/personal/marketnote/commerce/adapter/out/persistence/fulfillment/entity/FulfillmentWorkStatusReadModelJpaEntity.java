package com.personal.marketnote.commerce.adapter.out.persistence.fulfillment.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(
        name = "fulfillment_work_status_read_models",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_fulfillment_work_status_read_model_order_id",
                columnNames = "orderId"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FulfillmentWorkStatusReadModelJpaEntity extends BaseGeneralEntity {

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private String workStatus;

    public static FulfillmentWorkStatusReadModelJpaEntity of(Long orderId, String workStatus) {
        return FulfillmentWorkStatusReadModelJpaEntity.builder()
                .orderId(orderId)
                .workStatus(workStatus)
                .build();
    }

    public void updateFrom(String workStatus) {
        this.workStatus = workStatus;
        activate();
    }
}
