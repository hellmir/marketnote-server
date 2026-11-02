package com.personal.marketnote.file.adapter.out.persistence.file.repository;

import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.FileJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileJpaRepository extends JpaRepository<FileJpaEntity, Long> {
    @Query("""
            SELECT f
            FROM FileJpaEntity f
            WHERE f.ownerType = :ownerType
              AND f.ownerId = :ownerId
              AND f.status = 'ACTIVE'
            ORDER BY f.id ASC
            """)
    List<FileJpaEntity> findAllByOwnerTypeAndOwnerIdOrderByIdAsc(
            @Param("ownerType") OwnerType ownerType,
            @Param("ownerId") Long ownerId
    );

    @Query("""
            SELECT f
            FROM FileJpaEntity f
            WHERE f.ownerType = :ownerType
              AND f.ownerId = :ownerId
              AND f.sort = :sort
              AND f.status = 'ACTIVE'
            ORDER BY f.orderNum DESC
            """)
    List<FileJpaEntity> findByOwnerTypeAndOwnerIdAndSort(
            @Param("ownerType") OwnerType ownerType,
            @Param("ownerId") Long ownerId,
            @Param("sort") String sort,
            Pageable pageable
    );

    @Query("""
            SELECT f
            FROM FileJpaEntity f
            WHERE f.id IN :fileIds
              AND f.status = 'ACTIVE'
            ORDER BY f.orderNum DESC
            """)
    List<FileJpaEntity> findByIds(List<Long> fileIds);
}
