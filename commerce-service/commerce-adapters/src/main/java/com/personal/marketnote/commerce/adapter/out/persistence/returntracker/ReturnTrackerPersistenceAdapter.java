package com.personal.marketnote.commerce.adapter.out.persistence.returntracker;

import com.personal.marketnote.commerce.adapter.out.persistence.returntracker.entity.ReturnTrackerJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.returntracker.mapper.ReturnTrackerJpaEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.returntracker.repository.ReturnTrackerJpaRepository;
import com.personal.marketnote.commerce.domain.returntracker.ReturnInspectionStatus;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.port.out.returntracker.FindReturnTrackerPort;
import com.personal.marketnote.commerce.port.out.returntracker.SaveReturnTrackerPort;
import com.personal.marketnote.commerce.port.out.returntracker.UpdateReturnTrackerPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class ReturnTrackerPersistenceAdapter implements SaveReturnTrackerPort, FindReturnTrackerPort, UpdateReturnTrackerPort {
    private final ReturnTrackerJpaRepository returnTrackerJpaRepository;

    @Override
    public ReturnTracker save(ReturnTracker returnTracker) {
        ReturnTrackerJpaEntity entity = ReturnTrackerJpaEntity.from(returnTracker);
        ReturnTrackerJpaEntity saved = returnTrackerJpaRepository.save(entity);
        return ReturnTrackerJpaEntityToDomainMapper.toDomain(saved);
    }

    @Override
    public Optional<ReturnTracker> findByOrderId(Long orderId) {
        return returnTrackerJpaRepository.findByOrderId(orderId)
                .map(ReturnTrackerJpaEntityToDomainMapper::toDomain);
    }

    @Override
    public List<ReturnTracker> findByInspectionStatus(ReturnInspectionStatus inspectionStatus) {
        return returnTrackerJpaRepository.findByInspectionStatus(inspectionStatus).stream()
                .map(ReturnTrackerJpaEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public ReturnTracker update(ReturnTracker returnTracker) {
        ReturnTrackerJpaEntity entity = ReturnTrackerJpaEntity.from(returnTracker);
        ReturnTrackerJpaEntity updated = returnTrackerJpaRepository.save(entity);
        return ReturnTrackerJpaEntityToDomainMapper.toDomain(updated);
    }
}
