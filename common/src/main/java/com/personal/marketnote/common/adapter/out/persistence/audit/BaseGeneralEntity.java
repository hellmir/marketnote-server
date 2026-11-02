package com.personal.marketnote.common.adapter.out.persistence.audit;

import com.personal.marketnote.common.domain.EntityStatus;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static jakarta.persistence.GenerationType.IDENTITY;

@EntityListeners(value = AuditingEntityListener.class)
@MappedSuperclass
@Getter
public abstract class BaseGeneralEntity extends BaseEntity {
    protected BaseGeneralEntity() {
        status = EntityStatus.ACTIVE;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityStatus status;

    protected void activate() {
        status = EntityStatus.ACTIVE;
    }

    protected void deactivate() {
        status = EntityStatus.INACTIVE;
    }

    protected void hide() {
        status = EntityStatus.UNEXPOSED;
    }
}
