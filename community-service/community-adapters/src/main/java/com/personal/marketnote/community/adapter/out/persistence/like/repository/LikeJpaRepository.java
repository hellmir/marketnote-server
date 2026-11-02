package com.personal.marketnote.community.adapter.out.persistence.like.repository;

import com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeId;
import com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeJpaRepository extends JpaRepository<LikeJpaEntity, LikeId> {
    @Query("""
            SELECT COUNT(l) > 0
            FROM LikeJpaEntity l
            WHERE l.id.targetType = :targetType
              AND l.id.targetId = :targetId
              AND l.id.userId = :userId
              AND l.status = 'ACTIVE'
            """)
    boolean existsByTarget(
            @Param("targetType") LikeTargetType targetType,
            @Param("targetId") Long targetId,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT l
            FROM LikeJpaEntity l
            WHERE l.id.targetType = :targetType
            AND l.id.targetId = :targetId
            AND l.id.userId = :userId
            """)
    Optional<LikeJpaEntity> findByTargetAndUser(
            @Param("targetType") LikeTargetType targetType,
            @Param("targetId") Long targetId,
            @Param("userId") Long userId
    );
}
