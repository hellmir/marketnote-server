package com.personal.marketnote.community.adapter.out.persistence.report.repository;

import com.personal.marketnote.community.adapter.out.persistence.report.entity.ReportId;
import com.personal.marketnote.community.adapter.out.persistence.report.entity.ReportJpaEntity;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportJpaRepository extends JpaRepository<ReportJpaEntity, ReportId> {
    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM ReportJpaEntity r
            WHERE r.id.targetType = :targetType
              AND r.id.targetId = :targetId
              AND r.id.reporterId = :reporterId
            """)
    boolean existsByTargetTypeAndTargetIdAndReporterId(
            @Param("targetType") ReportTargetType targetType,
            @Param("targetId") Long targetId,
            @Param("reporterId") Long reporterId
    );

    @Query("""
            SELECT r
            FROM ReportJpaEntity r
            WHERE r.id.targetType = :targetType
              AND r.id.targetId = :targetId
            ORDER BY r.createdAt DESC
            """)
    List<ReportJpaEntity> findByTargetTypeAndTargetId(
            @Param("targetType") ReportTargetType targetType,
            @Param("targetId") Long targetId
    );
}
