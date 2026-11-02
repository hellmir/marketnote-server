package com.personal.marketnote.file.adapter.out.persistence.file.repository;

import com.personal.marketnote.file.adapter.out.persistence.file.entity.ResizedFileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResizedFileJpaRepository extends JpaRepository<ResizedFileJpaEntity, Long> {
    @Query("""
            SELECT r
            FROM ResizedFileJpaEntity r
            WHERE r.file.id IN :fileIds
              AND r.status = 'ACTIVE'
            ORDER BY r.id DESC
            """)
    List<ResizedFileJpaEntity> findAllByFile_IdIn(@Param("fileIds") List<Long> fileIds);
}

