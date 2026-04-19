package com.personal.marketnote.community.adapter.out.persistence.like.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.domain.like.Like;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "likes")
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class LikeJpaEntity extends BaseEntity {
    @EmbeddedId
    private LikeId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    public static LikeJpaEntity from(Like like) {
        LikeId likeId = new LikeId(
                like.getTargetType(),
                like.getTargetId(),
                like.getUserId()
        );

        return LikeJpaEntity.builder()
                .id(likeId)
                .status(like.getStatus())
                .build();
    }

    public void updateFrom(Like like) {
        status = like.getStatus();
    }
}
