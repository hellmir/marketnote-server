package com.personal.marketnote.common.adapter.out.persistence.audit;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(value = AuditingEntityListener.class)
@MappedSuperclass
@Getter
public abstract class BaseOrderedGeneralEntity extends BaseGeneralEntity {
    private Long orderNum;

    public void setIdToOrderNum() {
        orderNum = getId();
    }

    protected void updateOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }
}
