package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonBrandJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository.GifticonBrandJpaRepository;
import com.personal.marketnote.reward.domain.exception.GifticonBrandNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonBrand;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonBrandPort;
import com.personal.marketnote.reward.port.out.gifticon.SaveGifticonBrandPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonBrandPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class GifticonBrandPersistenceAdapter implements FindGifticonBrandPort, SaveGifticonBrandPort, UpdateGifticonBrandPort {

    private final GifticonBrandJpaRepository repository;

    @Override
    public Optional<GifticonBrand> findByBrandCode(String brandCode) {
        return repository.findByBrandCode(brandCode)
                .map(GifticonBrandJpaEntity::toDomain);
    }

    @Override
    public void save(GifticonBrand brand) {
        repository.save(GifticonBrandJpaEntity.from(brand));
    }

    @Override
    public void update(GifticonBrand brand) {
        GifticonBrandJpaEntity entity = repository.findById(brand.getId())
                .orElseThrow(() -> new GifticonBrandNotFoundException(brand.getBrandCode()));
        entity.updateFrom(brand);
    }
}
