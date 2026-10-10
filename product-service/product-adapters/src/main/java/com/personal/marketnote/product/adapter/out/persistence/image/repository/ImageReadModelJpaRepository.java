package com.personal.marketnote.product.adapter.out.persistence.image.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.image.entity.ImageReadModelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageReadModelJpaRepository extends JpaRepository<ImageReadModelJpaEntity, Long> {

    Optional<ImageReadModelJpaEntity> findByImageId(Long imageId);

    @Query("""
            SELECT DISTINCT i FROM ImageReadModelJpaEntity i
            LEFT JOIN FETCH i.resizedFiles
            WHERE i.targetId = :targetId
              AND i.fileSort = :fileSort
              AND i.status = :status
            ORDER BY i.sortOrder ASC
            """)
    List<ImageReadModelJpaEntity> findByTargetIdAndFileSortAndStatusOrderBySortOrderAsc(
            @Param("targetId") Long targetId,
            @Param("fileSort") String fileSort,
            @Param("status") EntityStatus status
    );
}
