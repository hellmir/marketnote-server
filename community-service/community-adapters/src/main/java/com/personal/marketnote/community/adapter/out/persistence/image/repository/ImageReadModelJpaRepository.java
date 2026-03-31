package com.personal.marketnote.community.adapter.out.persistence.image.repository;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.image.entity.ImageReadModelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageReadModelJpaRepository extends JpaRepository<ImageReadModelJpaEntity, Long> {

    Optional<ImageReadModelJpaEntity> findByImageId(Long imageId);

    List<ImageReadModelJpaEntity> findByTargetIdAndFileSortAndStatusOrderBySortOrderAsc(
            Long targetId, String fileSort, EntityStatus status
    );
}
