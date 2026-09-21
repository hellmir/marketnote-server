package com.personal.marketnote.reward.domain.attendance;

import com.personal.marketnote.reward.domain.exception.InvalidContinuousPeriodException;

public final class ContinuousPeriod {
    private final short value;

    private ContinuousPeriod(short value) {
        this.value = value;
    }

    public static ContinuousPeriod of(short value) {
        if (value <= 0) {
            throw new InvalidContinuousPeriodException(value);
        }
        return new ContinuousPeriod(value);
    }

    public short getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContinuousPeriod other)) {
            return false;
        }
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return Short.hashCode(value);
    }
}
