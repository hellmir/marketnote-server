package com.personal.marketnote.reward.domain.attendance;

import com.personal.marketnote.reward.domain.exception.InvalidRewardQuantityException;

public final class RewardQuantity {
    private final long value;

    private RewardQuantity(long value) {
        this.value = value;
    }

    public static RewardQuantity of(long value) {
        if (value < 0L) {
            throw new InvalidRewardQuantityException(value);
        }
        return new RewardQuantity(value);
    }

    public static RewardQuantity zero() {
        return new RewardQuantity(0L);
    }

    public RewardQuantity add(RewardQuantity other) {
        return new RewardQuantity(Math.addExact(value, other.value));
    }

    public long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RewardQuantity other)) {
            return false;
        }
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }
}
