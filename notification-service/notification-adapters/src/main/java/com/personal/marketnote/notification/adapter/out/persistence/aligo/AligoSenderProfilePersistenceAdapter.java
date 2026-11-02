package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.notification.adapter.out.mapper.AligoSenderProfileJpaEntityToDomainMapper;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoSenderProfileJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.repository.AligoSenderProfileJpaRepository;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileNotFoundException;
import com.personal.marketnote.notification.port.out.aligo.FindAligoSenderProfilePort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoSenderProfilePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoSenderProfilePort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class AligoSenderProfilePersistenceAdapter
        implements FindAligoSenderProfilePort, SaveAligoSenderProfilePort, UpdateAligoSenderProfilePort {

    private final AligoSenderProfileJpaRepository repository;

    @Override
    public Optional<AligoSenderProfile> findBySenderKey(String senderKey) {
        return repository.findBySenderKey(senderKey)
                .flatMap(AligoSenderProfileJpaEntityToDomainMapper::mapToDomain);
    }

    @Override
    public List<AligoSenderProfile> findAll() {
        return repository.findAll().stream()
                .flatMap(entity -> AligoSenderProfileJpaEntityToDomainMapper.mapToDomain(entity).stream())
                .toList();
    }

    @Override
    public AligoSenderProfile save(AligoSenderProfile senderProfile) {
        AligoSenderProfileJpaEntity entity = AligoSenderProfileJpaEntity.from(senderProfile);
        AligoSenderProfileJpaEntity saved = repository.save(entity);
        return AligoSenderProfileJpaEntityToDomainMapper.mapToDomain(saved).orElseThrow();
    }

    @Override
    public void update(AligoSenderProfile senderProfile) {
        AligoSenderProfileJpaEntity entity = repository.findById(senderProfile.getId())
                .orElseThrow(() -> new AligoSenderProfileNotFoundException(senderProfile.getId()));
        entity.updateFrom(senderProfile);
    }
}
