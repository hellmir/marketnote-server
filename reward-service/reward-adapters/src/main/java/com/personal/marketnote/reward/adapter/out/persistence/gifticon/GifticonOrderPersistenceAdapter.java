package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonOrderJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository.GifticonOrderJpaRepository;
import com.personal.marketnote.reward.domain.exception.DuplicateGifticonOrderException;
import com.personal.marketnote.reward.domain.exception.GifticonOrderNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonOrderPort;
import com.personal.marketnote.reward.port.out.gifticon.SaveGifticonOrderPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonOrderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class GifticonOrderPersistenceAdapter
        implements SaveGifticonOrderPort, UpdateGifticonOrderPort, FindGifticonOrderPort {

    private final GifticonOrderJpaRepository gifticonOrderJpaRepository;

    @Override
    public GifticonOrder save(GifticonOrder order) {
        try {
            GifticonOrderJpaEntity entity = GifticonOrderJpaEntity.from(order);
            GifticonOrderJpaEntity savedEntity = gifticonOrderJpaRepository.save(entity);
            return savedEntity.toDomain();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateGifticonOrderException(order.getTrId());
        }
    }

    @Override
    public void update(GifticonOrder order) {
        GifticonOrderJpaEntity entity = gifticonOrderJpaRepository.findByTrId(order.getTrId())
                .orElseThrow(() -> new GifticonOrderNotFoundException(order.getTrId()));
        entity.updateFrom(order);
    }

    @Override
    public Optional<GifticonOrder> findByTrId(String trId) {
        return gifticonOrderJpaRepository.findByTrId(trId)
                .map(GifticonOrderJpaEntity::toDomain);
    }

    @Override
    public boolean existsByTrId(String trId) {
        return gifticonOrderJpaRepository.existsByTrId(trId);
    }
}
