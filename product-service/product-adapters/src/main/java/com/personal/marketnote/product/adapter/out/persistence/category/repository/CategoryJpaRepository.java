package com.personal.marketnote.product.adapter.out.persistence.category.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.category.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {
    @Query("""
            SELECT c
            FROM CategoryJpaEntity c
            WHERE 1 = 1
              AND c.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
              AND (
                    (:parentId IS NULL AND c.parentCategoryId IS NULL)
                    OR (:parentId IS NOT NULL AND c.parentCategoryId = :parentId)
                  )
            ORDER BY c.id ASC
            """)
    List<CategoryJpaEntity> findActiveByParentId(@Param("parentId") Long parentId);

    @Query("""
            SELECT c
            FROM CategoryJpaEntity c
            WHERE 1 = 1
              AND c.id IN :ids
              AND c.status = :status
            """)
    List<CategoryJpaEntity> findAllByIdInAndStatus(@Param("ids") List<Long> ids, @Param("status") EntityStatus status);

    boolean existsByParentCategoryIdAndStatus(Long parentCategoryId, EntityStatus status);
}
