package com.personal.marketnote.common.configuration.kafka;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DltMessageResolutionJpaRepository extends JpaRepository<DltMessageResolutionJpaEntity, Long> {

    List<DltMessageResolutionJpaEntity> findByOriginalTopic(String originalTopic);
}
