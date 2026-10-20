package com.personal.marketnote.community.adapter.out.persistence.post.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.post.entity.PostJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long>, PostQueryRepository {
    boolean existsByParentIdAndStatus(Long parentId, EntityStatus status);
}
