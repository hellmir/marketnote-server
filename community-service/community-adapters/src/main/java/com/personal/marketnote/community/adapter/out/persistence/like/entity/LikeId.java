package com.personal.marketnote.community.adapter.out.persistence.like.entity;

import com.personal.marketnote.community.domain.like.LikeTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LikeId implements Serializable {
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 15)
    private LikeTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LikeId likeId = (LikeId) o;
        return targetType == likeId.targetType
                && Objects.equals(targetId, likeId.targetId)
                && Objects.equals(userId, likeId.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetType, targetId, userId);
    }
}
