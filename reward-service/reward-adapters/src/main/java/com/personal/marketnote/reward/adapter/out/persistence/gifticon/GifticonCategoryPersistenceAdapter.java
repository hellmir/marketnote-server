package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonCategoryJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonCategoryMappingJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository.GifticonCategoryJpaRepository;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository.GifticonCategoryMappingJpaRepository;
import com.personal.marketnote.reward.domain.exception.GifticonCategoryNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.port.out.gifticon.*;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class GifticonCategoryPersistenceAdapter implements
        FindGifticonCategoryPort, SaveGifticonCategoryPort, UpdateGifticonCategoryPort,
        FindGifticonCategoryMappingPort, SaveGifticonCategoryMappingPort {

    private final GifticonCategoryJpaRepository categoryRepository;
    private final GifticonCategoryMappingJpaRepository mappingRepository;

    @Override
    public Optional<GifticonCategory> findByCategoryCode(String categoryCode) {
        return categoryRepository.findByCategoryCode(categoryCode)
                .map(GifticonCategoryJpaEntity::toDomain);
    }

    @Override
    public Optional<GifticonCategory> findById(Long id) {
        return categoryRepository.findById(id)
                .map(GifticonCategoryJpaEntity::toDomain);
    }

    @Override
    public GifticonCategory save(GifticonCategory category) {
        GifticonCategoryJpaEntity saved = categoryRepository.save(GifticonCategoryJpaEntity.from(category));
        return saved.toDomain();
    }

    @Override
    public void update(GifticonCategory category) {
        GifticonCategoryJpaEntity entity = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new GifticonCategoryNotFoundException(category.getId()));
        entity.updateFrom(category);
    }

    @Override
    public Optional<Long> findCategoryIdByGiftishowCategorySeq(String giftishowCategorySeq) {
        return mappingRepository.findByGiftishowCategorySeq(giftishowCategorySeq)
                .map(GifticonCategoryMappingJpaEntity::getGifticonCategoryId);
    }

    @Override
    public void save(String giftishowCategorySeq, Long gifticonCategoryId) {
        mappingRepository.save(GifticonCategoryMappingJpaEntity.of(giftishowCategorySeq, gifticonCategoryId));
    }
}
