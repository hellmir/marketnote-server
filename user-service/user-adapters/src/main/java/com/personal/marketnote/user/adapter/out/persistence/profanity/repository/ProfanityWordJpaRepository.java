package com.personal.marketnote.user.adapter.out.persistence.profanity.repository;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.profanity.entity.ProfanityWordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfanityWordJpaRepository extends JpaRepository<ProfanityWordJpaEntity, Long> {
    List<ProfanityWordJpaEntity> findAllByStatus(EntityStatus status);
}
