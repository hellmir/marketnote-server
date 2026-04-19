package com.personal.marketnote.common.domain;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.Getter;

@Getter
public class BaseDomain {
    protected EntityStatus status = EntityStatus.ACTIVE;

    protected void activate() {
        status = EntityStatus.ACTIVE;
    }

    protected void deactivate() {
        status = EntityStatus.INACTIVE;
    }

    protected void hide() {
        status = EntityStatus.UNEXPOSED;
    }

    public void updateStatus(boolean isActive) {
        status = EntityStatus.from(isActive);
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean isInactive() {
        return status.isInactive();
    }
}
