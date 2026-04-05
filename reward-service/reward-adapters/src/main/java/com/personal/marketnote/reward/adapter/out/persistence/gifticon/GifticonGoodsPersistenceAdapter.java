package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonGoodsJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository.GifticonGoodsJpaRepository;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.SaveGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonGoodsPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class GifticonGoodsPersistenceAdapter implements FindGifticonGoodsPort, SaveGifticonGoodsPort, UpdateGifticonGoodsPort {

    private final GifticonGoodsJpaRepository repository;

    @Override
    public Optional<GifticonGoods> findByGoodsCode(String goodsCode) {
        return repository.findByGoodsCode(goodsCode)
                .map(GifticonGoodsJpaEntity::toDomain);
    }

    @Override
    public List<GifticonGoods> findAllByGoodsStatus(String goodsStatus) {
        return repository.findAllByGoodsStatus(goodsStatus).stream()
                .map(GifticonGoodsJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void save(GifticonGoods goods) {
        repository.save(GifticonGoodsJpaEntity.from(goods));
    }

    @Override
    public void update(GifticonGoods goods) {
        GifticonGoodsJpaEntity entity = repository.findById(goods.getId())
                .orElseThrow(() -> new GifticonGoodsNotFoundException(goods.getGoodsCode()));
        entity.updateFrom(goods);
    }
}
