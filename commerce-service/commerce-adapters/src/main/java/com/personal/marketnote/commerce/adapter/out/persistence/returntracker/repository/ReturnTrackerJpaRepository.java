package com.personal.marketnote.commerce.adapter.out.persistence.returntracker.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.returntracker.entity.ReturnTrackerJpaEntity;
import com.personal.marketnote.commerce.domain.returntracker.ReturnInspectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReturnTrackerJpaRepository extends JpaRepository<ReturnTrackerJpaEntity, Long> {
    Optional<ReturnTrackerJpaEntity> findByOrderId(Long orderId);

    List<ReturnTrackerJpaEntity> findByInspectionStatus(ReturnInspectionStatus inspectionStatus);
}
